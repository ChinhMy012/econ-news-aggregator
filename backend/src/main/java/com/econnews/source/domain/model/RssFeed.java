package com.econnews.source.domain.model;

import java.time.Instant;

public record RssFeed(
    long id,
    long sourceId,
    String name,
    String url,
    boolean enabled,
    long version,
    Instant createdAt,
    Instant updatedAt
) {}
