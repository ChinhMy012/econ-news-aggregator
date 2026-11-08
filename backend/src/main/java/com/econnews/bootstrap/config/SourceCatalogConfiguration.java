package com.econnews.bootstrap.config;

import com.econnews.source.application.port.CatalogTransaction;
import com.econnews.source.application.usecase.ManageSourceCatalog;
import com.econnews.source.domain.repository.SourceCatalog;
import java.util.function.Supplier;
import org.springframework.context.annotation.*;
import org.springframework.transaction.*;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
public class SourceCatalogConfiguration {

    @Bean
    public CatalogTransaction catalogTransaction(PlatformTransactionManager manager) {
        TransactionTemplate t = new TransactionTemplate(manager);
        t.setTimeout(15);
        return new CatalogTransaction() {
            public <T> T execute(Supplier<T> work) {
                return t.execute(status -> work.get());
            }
        };
    }

    @Bean
    public ManageSourceCatalog manageSourceCatalog(SourceCatalog c, CatalogTransaction t) {
        return new ManageSourceCatalog(c, t);
    }
}
