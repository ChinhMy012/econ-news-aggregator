package com.econnews.auth.adapter.in.scheduling;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.econnews.auth.application.usecase.CleanupRefreshTokensUseCase;

@Component
@Profile("!provision-admin")
public class RefreshTokenCleanupScheduler {
    private static final Logger log = LoggerFactory.getLogger(RefreshTokenCleanupScheduler.class);
    private final CleanupRefreshTokensUseCase cleanup;
    public RefreshTokenCleanupScheduler(CleanupRefreshTokensUseCase cleanup) { this.cleanup=cleanup; }
    @Scheduled(cron = "${app.auth.cleanup-cron:0 0 3 * * *}", zone = "UTC")
    public void cleanup() {
        int removed = cleanup.execute();
        log.info("Đã dọn {} bản ghi refresh token của các phiên hết hạn quá thời gian lưu giữ.", removed);
    }
}
