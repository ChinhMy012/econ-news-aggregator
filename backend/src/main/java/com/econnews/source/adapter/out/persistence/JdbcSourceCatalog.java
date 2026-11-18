package com.econnews.source.adapter.out.persistence;

import com.econnews.source.domain.model.*;
import com.econnews.source.domain.repository.SourceCatalog;
import java.sql.*;
import java.util.*;
import org.springframework.jdbc.core.*;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcSourceCatalog implements SourceCatalog {

    private final JdbcTemplate jdbc;

    public JdbcSourceCatalog(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private NewsSource mapSource(ResultSet r, int row) throws SQLException {
        return new NewsSource(
            r.getLong("id"),
            r.getString("code"),
            r.getString("name"),
            r.getString("website_url"),
            r.getBoolean("enabled"),
            r.getLong("version"),
            r.getTimestamp("created_at").toInstant(),
            r.getTimestamp("updated_at").toInstant()
        );
    }

    private RssFeed mapFeed(ResultSet r, int row) throws SQLException {
        return new RssFeed(
            r.getLong("id"),
            r.getLong("source_id"),
            r.getString("name"),
            r.getString("url"),
            r.getBoolean("enabled"),
            r.getLong("version"),
            r.getTimestamp("created_at").toInstant(),
            r.getTimestamp("updated_at").toInstant()
        );
    }

    private <T> Optional<T> one(String sql, RowMapper<T> mapper, Object... args) {
        return jdbc.query(sql, mapper, args).stream().findFirst();
    }

    public Slice<NewsSource> sources(int page, int size) {
        long total = jdbc.queryForObject("SELECT count(*) FROM news_sources", Long.class);
        return new Slice<>(
            jdbc.query(
                "SELECT * FROM news_sources ORDER BY id DESC LIMIT ? OFFSET ?",
                this::mapSource,
                size,
                (long) page * size
            ),
            total
        );
    }

    public Slice<RssFeed> feeds(long sid, int page, int size) {
        long total = jdbc.queryForObject(
            "SELECT count(*) FROM rss_feeds WHERE source_id=?",
            Long.class,
            sid
        );
        return new Slice<>(
            jdbc.query(
                "SELECT * FROM rss_feeds WHERE source_id=? ORDER BY id DESC LIMIT ? OFFSET ?",
                this::mapFeed,
                sid,
                size,
                (long) page * size
            ),
            total
        );
    }

    public Optional<NewsSource> source(long id) {
        return one("SELECT * FROM news_sources WHERE id=?", this::mapSource, id);
    }

    public Optional<RssFeed> feed(long sid, long id) {
        return one("SELECT * FROM rss_feeds WHERE source_id=? AND id=?", this::mapFeed, sid, id);
    }

    public NewsSource createSource(String code, String name, String url, boolean enabled) {
        return jdbc.queryForObject(
            "INSERT INTO news_sources(code,name,website_url,enabled) VALUES (?,?,?,?) RETURNING *",
            this::mapSource,
            code,
            name,
            url,
            enabled
        );
    }

    public Optional<NewsSource> updateSource(
        long id,
        String name,
        String url,
        boolean enabled,
        long version
    ) {
        return one(
            "UPDATE news_sources SET name=?,website_url=?,enabled=?,version=version+1,updated_at=CURRENT_TIMESTAMP WHERE id=? AND version=? RETURNING *",
            this::mapSource,
            name,
            url,
            enabled,
            id,
            version
        );
    }

    public RssFeed createFeed(long sid, String name, String url, boolean enabled) {
        return jdbc.queryForObject(
            "INSERT INTO rss_feeds(source_id,name,url,enabled) VALUES (?,?,?,?) RETURNING *",
            this::mapFeed,
            sid,
            name,
            url,
            enabled
        );
    }

    public Optional<RssFeed> updateFeed(
        long sid,
        long id,
        String name,
        String url,
        boolean enabled,
        long version
    ) {
        return one(
            "UPDATE rss_feeds SET name=?,url=?,enabled=?,version=version+1,updated_at=CURRENT_TIMESTAMP WHERE source_id=? AND id=? AND version=? RETURNING *",
            this::mapFeed,
            name,
            url,
            enabled,
            sid,
            id,
            version
        );
    }
}
