package com.econnews.article.domain.model;

import java.time.Instant;

public record ArticleDetails(
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
) {}
