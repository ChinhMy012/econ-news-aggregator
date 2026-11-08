package com.econnews.bootstrap.config;

import java.util.Map;
import java.time.Clock;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.PlatformTransactionManager;
import com.econnews.auth.adapter.out.persistence.SpringAuthTransaction;
import com.econnews.auth.adapter.out.security.SecureRefreshTokenCodec;
import com.econnews.auth.application.port.out.AuthTransaction;
import com.econnews.auth.application.port.out.RefreshTokenCodec;
import com.econnews.auth.domain.repository.RefreshTokenRepository;
import com.econnews.auth.application.usecase.RefreshAccessTokenUseCase;
import com.econnews.auth.application.usecase.LogoutUseCase;
import com.econnews.auth.application.usecase.CleanupRefreshTokensUseCase;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.econnews.auth.adapter.out.security.PasswordVerifierAdapter;
import com.econnews.auth.application.port.out.PasswordVerifier;
import com.econnews.auth.application.port.out.AccessTokenIssuer;
import com.econnews.auth.application.usecase.LoginUseCase;
import com.econnews.auth.application.usecase.GetCurrentAdminUseCase;
import com.econnews.auth.application.usecase.AuthenticateAdminUseCase;
import com.econnews.auth.domain.repository.AdminUserRepository;

@Configuration
@EnableScheduling
@EnableConfigurationProperties(AuthSessionProperties.class)
public class AuthConfiguration {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new DelegatingPasswordEncoder(
                "bcrypt",
                Map.of("bcrypt", new BCryptPasswordEncoder(12)));
    }

    @Bean
    public PasswordVerifier passwordVerifier(
            PasswordEncoder passwordEncoder) {
        return new PasswordVerifierAdapter(passwordEncoder);
    }

    @Bean
    public AuthenticateAdminUseCase authenticateAdminUseCase(
            AdminUserRepository repository,
            PasswordVerifier passwordVerifier) {
        return new AuthenticateAdminUseCase(
                repository,
                passwordVerifier);
    }
    @Bean
    public AuthTransaction authTransaction(PlatformTransactionManager manager) {
        return new SpringAuthTransaction(manager);
    }
    @Bean
    public RefreshTokenCodec refreshTokenCodec() { return new SecureRefreshTokenCodec(); }
    @Bean
    public LoginUseCase loginUseCase(AuthenticateAdminUseCase authenticate, AccessTokenIssuer issuer,
            RefreshTokenRepository tokens, RefreshTokenCodec codec, AuthTransaction transaction,
            AuthSessionProperties properties) {
        return new LoginUseCase(authenticate, issuer, tokens, codec, transaction,
                Clock.systemUTC(), properties.refreshTokenTtl());
    }
    @Bean
    public RefreshAccessTokenUseCase refreshAccessTokenUseCase(RefreshTokenRepository tokens,
            AdminUserRepository users, RefreshTokenCodec codec, AccessTokenIssuer issuer,
            AuthTransaction transaction) {
        return new RefreshAccessTokenUseCase(tokens, users, codec, issuer, transaction, Clock.systemUTC());
    }
    @Bean
    public LogoutUseCase logoutUseCase(RefreshTokenRepository tokens, RefreshTokenCodec codec,
            AuthTransaction transaction) {
        return new LogoutUseCase(tokens, codec, transaction, Clock.systemUTC());
    }
    @Bean
    public CleanupRefreshTokensUseCase cleanupRefreshTokensUseCase(RefreshTokenRepository tokens,
            AuthTransaction transaction, AuthSessionProperties properties) {
        return new CleanupRefreshTokensUseCase(tokens, transaction, Clock.systemUTC(),
                properties.expiredTokenRetention());
    }
    @Bean
    public GetCurrentAdminUseCase getCurrentAdminUseCase(AdminUserRepository repository) {
        return new GetCurrentAdminUseCase(repository);
    }
}