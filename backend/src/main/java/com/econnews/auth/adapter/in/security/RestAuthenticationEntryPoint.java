package com.econnews.auth.adapter.in.security;

import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final SecurityErrorWriter writer;
    public RestAuthenticationEntryPoint(SecurityErrorWriter writer) { this.writer = writer; }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException exception) throws IOException {
        String code = "AUTHENTICATION_REQUIRED";
        String message = "Bạn cần đăng nhập để thực hiện yêu cầu.";
        if (request.getHeader("Authorization") != null) {
            code = "INVALID_ACCESS_TOKEN";
            message = "Access token không hợp lệ.";
        }
        Throwable cause = exception;
        for (int depth = 0; cause != null && depth < 10; depth++, cause = cause.getCause()) {
            if (cause instanceof JwtValidationException validation
                    && validation.getErrors().stream().anyMatch(
                        error -> "access_token_expired".equals(error.getErrorCode()))) {
                code = "ACCESS_TOKEN_EXPIRED";
                message = "Access token đã hết hạn.";
                break;
            }
        }
        response.setHeader("WWW-Authenticate", "AUTHENTICATION_REQUIRED".equals(code)
                ? "Bearer" : "Bearer error=\"invalid_token\"");
        writer.write(request, response, HttpStatus.UNAUTHORIZED, code, message);
    }
}
