package com.econnews.bootstrap.config;

import com.econnews.article.application.ManageArticles;
import com.econnews.article.application.port.*;
import com.econnews.article.domain.repository.ArticleModerationRepository;
import com.econnews.shared.application.port.PublicationReferences;
import java.util.function.Supplier;
import org.springframework.context.annotation.*;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
public class ArticleConfiguration {

    @Bean
    public ArticleTransaction articleTransaction(PlatformTransactionManager manager) {
        var template = new TransactionTemplate(manager);
        template.setTimeout(15);
        return new ArticleTransaction() {
            public <T> T execute(Supplier<T> work) {
                return template.execute(status -> work.get());
            }
        };
    }

    @Bean
    public ManageArticles manageArticles(
        ArticleQuery query,
        ArticleModerationRepository moderation,
        ArticleTransaction tx,
        PublicationReferences references
    ) {
        return new ManageArticles(query, moderation, tx, references);
    }
}
