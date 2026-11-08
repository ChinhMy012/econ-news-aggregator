package com.econnews.auth.adapter.in.web.response;

import com.econnews.auth.application.model.LoginResult;

public record LoginResponse(
    String accessToken,
    String tokenType,
    long expiresIn,
    AdminResponse user
) {
    public static LoginResponse from(LoginResult result) {
        var admin = result.admin();
        var token = result.accessToken();
        return new LoginResponse(token.value(), "Bearer", token.expiresIn(),
                new AdminResponse(admin.id().toString(), admin.username(), "ADMIN"));
    }

    @Override
    public String toString() {
        return "LoginResponse[REDACTED]";
    }

    public record AdminResponse(String id, String username, String role) {}
}
