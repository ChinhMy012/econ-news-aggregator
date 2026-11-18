package com.econnews.article.adapter.in.web.response;

import com.econnews.article.domain.model.*;
import java.time.Instant;

public record PublicArticleResponse(
    long id,
    long sourceId,
    String sourceName,
    String sourceWebsiteUrl,
    String title,
    String originalUrl,
    String summary,
    Instant publishedAt,
    ContentType contentType
) {
    public static PublicArticleResponse from(ArticleDetails a) {
        return new PublicArticleResponse(
            a.id(),
            a.sourceId(),
            a.sourceName(),
            a.sourceWebsiteUrl(),
            a.title(),
            a.originalUrl(),
            a.summary(),
            a.publishedAt(),
            a.contentType()
        );
    }
}
