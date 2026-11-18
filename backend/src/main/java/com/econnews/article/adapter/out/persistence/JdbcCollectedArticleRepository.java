package com.econnews.article.adapter.out.persistence;

import com.econnews.article.domain.model.CollectedArticle;
import com.econnews.article.domain.repository.CollectedArticleRepository;
import java.sql.Timestamp;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcCollectedArticleRepository implements CollectedArticleRepository {

    private final JdbcTemplate jdbc;

    public JdbcCollectedArticleRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public boolean insertIfNew(long sourceId, long feedId, CollectedArticle a) {
        return (
            jdbc.update(
                """
                INSERT INTO articles(source_id,first_feed_id,title,original_url,normalized_url,url_hash,summary,published_at)
                VALUES(?,?,?,?,?,?,?,?) ON CONFLICT(source_id,url_hash) DO NOTHING
                """,
                sourceId,
                feedId,
                a.title(),
                a.originalUrl(),
                a.normalizedUrl(),
                a.urlHash(),
                a.summary(),
                a.publishedAt() == null ? null : Timestamp.from(a.publishedAt())
            ) ==
            1
        );
    }
}
