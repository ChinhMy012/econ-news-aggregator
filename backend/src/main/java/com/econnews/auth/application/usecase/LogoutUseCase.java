package com.econnews.auth.application.usecase;

import java.time.Clock;
import com.econnews.auth.application.port.out.AuthTransaction;
import com.econnews.auth.application.port.out.RefreshTokenCodec;
import com.econnews.auth.domain.repository.RefreshTokenRepository;

public final class LogoutUseCase {
    private final RefreshTokenRepository tokens;
    private final RefreshTokenCodec codec;
    private final AuthTransaction transaction;
    private final Clock clock;
    public LogoutUseCase(RefreshTokenRepository tokens, RefreshTokenCodec codec,
            AuthTransaction transaction, Clock clock) {
        this.tokens=tokens; this.codec=codec; this.transaction=transaction; this.clock=clock;
    }
    public void execute(String rawToken) {
        if (!codec.isWellFormed(rawToken)) return;
        transaction.execute(() -> {
            tokens.findByHash(codec.hash(rawToken)).ifPresent(token -> {
                if (tokens.lockFamily(token.familyId())) tokens.revokeFamily(token.familyId(), clock.instant());
            });
            return null;
        });
    }
}
