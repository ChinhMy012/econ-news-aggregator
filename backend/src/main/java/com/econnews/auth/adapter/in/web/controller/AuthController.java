package com.econnews.auth.adapter.in.web.controller;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.econnews.auth.adapter.in.web.request.LoginRequest;
import com.econnews.auth.adapter.in.web.response.LoginResponse;
import com.econnews.auth.application.model.LoginCommand;
import com.econnews.auth.application.usecase.LoginUseCase;
import com.econnews.shared.adapter.web.response.ApiResponses;
import com.econnews.shared.adapter.web.response.ApiSuccessResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final LoginUseCase loginUseCase;

    public AuthController(LoginUseCase loginUseCase) {
        this.loginUseCase = loginUseCase;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiSuccessResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest servletRequest) {
        var result = loginUseCase.execute(
                new LoginCommand(request.username(), request.password()));
        var response = ApiResponses.ok("Đăng nhập thành công.",
                LoginResponse.from(result), servletRequest);
        return ResponseEntity.status(response.getStatusCode())
                .headers(response.getHeaders())
                .cacheControl(CacheControl.noStore())
                .header(HttpHeaders.PRAGMA, "no-cache")
                .body(response.getBody());
    }
}
