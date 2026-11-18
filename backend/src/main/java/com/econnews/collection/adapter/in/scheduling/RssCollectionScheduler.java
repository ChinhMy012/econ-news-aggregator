package com.econnews.collection.adapter.in.scheduling;

import com.econnews.collection.application.exception.CollectionException;
import com.econnews.collection.application.usecase.CollectFeed;
import com.econnews.collection.domain.repository.CollectionRepository;
import java.time.Duration;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Profile("!provision-admin")
public class RssCollectionScheduler {

    private static final Logger log = LoggerFactory.getLogger(RssCollectionScheduler.class);

    private final CollectionRepository repository;
    private final CollectFeed collect;
    private final Duration interval;
    private final int batchSize;

    public RssCollectionScheduler(
        CollectionRepository repository,
        CollectFeed collect,
        @Value("${app.collection.schedule.interval:30m}") Duration interval,
        @Value("${app.collection.schedule.batch-size:5}") int batchSize
    ) {
        if (interval.isZero() || interval.isNegative() || batchSize < 1 || batchSize > 20) {
            throw new IllegalArgumentException("RSS schedule requires a positive interval and batch size 1–20");
        }
        this.repository = repository;
        this.collect = collect;
        this.interval = interval;
        this.batchSize = batchSize;
    }

    @Scheduled(cron = "${app.collection.schedule.cron:-}", zone = "UTC")
    public void collectDueFeeds() {
        for (var feed : repository.dueFeeds(Instant.now().minus(interval), batchSize)) {
            try {
                collect.collectScheduled(feed.sourceId(), feed.feedId(), interval);
            } catch (CollectionException e) {
                log.debug("Scheduled RSS skipped feedId={} reason={}", feed.feedId(), e.error());
            } catch (RuntimeException e) {
                log.warn("Scheduled RSS failed feedId={}", feed.feedId(), e);
            }
        }
    }
}
