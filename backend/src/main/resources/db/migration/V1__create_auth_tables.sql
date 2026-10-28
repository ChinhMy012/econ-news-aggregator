-- Tài khoản quản trị. Không tạo tài khoản hoặc mật khẩu mẫu trong migration.
CREATE TABLE admin_users (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    password_hash TEXT NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_admin_users_username UNIQUE (username),
    CONSTRAINT ck_admin_users_username CHECK (
        username = LOWER(BTRIM(username))
        AND CHAR_LENGTH(username) > 0
    ),
    CONSTRAINT ck_admin_users_password_hash CHECK (
        CHAR_LENGTH(BTRIM(password_hash)) > 0
    ),
    CONSTRAINT ck_admin_users_updated_at CHECK (updated_at >= created_at)
);

-- Mỗi dòng là một refresh token đã cấp; các token cùng phiên có chung family_id.
-- token_hash: SHA-256 dạng hexadecimal chữ thường của token ngẫu nhiên đủ mạnh.
-- Backend giữ nguyên expires_at của phiên khi xoay token và quản lý thu hồi.
CREATE TABLE refresh_tokens (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    admin_user_id BIGINT NOT NULL,
    token_hash VARCHAR(64) NOT NULL,
    family_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMPTZ NOT NULL,
    used_at TIMESTAMPTZ,
    revoked_at TIMESTAMPTZ,

    CONSTRAINT fk_refresh_tokens_admin_user
        FOREIGN KEY (admin_user_id)
        REFERENCES admin_users (id)
        ON DELETE RESTRICT,
    CONSTRAINT uk_refresh_tokens_token_hash UNIQUE (token_hash),
    CONSTRAINT ck_refresh_tokens_token_hash CHECK (
        token_hash ~ '^[0-9a-f]{64}$'
    ),
    CONSTRAINT ck_refresh_tokens_expires_at CHECK (expires_at > created_at),
    CONSTRAINT ck_refresh_tokens_used_at CHECK (
        used_at IS NULL OR used_at >= created_at
    ),
    CONSTRAINT ck_refresh_tokens_revoked_at CHECK (
        revoked_at IS NULL OR revoked_at >= created_at
    )
);

-- Tra cứu/thu hồi theo tài khoản, theo phiên và dọn dữ liệu hết hạn.
CREATE INDEX idx_refresh_tokens_admin_user ON refresh_tokens (admin_user_id);
CREATE INDEX idx_refresh_tokens_family ON refresh_tokens (family_id);
CREATE INDEX idx_refresh_tokens_expires_at ON refresh_tokens (expires_at);
