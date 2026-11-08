package com.econnews.article.adapter.out.persistence;

import com.econnews.article.application.port.ArticleQuery;
import com.econnews.article.domain.model.ArticlePreview;
import java.sql.Timestamp;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcArticleQuery implements ArticleQuery {

    private final JdbcTemplate jdbc;

    public JdbcArticleQuery(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Slice pending(int page, int size) {
        long total = jdbc.queryForObject(
            "SELECT count(*) FROM articles WHERE status='PENDING'",
            Long.class
        );
        var items = jdbc.query(
            "SELECT * FROM articles WHERE status='PENDING' ORDER BY id DESC LIMIT ? OFFSET ?",
            (rs, row) -> {
                Timestamp published = rs.getTimestamp("published_at");
                return new ArticlePreview(
                    rs.getLong("id"),
                    rs.getLong("source_id"),
                    rs.getLong("first_feed_id"),
                    rs.getString("title"),
                    rs.getString("original_url"),
                    rs.getString("summary"),
                    published == null ? null : published.toInstant(),
                    rs.getTimestamp("collected_at").toInstant(),
                    rs.getString("status")
                );
            },
            size,
            (long) page * size
        );
        return new Slice(items, total);
    }
}
