package com.econnews.auth.application.usecase;

import java.time.Clock;
import java.time.Duration;
import java.util.UUID;
import com.econnews.auth.application.model.LoginCommand;
import com.econnews.auth.application.model.LoginResult;
import com.econnews.auth.application.port.out.AccessTokenIssuer;
import com.econnews.auth.application.port.out.AuthTransaction;
import com.econnews.auth.application.port.out.RefreshTokenCodec;
import com.econnews.auth.domain.repository.RefreshTokenRepository;

public final class LoginUseCase {
    private final AuthenticateAdminUseCase authenticate;
    private final AccessTokenIssuer issuer;
    private final RefreshTokenRepository tokens;
    private final RefreshTokenCodec codec;
    private final AuthTransaction transaction;
    private final Clock clock;
    private final Duration ttl;
    public LoginUseCase(AuthenticateAdminUseCase authenticate, AccessTokenIssuer issuer,
            RefreshTokenRepository tokens, RefreshTokenCodec codec, AuthTransaction transaction,
            Clock clock, Duration ttl) {
        this.authenticate=authenticate; this.issuer=issuer; this.tokens=tokens;
        this.codec=codec; this.transaction=transaction; this.clock=clock; this.ttl=ttl;
    }
    public LoginResult execute(LoginCommand command, String previousRefreshToken) {
        var admin = authenticate.execute(command);
        return transaction.execute(() -> {
            var now = clock.instant();
            var accessToken = issuer.issue(admin);
            String rawToken = codec.generate();
            var expiresAt = now.plus(ttl);
            // Đăng nhập lại trong cùng trình duyệt thay thế phiên cũ, không ảnh hưởng thiết bị khác.
            if (codec.isWellFormed(previousRefreshToken)) {
                tokens.findByHash(codec.hash(previousRefreshToken)).ifPresent(old -> {
                    if (tokens.lockFamily(old.familyId())) tokens.revokeFamily(old.familyId(), now);
                });
            }
            tokens.insert(admin.id(), codec.hash(rawToken), UUID.randomUUID(), now, expiresAt);
            return new LoginResult(admin, accessToken, rawToken, expiresAt);
        });
    }
}
