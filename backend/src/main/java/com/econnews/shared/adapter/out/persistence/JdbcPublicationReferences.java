package com.econnews.shared.adapter.out.persistence;

import com.econnews.shared.application.port.PublicationReferences;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Repository
public class JdbcPublicationReferences implements PublicationReferences {

    private final JdbcTemplate jdbc;

    public JdbcPublicationReferences(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void lock() {
        if (
            !TransactionSynchronizationManager.isActualTransactionActive()
        ) throw new IllegalStateException("Publication lock requires a transaction");
        jdbc.queryForList("SELECT pg_advisory_xact_lock(87529041)");
    }

    public boolean articleIsEssential(long id) {
        return essential("article", id);
    }

    public boolean documentIsEssential(long id) {
        return essential("document", id);
    }

    private boolean essential(String kind, long id) {
        // Identifiers are internal constants, never values supplied by a request.
        String suffix = kind.equals("article") ? "articles" : "documents";
        String idColumn = kind + "_id";
        String excludeArticle = kind.equals("article") ? " AND a.id<>?" : "";
        String excludeDocument = kind.equals("document") ? " AND d.id<>?" : "";
        String sql = """
            SELECT EXISTS (
                SELECT 1 FROM event_overview_%s used JOIN events e ON e.id=used.event_id
                WHERE used.%s=? AND e.status='PUBLISHED'
                AND NOT EXISTS (SELECT 1 FROM event_overview_articles x JOIN articles a ON a.id=x.article_id
                    WHERE x.event_id=e.id AND a.status='PUBLISHED'%s)
                AND NOT EXISTS (SELECT 1 FROM event_overview_documents x JOIN documents d ON d.id=x.document_id
                    WHERE x.event_id=e.id AND d.enabled%s)
                UNION ALL
                SELECT 1 FROM milestone_%s used JOIN milestones m ON m.id=used.milestone_id
                WHERE used.%s=? AND m.status='PUBLISHED'
                AND NOT EXISTS (SELECT 1 FROM milestone_articles x JOIN articles a ON a.id=x.article_id
                    WHERE x.milestone_id=m.id AND a.status='PUBLISHED'%s)
                AND NOT EXISTS (SELECT 1 FROM milestone_documents x JOIN documents d ON d.id=x.document_id
                    WHERE x.milestone_id=m.id AND d.enabled%s)
            )
            """.formatted(
                suffix,
                idColumn,
                excludeArticle,
                excludeDocument,
                suffix,
                idColumn,
                excludeArticle,
                excludeDocument
            );
        return Boolean.TRUE.equals(jdbc.queryForObject(sql, Boolean.class, id, id, id, id));
    }
}
