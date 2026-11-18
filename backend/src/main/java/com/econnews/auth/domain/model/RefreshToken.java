package com.econnews.auth.domain.model;

import java.time.Instant;
import java.util.UUID;

public record RefreshToken(Long id, Long adminUserId, String tokenHash, UUID familyId,
        Instant createdAt, Instant expiresAt, Instant usedAt, Instant revokedAt) {
    public boolean isExpiredAt(Instant now) { return !expiresAt.isAfter(now); }
    public boolean isUsed() { return usedAt != null; }
    public boolean isRevoked() { return revokedAt != null; }
    @Override public String toString() { return "RefreshToken[REDACTED]"; }
}
