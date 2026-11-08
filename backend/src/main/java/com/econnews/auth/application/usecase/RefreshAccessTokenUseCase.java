package com.econnews.auth.application.usecase;

import java.time.Clock;
import com.econnews.auth.application.exception.InvalidRefreshTokenException;
import com.econnews.auth.application.model.AuthenticatedAdmin;
import com.econnews.auth.application.model.LoginResult;
import com.econnews.auth.application.port.out.AccessTokenIssuer;
import com.econnews.auth.application.port.out.AuthTransaction;
import com.econnews.auth.application.port.out.RefreshTokenCodec;
import com.econnews.auth.domain.repository.AdminUserRepository;
import com.econnews.auth.domain.repository.RefreshTokenRepository;

public final class RefreshAccessTokenUseCase {
    private final RefreshTokenRepository tokens;
    private final AdminUserRepository users;
    private final RefreshTokenCodec codec;
    private final AccessTokenIssuer issuer;
    private final AuthTransaction transaction;
    private final Clock clock;
    public RefreshAccessTokenUseCase(RefreshTokenRepository tokens, AdminUserRepository users,
            RefreshTokenCodec codec, AccessTokenIssuer issuer, AuthTransaction transaction, Clock clock) {
        this.tokens = tokens; this.users = users; this.codec = codec;
        this.issuer = issuer; this.transaction = transaction; this.clock = clock;
    }
    public LoginResult execute(String rawToken) {
        if (!codec.isWellFormed(rawToken)) throw new InvalidRefreshTokenException();
        String hash = codec.hash(rawToken);
        LoginResult result = transaction.execute(() -> {
            var candidate = tokens.findByHash(hash);
            if (candidate.isEmpty() || !tokens.lockFamily(candidate.get().familyId())) return null;
            // Đọc lại SAU khi khóa để thấy kết quả của yêu cầu vừa chạy trước đó.
            var current = tokens.findByHash(hash);
            if (current.isEmpty()) return null;
            var token = current.get();
            var now = clock.instant();
            if (token.isUsed()) {
                tokens.revokeFamily(token.familyId(), now);
                return null;
            }
            if (token.isRevoked() || token.isExpiredAt(now)) return null;
            var account = users.findById(token.adminUserId()).filter(user -> user.isEnabled());
            if (account.isEmpty()) {
                tokens.revokeFamily(token.familyId(), now);
                return null;
            }
            var user = account.get();
            var admin = new AuthenticatedAdmin(user.getId(), user.getUsername());
            var accessToken = issuer.issue(admin);
            String replacement = codec.generate();
            tokens.insert(user.getId(), codec.hash(replacement), token.familyId(), now, token.expiresAt());
            tokens.markUsed(token.id(), now);
            return new LoginResult(admin, accessToken, replacement, token.expiresAt());
        });
        // Ném lỗi ngoài transaction để việc thu hồi do tái sử dụng vẫn được commit.
        if (result == null) throw new InvalidRefreshTokenException();
        return result;
    }
}
