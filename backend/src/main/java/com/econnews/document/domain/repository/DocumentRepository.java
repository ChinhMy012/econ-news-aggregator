package com.econnews.document.domain.repository;

import com.econnews.document.domain.model.ReferenceDocument;
import java.time.LocalDate;
import java.util.*;

public interface DocumentRepository {
    record Slice(List<ReferenceDocument> items, long total) {}

    Slice list(int page, int size);
    Optional<ReferenceDocument> find(long id);
    ReferenceDocument create(
        String title,
        String issuer,
        String url,
        LocalDate issuedOn,
        boolean enabled
    );
    boolean update(
        long id,
        long version,
        String title,
        String issuer,
        LocalDate issuedOn,
        boolean enabled
    );
}
