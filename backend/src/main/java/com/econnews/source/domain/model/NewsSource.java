package com.econnews.source.domain.model;

import java.time.Instant;

public record NewsSource(
    long id,
    String code,
    String name,
    String websiteUrl,
    boolean enabled,
    long version,
    Instant createdAt,
    Instant updatedAt
) {}
