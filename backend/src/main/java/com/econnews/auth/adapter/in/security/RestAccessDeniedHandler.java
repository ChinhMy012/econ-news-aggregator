package com.econnews.auth.adapter.in.security;

import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.csrf.CsrfException;
import org.springframework.stereotype.Component;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {
    private final SecurityErrorWriter writer;
    public RestAccessDeniedHandler(SecurityErrorWriter writer) { this.writer = writer; }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
            AccessDeniedException exception) throws IOException {
        boolean csrf = exception instanceof CsrfException;
        if (!csrf) response.setHeader("WWW-Authenticate", "Bearer error=\"insufficient_scope\"");
        writer.write(request, response, HttpStatus.FORBIDDEN,
                csrf ? "CSRF_INVALID" : "FORBIDDEN",
                csrf ? "Thông tin bảo vệ CSRF thiếu hoặc không hợp lệ."
                     : "Bạn không được phép thực hiện yêu cầu.");
    }
}
