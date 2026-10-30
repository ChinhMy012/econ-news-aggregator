package com.econnews.auth.application.usecase;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

import com.econnews.auth.application.exception.InvalidCredentialsException;
import com.econnews.auth.application.model.AuthenticatedAdmin;
import com.econnews.auth.application.model.LoginCommand;
import com.econnews.auth.application.port.out.PasswordVerifier;
import com.econnews.auth.domain.repository.AdminUserRepository;

public final class AuthenticateAdminUseCase {

    private final AdminUserRepository repository;
    private final PasswordVerifier passwordVerifier;

    public AuthenticateAdminUseCase(
            AdminUserRepository repository,
            PasswordVerifier passwordVerifier) {
        this.repository = repository;
        this.passwordVerifier = passwordVerifier;
    }

    public AuthenticatedAdmin execute(LoginCommand command) {
        if (command == null
                || command.username() == null
                || command.username().isBlank()
                || command.password() == null
                || command.password().isEmpty()) {
            throw new InvalidCredentialsException();
        }

        // BCrypt giới hạn đầu vào tối đa 72 byte.
        if (command.password()
                .getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new InvalidCredentialsException();
        }

        String username = command.username()
                .strip()
                .toLowerCase(Locale.ROOT);

        var account = repository.findByUsername(username);

        String passwordHash = account
                .map(user -> user.getPasswordHash())
                .orElse(null);

        // Vẫn kiểm tra mật khẩu khi không tìm thấy tài khoản.
        boolean passwordMatches = passwordVerifier.matches(
                command.password(),
                passwordHash);

        if (account.isEmpty()) {
            throw new InvalidCredentialsException();
        }

        var user = account.get();

        if (!passwordMatches || !user.isEnabled()) {
            throw new InvalidCredentialsException();
        }

        return new AuthenticatedAdmin(
                user.getId(),
                user.getUsername());
    }
}