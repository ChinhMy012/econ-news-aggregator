package com.econnews.auth.application.usecase;

import java.time.Clock;
import java.time.Duration;
import com.econnews.auth.application.port.out.AuthTransaction;
import com.econnews.auth.domain.repository.RefreshTokenRepository;

public final class CleanupRefreshTokensUseCase {
    private final RefreshTokenRepository tokens;
    private final AuthTransaction transaction;
    private final Clock clock;
    private final Duration retention;
    public CleanupRefreshTokensUseCase(RefreshTokenRepository tokens, AuthTransaction transaction,
            Clock clock, Duration retention) {
        this.tokens=tokens; this.transaction=transaction; this.clock=clock; this.retention=retention;
    }
    public int execute() {
        return transaction.execute(() -> tokens.deleteFamiliesExpiredBefore(clock.instant().minus(retention)));
    }
}
