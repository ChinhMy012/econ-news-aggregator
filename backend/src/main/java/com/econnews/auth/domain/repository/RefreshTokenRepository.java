package com.econnews.auth.domain.repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import com.econnews.auth.domain.model.RefreshToken;

public interface RefreshTokenRepository {
    Optional<RefreshToken> findByHash(String hash);
    boolean lockFamily(UUID familyId);
    void insert(Long userId, String hash, UUID familyId, Instant createdAt, Instant expiresAt);
    void markUsed(Long tokenId, Instant now);
    void revokeFamily(UUID familyId, Instant now);
    int deleteFamiliesExpiredBefore(Instant cutoff);
}
