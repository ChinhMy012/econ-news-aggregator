package com.econnews.article.application;

import com.econnews.article.domain.model.CollectedArticle;
import com.econnews.collection.domain.model.FeedEntry;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.*;

public class NormalizeCollectedArticle {

    public CollectedArticle normalize(FeedEntry entry) {
        String title = text(entry.title(), 500);
        if (title == null || entry.url() == null) throw new IllegalArgumentException(
            "Missing article title or URL"
        );
        URI uri = URI.create(entry.url().strip()).normalize();
        String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
        if (
            !Set.of("http", "https").contains(scheme) ||
            uri.getHost() == null ||
            uri.getUserInfo() != null
        ) throw new IllegalArgumentException("Invalid article URL");
        String original = uri.toASCIIString();
        if (original.length() > 2048) throw new IllegalArgumentException("Article URL too long");
        int port = uri.getPort();
        if ((scheme.equals("https") && port == 443) || (scheme.equals("http") && port == 80)) port =
            -1;
        String path = uri.getRawPath();
        List<String> query = new ArrayList<>();
        if (uri.getRawQuery() != null) for (String pair : uri.getRawQuery().split("&")) {
            String key = URLDecoder.decode(
                pair.split("=", 2)[0],
                StandardCharsets.UTF_8
            ).toLowerCase(Locale.ROOT);
            if (!key.startsWith("utm_") && !Set.of("fbclid", "gclid").contains(key)) query.add(
                pair
            );
        }
        String normalized =
            scheme +
            "://" +
            uri.getHost().toLowerCase(Locale.ROOT) +
            (port == -1 ? "" : ":" + port) +
            (path == null || path.isEmpty() ? "/" : path) +
            (query.isEmpty() ? "" : "?" + String.join("&", query));
        normalized = URI.create(normalized).toASCIIString();
        if (normalized.length() > 2048) throw new IllegalArgumentException("Article URL too long");
        try {
            String hash = HexFormat.of().formatHex(
                MessageDigest.getInstance("SHA-256").digest(
                    normalized.getBytes(StandardCharsets.UTF_8)
                )
            );
            return new CollectedArticle(
                title,
                original,
                normalized,
                hash,
                text(entry.summary(), 4000),
                entry.publishedAt()
            );
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private String text(String input, int max) {
        if (input == null) return null;
        // Cosmetic text extraction only; frontend must always render as escaped text, never HTML.
        String cleaned = input.replaceAll("<[^>]*>", " ").replaceAll("\\s+", " ").strip();
        if (cleaned.isEmpty()) return null;
        return cleaned.codePointCount(0, cleaned.length()) > max
            ? cleaned.substring(0, cleaned.offsetByCodePoints(0, max))
            : cleaned;
    }
}
