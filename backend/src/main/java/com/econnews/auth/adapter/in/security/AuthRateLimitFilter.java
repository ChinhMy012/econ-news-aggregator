package com.econnews.auth.adapter.in.security;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;
import com.econnews.bootstrap.config.AuthRateLimitProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/** Giới hạn trong bộ nhớ cho một backend; không tin X-Forwarded-For do client gửi. */
public final class AuthRateLimitFilter extends OncePerRequestFilter {
    private final Map<String, Window> clients = new HashMap<>();
    private final AuthRateLimitProperties properties;
    private final SecurityErrorWriter writer;
    private long lastCleanup = System.nanoTime();
    public AuthRateLimitFilter(AuthRateLimitProperties properties, SecurityErrorWriter writer) {
        this.properties=properties; this.writer=writer;
    }
    @Override protected boolean shouldNotFilter(HttpServletRequest request) {
        return !"POST".equals(request.getMethod()) ||
                !(request.getServletPath().equals("/api/v1/auth/login")
                || request.getServletPath().equals("/api/v1/auth/refresh"));
    }
    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {
        boolean login = request.getServletPath().endsWith("/login");
        long retry = admit(request.getRemoteAddr()+request.getServletPath(),
                login ? properties.loginRequests() : properties.refreshRequests());
        if (retry > 0) {
            response.setHeader("Retry-After", Long.toString(retry));
            writer.write(request, response, HttpStatus.TOO_MANY_REQUESTS,
                    login ? "LOGIN_RATE_LIMITED" : "REFRESH_RATE_LIMITED",
                    "Bạn gửi quá nhiều yêu cầu. Vui lòng thử lại sau.");
            return;
        }
        chain.doFilter(request, response);
    }
    private synchronized long admit(String key, int limit) {
        long now = System.nanoTime();
        long duration = properties.window().toNanos();
        if (now-lastCleanup >= duration) {
            clients.entrySet().removeIf(entry -> now-entry.getValue().started >= duration);
            lastCleanup=now;
        }
        Window window = clients.get(key);
        if (window == null || now-window.started >= duration) {
            if (window == null && clients.size() >= properties.maxClients()) {
                return Math.max(1, properties.window().toSeconds());
            }
            clients.put(key, new Window(now, 1));
            return 0;
        }
        if (window.count >= limit) return Math.max(1, (duration-(now-window.started)+999999999L)/1000000000L);
        window.count++;
        return 0;
    }
    private static class Window {
        final long started;
        int count;
        Window(long started, int count) { this.started=started; this.count=count; }
    }
}
