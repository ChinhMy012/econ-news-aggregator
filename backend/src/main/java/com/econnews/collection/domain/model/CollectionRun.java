package com.econnews.collection.domain.model;

import java.time.Instant;

public record CollectionRun(
    long id,
    long sourceId,
    long feedId,
    String feedUrl,
    long requestedBy,
    String status,
    int receivedCount,
    int insertedCount,
    int duplicateCount,
    int invalidCount,
    String errorCode,
    Instant startedAt,
    Instant finishedAt
) {}
