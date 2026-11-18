package com.econnews.auth.adapter.in.security;

import java.io.IOException;
import com.econnews.auth.adapter.in.web.response.AuthErrorResponses;
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
        var failure = AuthErrorResponses.AUTHENTICATION_REQUIRED;
        if (request.getHeader("Authorization") != null) {
            failure = AuthErrorResponses.INVALID_ACCESS_TOKEN;
        }
        Throwable cause = exception;
        for (int depth = 0; cause != null && depth < 10; depth++, cause = cause.getCause()) {
            if (cause instanceof JwtValidationException validation
                    && validation.getErrors().stream().anyMatch(
                        error -> "access_token_expired".equals(error.getErrorCode()))) {
                failure = AuthErrorResponses.ACCESS_TOKEN_EXPIRED;
                break;
            }
        }
        response.setHeader("WWW-Authenticate", failure == AuthErrorResponses.AUTHENTICATION_REQUIRED
                ? "Bearer" : "Bearer error=\"invalid_token\"");
        writer.write(request, response, failure.status(), failure.code(), failure.message());
    }
}
