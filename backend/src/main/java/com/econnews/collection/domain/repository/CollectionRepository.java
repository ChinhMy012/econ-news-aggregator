package com.econnews.collection.domain.repository;

import com.econnews.collection.domain.model.*;
import java.time.Instant;
import java.util.*;

public interface CollectionRepository {
    record FeedTarget(
        long sourceId,
        long feedId,
        String url,
        boolean sourceEnabled,
        boolean feedEnabled
    ) {}

    record Slice<T>(List<T> items, long total) {}

    Optional<FeedTarget> lockFeed(long sourceId, long feedId);
    Optional<CollectionRun> latest(long feedId);
    CollectionRun start(FeedTarget target, long requestedBy);
    Optional<CollectionRun> run(long id);
    Slice<CollectionRun> runs(long sourceId, long feedId, int page, int size);
    CollectionRun finish(
        long id,
        String status,
        int inserted,
        int duplicate,
        int invalid,
        String errorCode
    );
    void interruptStale(Instant cutoff);
}
