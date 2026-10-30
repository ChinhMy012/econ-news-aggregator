package com.econnews.bootstrap.config;

import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.econnews.auth.adapter.out.security.PasswordVerifierAdapter;
import com.econnews.auth.application.port.out.PasswordVerifier;
import com.econnews.auth.application.usecase.AuthenticateAdminUseCase;
import com.econnews.auth.domain.repository.AdminUserRepository;

@Configuration
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
}