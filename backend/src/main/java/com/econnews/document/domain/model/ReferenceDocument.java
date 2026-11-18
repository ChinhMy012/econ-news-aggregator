package com.econnews.document.domain.model;

import java.time.*;

public record ReferenceDocument(
    long id,
    String title,
    String issuer,
    String url,
    LocalDate issuedOn,
    boolean enabled,
    long version,
    Instant createdAt,
    Instant updatedAt
) {}
