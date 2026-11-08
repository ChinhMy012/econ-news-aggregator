package com.econnews.article.adapter.out.persistence;

import com.econnews.article.domain.model.*;
import com.econnews.article.domain.repository.ArticleModerationRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcArticleModerationRepository implements ArticleModerationRepository {

    private final JdbcTemplate jdbc;

    public JdbcArticleModerationRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public boolean update(
        long id,
        long expectedVersion,
        ArticleStatus expectedStatus,
        ArticleStatus status,
        ContentType type,
        String reason
    ) {
        return (
            jdbc.update(
                """
                UPDATE articles SET status=?,content_type=?,moderation_reason=?,version=version+1,updated_at=CURRENT_TIMESTAMP
                WHERE id=? AND version=? AND status=?
                """,
                status.name(),
                type == null ? null : type.name(),
                reason,
                id,
                expectedVersion,
                expectedStatus.name()
            ) ==
            1
        );
    }
}
