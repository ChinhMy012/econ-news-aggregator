package com.econnews.article.domain.model;

import java.time.Instant;

public record ArticlePreview(
    long id,
    long sourceId,
    long firstFeedId,
    String title,
    String originalUrl,
    String summary,
    Instant publishedAt,
    Instant collectedAt,
    String status
) {}
