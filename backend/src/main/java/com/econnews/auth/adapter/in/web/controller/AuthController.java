package com.econnews.auth.adapter.in.web.controller;

import com.econnews.auth.adapter.in.web.response.AuthErrorResponses;
import com.econnews.auth.adapter.in.web.response.AuthMessages;
import java.util.List;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;
import com.econnews.auth.adapter.in.web.RefreshTokenCookieWriter;
import com.econnews.auth.adapter.in.web.request.LoginRequest;
import com.econnews.auth.adapter.in.web.response.CurrentAdminResponse;
import com.econnews.auth.adapter.in.web.response.LoginResponse;
import com.econnews.auth.application.exception.InvalidRefreshTokenException;
import com.econnews.auth.application.model.LoginCommand;
import com.econnews.auth.application.model.LoginResult;
import com.econnews.auth.application.usecase.*;
import com.econnews.shared.adapter.web.error.ApiErrorResponse;
import com.econnews.shared.adapter.web.response.ApiResponses;
import com.econnews.shared.adapter.web.response.ApiSuccessResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final LoginUseCase login;
    private final GetCurrentAdminUseCase currentAdmin;
    private final RefreshAccessTokenUseCase refresh;
    private final LogoutUseCase logout;
    private final RefreshTokenCookieWriter cookies;
    public AuthController(LoginUseCase login, GetCurrentAdminUseCase currentAdmin,
            RefreshAccessTokenUseCase refresh, LogoutUseCase logout, RefreshTokenCookieWriter cookies) {
        this.login=login; this.currentAdmin=currentAdmin; this.refresh=refresh;
        this.logout=logout; this.cookies=cookies;
    }
    @GetMapping("/csrf")
    public ResponseEntity<ApiSuccessResponse<CsrfResponse>> csrf(
            CsrfToken token, HttpServletRequest request) {
        return noStore(ApiResponses.ok(AuthMessages.CSRF,
                new CsrfResponse(token.getToken(), token.getHeaderName()), request));
    }
    @PostMapping("/login")
    public ResponseEntity<ApiSuccessResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest body, HttpServletRequest request,
            HttpServletResponse response) {
        var result = login.execute(new LoginCommand(body.username(), body.password()), cookies.read(request));
        return authenticated(result, AuthMessages.LOGIN, request, response);
    }
    @PostMapping("/refresh")
    public ResponseEntity<ApiSuccessResponse<LoginResponse>> refresh(
            HttpServletRequest request, HttpServletResponse response) {
        return authenticated(refresh.execute(cookies.read(request)),
                AuthMessages.REFRESH, request, response);
    }
    @PostMapping("/logout")
    public ResponseEntity<ApiSuccessResponse<Void>> logout(
            HttpServletRequest request, HttpServletResponse response) {
        logout.execute(cookies.read(request));
        cookies.clear(response);
        return noStore(ApiResponses.<Void>ok(AuthMessages.LOGOUT, null, request));
    }
    @GetMapping("/me")
    public ResponseEntity<ApiSuccessResponse<CurrentAdminResponse>> me(
            @AuthenticationPrincipal Jwt jwt, HttpServletRequest request) {
        var admin = currentAdmin.execute(Long.valueOf(jwt.getSubject()));
        return noStore(ApiResponses.ok(AuthMessages.CURRENT_ADMIN,
                CurrentAdminResponse.from(admin), request));
    }
    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<ApiErrorResponse> invalidRefresh(
            HttpServletRequest request, HttpServletResponse response) {
        cookies.clear(response);
        var error = AuthErrorResponses.INVALID_REFRESH_TOKEN;
        return noStore(ApiResponses.error(error.status(), error.code(), error.message(), List.of(), request));
    }
    private ResponseEntity<ApiSuccessResponse<LoginResponse>> authenticated(LoginResult result,
            String message, HttpServletRequest request, HttpServletResponse response) {
        cookies.write(response, result.refreshToken(), result.refreshExpiresAt());
        return noStore(ApiResponses.ok(message, LoginResponse.from(result), request));
    }
    private static <T> ResponseEntity<T> noStore(ResponseEntity<T> response) {
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders())
                .cacheControl(CacheControl.noStore()).header("Pragma", "no-cache").body(response.getBody());
    }
    public record CsrfResponse(String token, String headerName) {
        @Override public String toString() { return "CsrfResponse[REDACTED]"; }
    }
}
