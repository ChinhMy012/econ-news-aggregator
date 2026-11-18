package com.econnews.auth.adapter.in.web.response;

import com.econnews.auth.application.model.AuthenticatedAdmin;

public record CurrentAdminResponse(String id, String username, String role) {
    public static CurrentAdminResponse from(AuthenticatedAdmin admin) {
        return new CurrentAdminResponse(admin.id().toString(), admin.username(), "ADMIN");
    }
}
