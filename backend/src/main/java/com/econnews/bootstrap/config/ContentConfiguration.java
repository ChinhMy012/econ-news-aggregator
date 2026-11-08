package com.econnews.bootstrap.config;

import com.econnews.document.application.ManageDocuments;
import com.econnews.document.domain.repository.DocumentRepository;
import com.econnews.event.application.ManageEvents;
import com.econnews.event.domain.repository.EventRepository;
import com.econnews.shared.application.port.*;
import java.util.function.Supplier;
import org.springframework.context.annotation.*;
import org.springframework.transaction.*;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
public class ContentConfiguration {

    @Bean
    public ContentTransaction contentTransaction(PlatformTransactionManager manager) {
        var write = new TransactionTemplate(manager);
        write.setTimeout(15);
        var read = new TransactionTemplate(manager);
        read.setReadOnly(true);
        read.setIsolationLevel(TransactionDefinition.ISOLATION_REPEATABLE_READ);
        read.setTimeout(15);
        return new ContentTransaction() {
            public <T> T write(Supplier<T> work) {
                return write.execute(status -> work.get());
            }

            public <T> T read(Supplier<T> work) {
                return read.execute(status -> work.get());
            }
        };
    }

    @Bean
    public ManageEvents manageEvents(
        EventRepository repo,
        ContentTransaction tx,
        PublicationReferences refs
    ) {
        return new ManageEvents(repo, tx, refs);
    }

    @Bean
    public ManageDocuments manageDocuments(
        DocumentRepository repo,
        ContentTransaction tx,
        PublicationReferences refs
    ) {
        return new ManageDocuments(repo, tx, refs);
    }
}
