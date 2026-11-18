package com.econnews.document.adapter.in.web.response;

import com.econnews.document.domain.model.ReferenceDocument;
import java.time.*;

public record DocumentResponse(
    long id,
    String title,
    String issuer,
    String url,
    LocalDate issuedOn,
    boolean enabled,
    long version,
    Instant createdAt,
    Instant updatedAt
) {
    public static DocumentResponse from(ReferenceDocument d) {
        return new DocumentResponse(
            d.id(),
            d.title(),
            d.issuer(),
            d.url(),
            d.issuedOn(),
            d.enabled(),
            d.version(),
            d.createdAt(),
            d.updatedAt()
        );
    }
}
