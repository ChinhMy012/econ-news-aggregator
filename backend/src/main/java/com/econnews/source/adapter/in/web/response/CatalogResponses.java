package com.econnews.source.adapter.in.web.response;

import com.econnews.source.domain.model.*;
import java.time.Instant;

public final class CatalogResponses {

    private CatalogResponses() {}

    public record Source(
        long id,
        String code,
        String name,
        String websiteUrl,
        boolean enabled,
        long version,
        Instant createdAt,
        Instant updatedAt
    ) {
        public static Source from(NewsSource s) {
            return new Source(
                s.id(),
                s.code(),
                s.name(),
                s.websiteUrl(),
                s.enabled(),
                s.version(),
                s.createdAt(),
                s.updatedAt()
            );
        }
    }

    public record Feed(
        long id,
        long sourceId,
        String name,
        String url,
        boolean enabled,
        long version,
        Instant createdAt,
        Instant updatedAt
    ) {
        public static Feed from(RssFeed f) {
            return new Feed(
                f.id(),
                f.sourceId(),
                f.name(),
                f.url(),
                f.enabled(),
                f.version(),
                f.createdAt(),
                f.updatedAt()
            );
        }
    }
}
