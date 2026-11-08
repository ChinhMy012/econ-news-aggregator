package com.econnews.auth.adapter.out.persistence;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import com.econnews.auth.domain.model.RefreshToken;
import com.econnews.auth.domain.repository.RefreshTokenRepository;

@Repository
public class RefreshTokenPersistenceAdapter implements RefreshTokenRepository {
    private final JdbcTemplate jdbc;
    public RefreshTokenPersistenceAdapter(JdbcTemplate jdbc) { this.jdbc = jdbc; }
    @Override public Optional<RefreshToken> findByHash(String hash) {
        return jdbc.query("SELECT * FROM refresh_tokens WHERE token_hash = ?",
                (rs, row) -> new RefreshToken(rs.getLong("id"), rs.getLong("admin_user_id"),
                    rs.getString("token_hash"), rs.getObject("family_id", UUID.class),
                    time(rs, "created_at"), time(rs, "expires_at"),
                    time(rs, "used_at"), time(rs, "revoked_at")), hash).stream().findFirst();
    }
    @Override public boolean lockFamily(UUID familyId) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            throw new IllegalStateException("Khóa phiên phải nằm trong transaction.");
        }
        // Token đầu tiên được giữ đến khi cả phiên đủ điều kiện dọn dữ liệu.
        return !jdbc.queryForList("""
                SELECT id FROM refresh_tokens WHERE family_id = ?
                ORDER BY id LIMIT 1 FOR UPDATE
                """, Long.class, familyId).isEmpty();
    }
    @Override public void insert(Long userId, String hash, UUID familyId,
            Instant createdAt, Instant expiresAt) {
        jdbc.update("""
                INSERT INTO refresh_tokens
                (admin_user_id, token_hash, family_id, created_at, expires_at)
                VALUES (?, ?, ?, ?, ?)
                """, userId, hash, familyId, Timestamp.from(createdAt), Timestamp.from(expiresAt));
    }
    @Override public void markUsed(Long id, Instant now) {
        int changed = jdbc.update("""
                UPDATE refresh_tokens SET used_at = ?
                WHERE id = ? AND used_at IS NULL AND revoked_at IS NULL
                """, Timestamp.from(now), id);
        if (changed != 1) throw new IllegalStateException("Không thể tiêu thụ refresh token.");
    }
    @Override public void revokeFamily(UUID familyId, Instant now) {
        jdbc.update("""
                UPDATE refresh_tokens SET revoked_at = GREATEST(?, created_at)
                WHERE family_id = ? AND revoked_at IS NULL
                """, Timestamp.from(now), familyId);
    }
    @Override public int deleteFamiliesExpiredBefore(Instant cutoff) {
        return jdbc.update("""
                DELETE FROM refresh_tokens WHERE family_id IN (
                    SELECT family_id FROM refresh_tokens
                    GROUP BY family_id HAVING MAX(expires_at) < ?
                )
                """, Timestamp.from(cutoff));
    }
    private static Instant time(ResultSet rs, String name) throws SQLException {
        Timestamp timestamp = rs.getTimestamp(name);
        return timestamp == null ? null : timestamp.toInstant();
    }
}
