package com.econnews.article.domain.model;

import java.time.Instant;

public record CollectedArticle(
    String title,
    String originalUrl,
    String normalizedUrl,
    String urlHash,
    String summary,
    Instant publishedAt
) {}
