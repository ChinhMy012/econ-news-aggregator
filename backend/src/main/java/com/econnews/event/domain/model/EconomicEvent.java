package com.econnews.event.domain.model;

import java.time.Instant;

public record EconomicEvent(
    long id,
    String title,
    String contentType,
    String overview,
    String status,
    String hiddenReason,
    long version,
    Instant createdAt,
    Instant updatedAt
) {}
