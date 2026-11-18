package com.econnews.article.adapter.out.persistence;

import com.econnews.article.application.port.ArticleQuery;
import com.econnews.article.domain.model.*;
import java.sql.*;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcArticleQuery implements ArticleQuery {

    private final JdbcTemplate jdbc;
    private static final String SELECT =
        "SELECT a.*,s.name AS source_name,s.website_url AS source_website_url FROM articles a JOIN news_sources s ON s.id=a.source_id";

    public JdbcArticleQuery(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private ArticleDetails mapArticle(ResultSet rs, int row) throws SQLException {
        Timestamp published = rs.getTimestamp("published_at");
        String type = rs.getString("content_type");
        return new ArticleDetails(
            rs.getLong("id"),
            rs.getLong("source_id"),
            rs.getString("source_name"),
            rs.getString("source_website_url"),
            rs.getLong("first_feed_id"),
            rs.getString("title"),
            rs.getString("original_url"),
            rs.getString("summary"),
            published == null ? null : published.toInstant(),
            rs.getTimestamp("collected_at").toInstant(),
            ArticleStatus.valueOf(rs.getString("status")),
            type == null ? null : ContentType.valueOf(type),
            rs.getString("moderation_reason"),
            rs.getLong("version"),
            rs.getTimestamp("updated_at").toInstant()
        );
    }

    public Slice list(ArticleStatus status, ContentType contentType, int page, int size) {
        String where = " WHERE 1=1";
        List<Object> args = new ArrayList<>();
        if (status != null) {
            where += " AND a.status=?";
            args.add(status.name());
        }
        if (contentType != null) {
            where += " AND a.content_type=?";
            args.add(contentType.name());
        }
        long total = jdbc.queryForObject(
            "SELECT count(*) FROM articles a" + where,
            Long.class,
            args.toArray()
        );
        args.add(size);
        args.add((long) page * size);
        return new Slice(
            jdbc.query(
                SELECT + where + " ORDER BY a.id DESC LIMIT ? OFFSET ?",
                this::mapArticle,
                args.toArray()
            ),
            total
        );
    }

    public Optional<ArticleDetails> find(long id) {
        return jdbc.query(SELECT + " WHERE a.id=?", this::mapArticle, id).stream().findFirst();
    }
}
