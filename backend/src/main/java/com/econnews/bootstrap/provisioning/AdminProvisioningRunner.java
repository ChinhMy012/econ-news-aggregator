package com.econnews.bootstrap.provisioning;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("provision-admin")
public class AdminProvisioningRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminProvisioningRunner.class);

    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;
    private final Environment environment;
    private final ConfigurableApplicationContext context;

    public AdminProvisioningRunner(
            JdbcTemplate jdbcTemplate,
            PasswordEncoder passwordEncoder,
            Environment environment,
            ConfigurableApplicationContext context) {
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
        this.environment = environment;
        this.context = context;
    }

    @Override
    public void run(String... args) {
        try {
            String username = environment.getRequiredProperty(
                    "ECONNEWS_ADMIN_USERNAME").strip().toLowerCase(Locale.ROOT);

            String password = environment.getRequiredProperty(
                    "ECONNEWS_ADMIN_PASSWORD");

            validate(username, password);

            String passwordHash = passwordEncoder.encode(password);

            int inserted = jdbcTemplate.update(
                    """
                            INSERT INTO admin_users (
                                username,
                                password_hash,
                                enabled
                            )
                            VALUES (?, ?, TRUE)
                            ON CONFLICT (username) DO NOTHING
                            """,
                    username,
                    passwordHash);

            if (inserted == 0) {
                throw new IllegalStateException(
                        "Tài khoản đã tồn tại. Không thay đổi mật khẩu.");
            }

            log.info("Đã tạo tài khoản quản trị thành công.");
        } finally {
            context.close();
        }
    }

    private void validate(String username, String password) {
        if (username.isBlank() || username.length() > 100) {
            throw new IllegalArgumentException(
                    "Tên đăng nhập phải có từ 1 đến 100 ký tự.");
        }

        if (password.isBlank() || password.length() < 12) {
            throw new IllegalArgumentException(
                    "Mật khẩu phải có ít nhất 12 ký tự và không được toàn khoảng trắng.");
        }

        if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException(
                    "Mật khẩu vượt giới hạn 72 byte của BCrypt.");
        }
    }
}