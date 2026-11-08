package com.econnews.collection.adapter.out.persistence;

import com.econnews.collection.domain.model.CollectionRun;
import com.econnews.collection.domain.repository.CollectionRepository;
import java.sql.*;
import java.time.Instant;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcCollectionRepository implements CollectionRepository {

    private final JdbcTemplate jdbc;

    public JdbcCollectionRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private CollectionRun mapRun(ResultSet rs, int row) throws SQLException {
        Timestamp end = rs.getTimestamp("finished_at");
        return new CollectionRun(
            rs.getLong("id"),
            rs.getLong("source_id"),
            rs.getLong("feed_id"),
            rs.getString("feed_url"),
            rs.getLong("requested_by"),
            rs.getString("status"),
            rs.getInt("received_count"),
            rs.getInt("inserted_count"),
            rs.getInt("duplicate_count"),
            rs.getInt("invalid_count"),
            rs.getString("error_code"),
            rs.getTimestamp("started_at").toInstant(),
            end == null ? null : end.toInstant()
        );
    }

    public Optional<FeedTarget> lockFeed(long sourceId, long feedId) {
        return jdbc
            .query(
                """
                SELECT f.source_id,f.id,f.url,s.enabled AS source_enabled,f.enabled AS feed_enabled
                FROM rss_feeds f JOIN news_sources s ON s.id=f.source_id
                WHERE f.source_id=? AND f.id=? FOR UPDATE OF f,s
                """,
                (rs, row) ->
                    new FeedTarget(
                        rs.getLong("source_id"),
                        rs.getLong("id"),
                        rs.getString("url"),
                        rs.getBoolean("source_enabled"),
                        rs.getBoolean("feed_enabled")
                    ),
                sourceId,
                feedId
            )
            .stream()
            .findFirst();
    }

    public Optional<CollectionRun> latest(long feedId) {
        return jdbc
            .query(
                "SELECT * FROM collection_runs WHERE feed_id=? ORDER BY id DESC LIMIT 1",
                this::mapRun,
                feedId
            )
            .stream()
            .findFirst();
    }

    public CollectionRun start(FeedTarget t, long requestedBy) {
        return jdbc.queryForObject(
            "INSERT INTO collection_runs(source_id,feed_id,feed_url,requested_by) VALUES(?,?,?,?) RETURNING *",
            this::mapRun,
            t.sourceId(),
            t.feedId(),
            t.url(),
            requestedBy
        );
    }

    public Optional<CollectionRun> run(long id) {
        return jdbc
            .query("SELECT * FROM collection_runs WHERE id=?", this::mapRun, id)
            .stream()
            .findFirst();
    }

    public Slice<CollectionRun> runs(long sourceId, long feedId, int page, int size) {
        long total = jdbc.queryForObject(
            "SELECT count(*) FROM collection_runs WHERE source_id=? AND feed_id=?",
            Long.class,
            sourceId,
            feedId
        );
        return new Slice<>(
            jdbc.query(
                "SELECT * FROM collection_runs WHERE source_id=? AND feed_id=? ORDER BY id DESC LIMIT ? OFFSET ?",
                this::mapRun,
                sourceId,
                feedId,
                size,
                (long) page * size
            ),
            total
        );
    }

    public CollectionRun finish(
        long id,
        String status,
        int inserted,
        int duplicate,
        int invalid,
        String errorCode
    ) {
        return jdbc.queryForObject(
            """
            UPDATE collection_runs SET status=?,received_count=?,inserted_count=?,duplicate_count=?,invalid_count=?,
            error_code=?,finished_at=GREATEST(CURRENT_TIMESTAMP,started_at) WHERE id=? AND status='RUNNING' RETURNING *
            """,
            this::mapRun,
            status,
            inserted + duplicate + invalid,
            inserted,
            duplicate,
            invalid,
            errorCode,
            id
        );
    }

    public void interruptStale(Instant cutoff) {
        jdbc.update(
            "UPDATE collection_runs SET status='INTERRUPTED',error_code='STALE_RUN',finished_at=GREATEST(CURRENT_TIMESTAMP,started_at) WHERE status='RUNNING' AND started_at<?",
            Timestamp.from(cutoff)
        );
    }
}
