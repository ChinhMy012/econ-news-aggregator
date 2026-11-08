package com.econnews.auth.application.model;

public record LoginResult(AuthenticatedAdmin admin, IssuedAccessToken accessToken) {
    @Override
    public String toString() {
        return "LoginResult[REDACTED]";
    }
}
