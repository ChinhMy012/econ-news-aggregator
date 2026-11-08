package com.econnews.auth.application.model;

import java.time.Instant;

public record LoginResult(AuthenticatedAdmin admin, IssuedAccessToken accessToken,
        String refreshToken, Instant refreshExpiresAt) {
    @Override public String toString() { return "LoginResult[REDACTED]"; }
}
