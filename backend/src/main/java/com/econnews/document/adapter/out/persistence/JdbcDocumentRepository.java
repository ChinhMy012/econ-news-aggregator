package com.econnews.document.adapter.out.persistence;

import com.econnews.document.domain.model.ReferenceDocument;
import com.econnews.document.domain.repository.DocumentRepository;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcDocumentRepository implements DocumentRepository {

    private final JdbcTemplate jdbc;

    public JdbcDocumentRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private ReferenceDocument mapDocument(ResultSet rs, int row) throws SQLException {
        return new ReferenceDocument(
            rs.getLong("id"),
            rs.getString("title"),
            rs.getString("issuer"),
            rs.getString("url"),
            rs.getObject("issued_on", LocalDate.class),
            rs.getBoolean("enabled"),
            rs.getLong("version"),
            rs.getTimestamp("created_at").toInstant(),
            rs.getTimestamp("updated_at").toInstant()
        );
    }

    public Slice list(int page, int size) {
        long total = jdbc.queryForObject("SELECT count(*) FROM documents", Long.class);
        return new Slice(
            jdbc.query(
                "SELECT * FROM documents ORDER BY id DESC LIMIT ? OFFSET ?",
                this::mapDocument,
                size,
                (long) page * size
            ),
            total
        );
    }

    public Optional<ReferenceDocument> find(long id) {
        return jdbc
            .query("SELECT * FROM documents WHERE id=?", this::mapDocument, id)
            .stream()
            .findFirst();
    }

    public ReferenceDocument create(
        String title,
        String issuer,
        String url,
        LocalDate issuedOn,
        boolean enabled
    ) {
        return jdbc.queryForObject(
            "INSERT INTO documents(title,issuer,url,issued_on,enabled) VALUES(?,?,?,?,?) RETURNING *",
            this::mapDocument,
            title,
            issuer,
            url,
            issuedOn,
            enabled
        );
    }

    public boolean update(
        long id,
        long version,
        String title,
        String issuer,
        LocalDate issuedOn,
        boolean enabled
    ) {
        return (
            jdbc.update(
                "UPDATE documents SET title=?,issuer=?,issued_on=?,enabled=?,version=version+1,updated_at=CURRENT_TIMESTAMP WHERE id=? AND version=?",
                title,
                issuer,
                issuedOn,
                enabled,
                id,
                version
            ) ==
            1
        );
    }
}
