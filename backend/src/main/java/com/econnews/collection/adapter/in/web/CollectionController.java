package com.econnews.collection.adapter.in.web;

import com.econnews.collection.adapter.in.web.response.CollectionMessages;
import com.econnews.collection.application.usecase.CollectFeed;
import com.econnews.collection.domain.model.CollectionRun;
import com.econnews.shared.adapter.web.response.*;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/sources/{sourceId}/feeds/{feedId}/collections")
public class CollectionController {

    private final CollectFeed collect;

    public CollectionController(CollectFeed collect) {
        this.collect = collect;
    }

    public record RunResponse(
        long id,
        long sourceId,
        long feedId,
        String status,
        String trigger,
        int receivedCount,
        int insertedCount,
        int duplicateCount,
        int invalidCount,
        String errorCode,
        Instant startedAt,
        Instant finishedAt
    ) {
        public static RunResponse from(CollectionRun r) {
            return new RunResponse(
                r.id(),
                r.sourceId(),
                r.feedId(),
                r.status(),
                r.requestedBy() == null ? "SCHEDULED" : "MANUAL",
                r.receivedCount(),
                r.insertedCount(),
                r.duplicateCount(),
                r.invalidCount(),
                r.errorCode(),
                r.startedAt(),
                r.finishedAt()
            );
        }
    }

    @PostMapping
    public ResponseEntity<?> run(
        @PathVariable long sourceId,
        @PathVariable long feedId,
        @AuthenticationPrincipal Jwt jwt,
        HttpServletRequest request
    ) {
        var result = RunResponse.from(
            collect.collect(sourceId, feedId, Long.parseLong(jwt.getSubject()))
        );
        return ApiResponses.ok(
            CollectionMessages.RUN_RECORDED,
            result,
            request
        );
    }

    @GetMapping
    public ResponseEntity<?> history(
        @PathVariable long sourceId,
        @PathVariable long feedId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        HttpServletRequest request
    ) {
        var result = collect.history(sourceId, feedId, page, size);
        return ApiResponses.page(
            CollectionMessages.RUN_HISTORY,
            result.items().stream().map(RunResponse::from).toList(),
            PageMeta.of(page, size, result.total()),
            request
        );
    }
}
