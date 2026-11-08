package com.econnews.auth.adapter.out.persistence;

import java.util.function.Supplier;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import com.econnews.auth.application.port.out.AuthTransaction;

public final class SpringAuthTransaction implements AuthTransaction {
    private final TransactionTemplate template;
    public SpringAuthTransaction(PlatformTransactionManager manager) {
        template = new TransactionTemplate(manager);
        template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        template.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        template.setTimeout(15);
    }
    @Override public <T> T execute(Supplier<T> operation) {
        return template.execute(status -> operation.get());
    }
}
