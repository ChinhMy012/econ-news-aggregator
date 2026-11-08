package com.econnews.bootstrap.config;

import com.econnews.article.application.NormalizeCollectedArticle;
import com.econnews.article.domain.repository.CollectedArticleRepository;
import com.econnews.collection.adapter.out.rss.*;
import com.econnews.collection.application.port.*;
import com.econnews.collection.application.usecase.CollectFeed;
import com.econnews.collection.domain.repository.CollectionRepository;
import java.time.Duration;
import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
public class RssCollectionConfiguration {

    @Bean(destroyMethod = "close")
    public SafeRssDownloader safeRssDownloader() {
        return new SafeRssDownloader();
    }

    @Bean
    public SecureFeedParser secureFeedParser() {
        return new SecureFeedParser();
    }

    @Bean
    public FeedReader feedReader(SafeRssDownloader downloader, SecureFeedParser parser) {
        return new HttpFeedReader(downloader, parser);
    }

    @Bean
    public CollectionTransaction collectionTransaction(PlatformTransactionManager manager) {
        var template = new TransactionTemplate(manager);
        template.setTimeout(15);
        return new CollectionTransaction() {
            public <T> T execute(Supplier<T> work) {
                return template.execute(status -> work.get());
            }
        };
    }

    @Bean
    public CollectFeed collectFeed(
        CollectionRepository runs,
        CollectedArticleRepository articles,
        CollectionTransaction tx,
        FeedReader reader,
        @Value("${app.collection.cooldown:30s}") Duration cooldown
    ) {
        if (cooldown.isNegative()) throw new IllegalArgumentException(
            "Collection cooldown must not be negative"
        );
        return new CollectFeed(
            runs,
            articles,
            tx,
            reader,
            new NormalizeCollectedArticle(),
            cooldown
        );
    }
}
