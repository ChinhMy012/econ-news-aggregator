package com.econnews.auth.domain.model;

import java.util.Objects;

public final class AdminUser {

    private final Long id;
    private final String username;
    private final String passwordHash;
    private final boolean enabled;

    public AdminUser(
            Long id,
            String username,
            String passwordHash,
            boolean enabled) {

        if (id == null || id <= 0) {
            throw new IllegalArgumentException(
                    "Mã tài khoản phải là số dương.");
        }

        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException(
                    "Tên đăng nhập không được để trống.");
        }

        if (passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalArgumentException(
                    "Mật khẩu đã băm không được để trống.");
        }

        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.enabled = enabled;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof AdminUser other)) {
            return false;
        }

        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}