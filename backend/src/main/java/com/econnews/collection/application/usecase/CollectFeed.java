package com.econnews.collection.application.usecase;

import com.econnews.article.application.NormalizeCollectedArticle;
import com.econnews.article.domain.repository.CollectedArticleRepository;
import com.econnews.collection.application.exception.*;
import com.econnews.collection.application.port.*;
import com.econnews.collection.domain.model.CollectionRun;
import com.econnews.collection.domain.repository.CollectionRepository;
import java.time.*;
import java.util.concurrent.Semaphore;

public class CollectFeed {

    private final CollectionRepository runs;
    private final CollectedArticleRepository articles;
    private final CollectionTransaction tx;
    private final FeedReader reader;
    private final NormalizeCollectedArticle normalize;
    private final Duration cooldown;
    private final Semaphore capacity = new Semaphore(2);

    public CollectFeed(
        CollectionRepository runs,
        CollectedArticleRepository articles,
        CollectionTransaction tx,
        FeedReader reader,
        NormalizeCollectedArticle normalize,
        Duration cooldown
    ) {
        this.runs = runs;
        this.articles = articles;
        this.tx = tx;
        this.reader = reader;
        this.normalize = normalize;
        this.cooldown = cooldown;
    }

    public CollectionRun collect(long sourceId, long feedId, long requestedBy) {
        return collect(sourceId, feedId, requestedBy, cooldown);
    }

    public CollectionRun collectScheduled(long sourceId, long feedId, Duration interval) {
        Duration minimumGap = interval.compareTo(cooldown) < 0 ? cooldown : interval;
        return collect(sourceId, feedId, null, minimumGap);
    }

    private CollectionRun collect(long sourceId, long feedId, Long requestedBy, Duration minimumGap) {
        if (!capacity.tryAcquire()) throw new CollectionException(CollectionError.COLLECTION_BUSY);
        try {
            CollectionRun run = tx.execute(() -> {
                runs.interruptStale(Instant.now().minus(Duration.ofMinutes(5)));
                var target = runs
                    .lockFeed(sourceId, feedId)
                    .orElseThrow(() ->
                        new CollectionException(CollectionError.RESOURCE_NOT_FOUND)
                    );
                if (!target.sourceEnabled() || !target.feedEnabled()) throw new CollectionException(CollectionError.FEED_DISABLED);
                runs
                    .latest(feedId)
                    .ifPresent(last -> {
                        if (last.status().equals("RUNNING")) throw new CollectionException(CollectionError.COLLECTION_ALREADY_RUNNING);
                        if (
                            last.startedAt().plus(minimumGap).isAfter(Instant.now())
                        ) throw new CollectionException(CollectionError.COLLECTION_COOLDOWN);
                    });
                return runs.start(target, requestedBy);
            });
            try {
                var entries = reader.read(run.feedUrl());
                return tx.execute(() -> {
                    var current = runs
                        .lockFeed(sourceId, feedId)
                        .orElseThrow(() -> new FeedReadException("FEED_CHANGED"));
                    if (
                        !current.sourceEnabled() ||
                        !current.feedEnabled() ||
                        !current.url().equals(run.feedUrl())
                    ) throw new FeedReadException("FEED_CHANGED");
                    int inserted = 0,
                        duplicate = 0,
                        invalid = 0;
                    for (var entry : entries) {
                        com.econnews.article.domain.model.CollectedArticle article;
                        try {
                            article = normalize.normalize(entry);
                        } catch (IllegalArgumentException e) {
                            invalid++;
                            continue;
                        }
                        if (articles.insertIfNew(sourceId, feedId, article)) inserted++;
                        else duplicate++;
                    }
                    String status = invalid == 0
                        ? "SUCCESS"
                        : (inserted + duplicate > 0 ? "PARTIAL_SUCCESS" : "FAILED");
                    return runs.finish(
                        run.id(),
                        status,
                        inserted,
                        duplicate,
                        invalid,
                        invalid == 0 ? null : "INVALID_ENTRIES"
                    );
                });
            } catch (FeedReadException e) {
                return tx.execute(() -> runs.finish(run.id(), "FAILED", 0, 0, 0, e.code()));
            } catch (RuntimeException e) {
                tx.execute(() ->
                    runs.finish(run.id(), "FAILED", 0, 0, 0, "COLLECTION_STORAGE_ERROR")
                );
                throw e;
            }
        } finally {
            capacity.release();
        }
    }

    public CollectionRepository.Slice<CollectionRun> history(
        long sourceId,
        long feedId,
        int page,
        int size
    ) {
        if (page < 0 || size < 1 || size > 100) throw new CollectionException(CollectionError.INVALID_PAGINATION);
        return tx.execute(() -> {
            runs
                .lockFeed(sourceId, feedId)
                .orElseThrow(() ->
                    new CollectionException(CollectionError.RESOURCE_NOT_FOUND)
                );
            return runs.runs(sourceId, feedId, page, size);
        });
    }
}
