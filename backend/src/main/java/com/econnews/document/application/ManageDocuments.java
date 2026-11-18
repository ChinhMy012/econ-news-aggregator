package com.econnews.document.application;

import com.econnews.document.application.exception.DocumentError;
import com.econnews.document.application.exception.DocumentException;
import com.econnews.document.domain.model.ReferenceDocument;
import com.econnews.document.domain.repository.DocumentRepository;
import com.econnews.shared.application.port.*;
import java.net.URI;
import java.time.LocalDate;
import java.util.Locale;

public class ManageDocuments {

    private final DocumentRepository repo;
    private final ContentTransaction tx;
    private final PublicationReferences refs;

    public ManageDocuments(
        DocumentRepository repo,
        ContentTransaction tx,
        PublicationReferences refs
    ) {
        this.repo = repo;
        this.tx = tx;
        this.refs = refs;
    }

    public DocumentRepository.Slice list(int page, int size) {
        if (page < 0 || size < 1 || size > 100) throw new DocumentException(DocumentError.INVALID_PAGINATION);
        return tx.read(() -> repo.list(page, size));
    }

    public ReferenceDocument detail(long id) {
        return repo
            .find(id)
            .orElseThrow(() -> new DocumentException(DocumentError.RESOURCE_NOT_FOUND));
    }

    public ReferenceDocument create(
        String title,
        String issuer,
        String url,
        LocalDate issuedOn,
        boolean enabled
    ) {
        String name = text(title, 300),
            publisher = text(issuer, 200),
            normalized = url(url);
        return tx.write(() -> {
            refs.lock();
            return repo.create(name, publisher, normalized, issuedOn, enabled);
        });
    }

    public ReferenceDocument update(
        long id,
        long version,
        String title,
        String issuer,
        LocalDate issuedOn,
        boolean enabled
    ) {
        String name = text(title, 300),
            publisher = text(issuer, 200);
        return tx.write(() -> {
            refs.lock();
            var old = detail(id);
            if (old.version() != version) throw new DocumentException(DocumentError.VERSION_CONFLICT);
            if (!enabled && old.enabled() && refs.documentIsEssential(id)) throw new DocumentException(DocumentError.SOURCE_REQUIRED);
            if (!repo.update(id, version, name, publisher, issuedOn, enabled)) throw new DocumentException(DocumentError.VERSION_CONFLICT);
            return detail(id);
        });
    }

    private String text(String value, int max) {
        if (value == null || value.isBlank() || value.strip().length() > max) throw new DocumentException(DocumentError.INVALID_DOCUMENT);
        return value.strip();
    }

    private String url(String value) {
        try {
            URI u = URI.create(value.strip());
            if (
                !"https".equalsIgnoreCase(u.getScheme()) ||
                u.getHost() == null ||
                u.getUserInfo() != null ||
                u.getFragment() != null ||
                (u.getPort() != -1 && u.getPort() != 443)
            ) throw new IllegalArgumentException();
            String path = u.getRawPath();
            String result =
                "https://" +
                u.getHost().toLowerCase(Locale.ROOT) +
                (path == null || path.isEmpty() ? "/" : path) +
                (u.getRawQuery() == null ? "" : "?" + u.getRawQuery());
            result = URI.create(result).toASCIIString();
            if (result.length() > 2048) throw new IllegalArgumentException();
            return result;
        } catch (RuntimeException e) {
            throw new DocumentException(DocumentError.INVALID_DOCUMENT_URL);
        }
    }

}
