package com.econnews.event.adapter.out.persistence;

import com.econnews.event.domain.model.*;
import com.econnews.event.domain.repository.EventRepository;
import java.sql.*;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcEventRepository implements EventRepository {

    private final JdbcTemplate jdbc;

    public JdbcEventRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private EconomicEvent mapEvent(ResultSet rs, int row) throws SQLException {
        return new EconomicEvent(
            rs.getLong("id"),
            rs.getString("title"),
            rs.getString("content_type"),
            rs.getString("overview"),
            rs.getString("status"),
            rs.getString("hidden_reason"),
            rs.getLong("version"),
            rs.getTimestamp("created_at").toInstant(),
            rs.getTimestamp("updated_at").toInstant()
        );
    }

    private Milestone mapMilestone(ResultSet rs, int row) throws SQLException {
        return new Milestone(
            rs.getLong("id"),
            rs.getLong("event_id"),
            rs.getString("title"),
            rs.getString("description"),
            rs.getString("date_precision"),
            rs.getInt("date_year"),
            rs.getObject("date_month", Integer.class),
            rs.getObject("date_day", Integer.class),
            rs.getString("status")
        );
    }

    public Slice list(String status, String contentType, int page, int size) {
        String where = status == null ? "" : " WHERE status=?";
        List<Object> args = new ArrayList<>();
        if (status != null) args.add(status);
        if (contentType != null) {
            where += status == null ? " WHERE content_type=?" : " AND content_type=?";
            args.add(contentType);
        }
        long total = jdbc.queryForObject(
            "SELECT count(*) FROM events" + where,
            Long.class,
            args.toArray()
        );
        args.add(size);
        args.add((long) page * size);
        return new Slice(
            jdbc.query(
                "SELECT * FROM events" + where + " ORDER BY id DESC LIMIT ? OFFSET ?",
                this::mapEvent,
                args.toArray()
            ),
            total
        );
    }

    public Optional<EconomicEvent> find(long id) {
        return jdbc
            .query("SELECT * FROM events WHERE id=?", this::mapEvent, id)
            .stream()
            .findFirst();
    }

    public EconomicEvent create(String title, String type, String overview) {
        return jdbc.queryForObject(
            "INSERT INTO events(title,content_type,overview) VALUES(?,?,?) RETURNING *",
            this::mapEvent,
            title,
            type,
            overview
        );
    }

    public void edit(long id, String title, String type, String overview) {
        jdbc.update(
            "UPDATE events SET title=?,content_type=?,overview=? WHERE id=?",
            title,
            type,
            overview,
            id
        );
    }

    public boolean bump(long id, long version) {
        return (
            jdbc.update(
                "UPDATE events SET version=version+1,updated_at=CURRENT_TIMESTAMP WHERE id=? AND version=?",
                id,
                version
            ) ==
            1
        );
    }

    public void status(long id, String status, String reason) {
        jdbc.update("UPDATE events SET status=?,hidden_reason=? WHERE id=?", status, reason, id);
    }

    public List<Milestone> milestones(long eventId) {
        return jdbc.query(
            "SELECT * FROM milestones WHERE event_id=? ORDER BY date_year,COALESCE(date_month,1),COALESCE(date_day,1),CASE date_precision WHEN 'YEAR' THEN 0 WHEN 'MONTH' THEN 1 ELSE 2 END,id",
            this::mapMilestone,
            eventId
        );
    }

    public Optional<Milestone> milestone(long eventId, long id) {
        return jdbc
            .query(
                "SELECT * FROM milestones WHERE event_id=? AND id=?",
                this::mapMilestone,
                eventId,
                id
            )
            .stream()
            .findFirst();
    }

    public long createMilestone(
        long eventId,
        String title,
        String description,
        String precision,
        int year,
        Integer month,
        Integer day
    ) {
        return jdbc.queryForObject(
            "INSERT INTO milestones(event_id,title,description,date_precision,date_year,date_month,date_day) VALUES(?,?,?,?,?,?,?) RETURNING id",
            Long.class,
            eventId,
            title,
            description,
            precision,
            year,
            month,
            day
        );
    }

    public void editMilestone(
        long id,
        String title,
        String description,
        String precision,
        int year,
        Integer month,
        Integer day
    ) {
        jdbc.update(
            "UPDATE milestones SET title=?,description=?,date_precision=?,date_year=?,date_month=?,date_day=?,updated_at=CURRENT_TIMESTAMP WHERE id=?",
            title,
            description,
            precision,
            year,
            month,
            day,
            id
        );
    }

    public void milestoneStatus(long id, String status) {
        jdbc.update(
            "UPDATE milestones SET status=?,updated_at=CURRENT_TIMESTAMP WHERE id=?",
            status,
            id
        );
    }

    public boolean usableArticle(long id) {
        return Boolean.TRUE.equals(
            jdbc.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM articles WHERE id=? AND status='PUBLISHED')",
                Boolean.class,
                id
            )
        );
    }

    public boolean usableDocument(long id) {
        return Boolean.TRUE.equals(
            jdbc.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM documents WHERE id=? AND enabled)",
                Boolean.class,
                id
            )
        );
    }

    public Map<Long, List<Citation>> milestoneCitations(long eventId) {
        Map<Long, List<Citation>> byMilestone = new HashMap<>();
        jdbc.query(
            """
            SELECT x.milestone_id, a.id, 'ARTICLE' AS kind, a.title, a.original_url AS url,
                   s.name AS publisher, a.status = 'PUBLISHED' AS usable, 0 AS sort_kind
            FROM milestone_articles x
            JOIN articles a ON a.id = x.article_id
            JOIN news_sources s ON s.id = a.source_id
            WHERE x.event_id = ?
            UNION ALL
            SELECT x.milestone_id, d.id, 'DOCUMENT', d.title, d.url, d.issuer, d.enabled, 1
            FROM milestone_documents x
            JOIN documents d ON d.id = x.document_id
            WHERE x.event_id = ?
            ORDER BY milestone_id, sort_kind, id
            """,
            rs -> {
                Citation citation = new Citation(
                    rs.getLong("id"), rs.getString("kind"), rs.getString("title"),
                    rs.getString("url"), rs.getString("publisher"), rs.getBoolean("usable")
                );
                byMilestone.computeIfAbsent(rs.getLong("milestone_id"), id -> new ArrayList<>())
                    .add(citation);
            },
            eventId, eventId
        );
        byMilestone.replaceAll((id, sources) -> List.copyOf(sources));
        return byMilestone;
    }

    public List<Citation> citations(long eventId, Long milestoneId) {
        String articleTable = milestoneId == null
            ? "event_overview_articles"
            : "milestone_articles";
        String documentTable = milestoneId == null
            ? "event_overview_documents"
            : "milestone_documents";
        String filter = milestoneId == null ? "x.event_id=?" : "x.event_id=? AND x.milestone_id=?";
        Object[] args = milestoneId == null
            ? new Object[] { eventId }
            : new Object[] { eventId, milestoneId };
        List<Citation> result = new ArrayList<>(
            jdbc.query(
                "SELECT a.id,a.title,a.original_url,s.name,a.status FROM " +
                    articleTable +
                    " x JOIN articles a ON a.id=x.article_id JOIN news_sources s ON s.id=a.source_id WHERE " +
                    filter +
                    " ORDER BY a.id",
                (rs, row) ->
                    new Citation(
                        rs.getLong(1),
                        "ARTICLE",
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4),
                        rs.getString(5).equals("PUBLISHED")
                    ),
                args
            )
        );
        result.addAll(
            jdbc.query(
                "SELECT d.id,d.title,d.url,d.issuer,d.enabled FROM " +
                    documentTable +
                    " x JOIN documents d ON d.id=x.document_id WHERE " +
                    filter +
                    " ORDER BY d.id",
                (rs, row) ->
                    new Citation(
                        rs.getLong(1),
                        "DOCUMENT",
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4),
                        rs.getBoolean(5)
                    ),
                args
            )
        );
        return List.copyOf(result);
    }

    public void replaceCitations(
        long eventId,
        Long milestoneId,
        List<Long> articleIds,
        List<Long> documentIds
    ) {
        for (long id : articleIds)
            jdbc.update(
                "INSERT INTO event_articles(event_id,article_id) VALUES(?,?) ON CONFLICT DO NOTHING",
                eventId,
                id
            );
        for (long id : documentIds)
            jdbc.update(
                "INSERT INTO event_documents(event_id,document_id) VALUES(?,?) ON CONFLICT DO NOTHING",
                eventId,
                id
            );
        if (milestoneId == null) {
            jdbc.update("DELETE FROM event_overview_articles WHERE event_id=?", eventId);
            jdbc.update("DELETE FROM event_overview_documents WHERE event_id=?", eventId);
            for (long id : articleIds)
                jdbc.update(
                    "INSERT INTO event_overview_articles(event_id,article_id) VALUES(?,?)",
                    eventId,
                    id
                );
            for (long id : documentIds)
                jdbc.update(
                    "INSERT INTO event_overview_documents(event_id,document_id) VALUES(?,?)",
                    eventId,
                    id
                );
        } else {
            jdbc.update(
                "DELETE FROM milestone_articles WHERE event_id=? AND milestone_id=?",
                eventId,
                milestoneId
            );
            jdbc.update(
                "DELETE FROM milestone_documents WHERE event_id=? AND milestone_id=?",
                eventId,
                milestoneId
            );
            for (long id : articleIds)
                jdbc.update(
                    "INSERT INTO milestone_articles(event_id,milestone_id,article_id) VALUES(?,?,?)",
                    eventId,
                    milestoneId,
                    id
                );
            for (long id : documentIds)
                jdbc.update(
                    "INSERT INTO milestone_documents(event_id,milestone_id,document_id) VALUES(?,?,?)",
                    eventId,
                    milestoneId,
                    id
                );
        }
    }
}
