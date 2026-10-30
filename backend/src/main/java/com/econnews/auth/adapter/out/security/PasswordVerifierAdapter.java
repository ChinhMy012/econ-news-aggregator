package com.econnews.auth.adapter.out.security;

import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;

import com.econnews.auth.application.port.out.PasswordVerifier;

public final class PasswordVerifierAdapter implements PasswordVerifier {

    private final PasswordEncoder passwordEncoder;
    private final String dummyHash;

    public PasswordVerifierAdapter(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
        this.dummyHash = passwordEncoder.encode(
                UUID.randomUUID().toString());
    }

    @Override
    public boolean matches(
            String rawPassword,
            String passwordHash) {

        if (passwordHash == null) {
            passwordEncoder.matches(rawPassword, dummyHash);
            return false;
        }

        return passwordEncoder.matches(rawPassword, passwordHash);
    }
}