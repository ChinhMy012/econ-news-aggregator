package com.econnews.article.adapter.in.web.response;

import com.econnews.article.domain.model.*;
import java.time.Instant;

public record AdminArticleResponse(
    long id,
    long sourceId,
    String sourceName,
    String sourceWebsiteUrl,
    long firstFeedId,
    String title,
    String originalUrl,
    String summary,
    Instant publishedAt,
    Instant collectedAt,
    ArticleStatus status,
    ContentType contentType,
    String moderationReason,
    long version,
    Instant updatedAt
) {
    public static AdminArticleResponse from(ArticleDetails a) {
        return new AdminArticleResponse(
            a.id(),
            a.sourceId(),
            a.sourceName(),
            a.sourceWebsiteUrl(),
            a.firstFeedId(),
            a.title(),
            a.originalUrl(),
            a.summary(),
            a.publishedAt(),
            a.collectedAt(),
            a.status(),
            a.contentType(),
            a.moderationReason(),
            a.version(),
            a.updatedAt()
        );
    }
}
