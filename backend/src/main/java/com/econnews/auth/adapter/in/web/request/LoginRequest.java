package com.econnews.auth.adapter.in.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
    @NotBlank(message = "Tên đăng nhập không được để trống.")
    @Size(max = 100, message = "Tên đăng nhập không được vượt quá 100 ký tự.")
    String username,
    @NotBlank(message = "Mật khẩu không được để trống.")
    @Size(max = 72, message = "Mật khẩu không được vượt quá 72 ký tự.")
    String password
) {
    @Override
    public String toString() {
        return "LoginRequest[REDACTED]";
    }
}
