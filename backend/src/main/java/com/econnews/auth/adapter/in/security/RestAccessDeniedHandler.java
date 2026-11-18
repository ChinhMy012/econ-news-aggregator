package com.econnews.auth.adapter.in.security;

import java.io.IOException;
import com.econnews.auth.adapter.in.web.response.AuthErrorResponses;
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
        var failure = csrf ? AuthErrorResponses.CSRF_INVALID : AuthErrorResponses.FORBIDDEN;
        writer.write(request, response, failure.status(), failure.code(), failure.message());
    }
}
