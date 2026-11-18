package com.econnews.document.adapter.in.web;

import com.econnews.document.adapter.in.web.response.DocumentMessages;
import com.econnews.document.adapter.in.web.request.DocumentRequests.*;
import com.econnews.document.adapter.in.web.response.DocumentResponse;
import com.econnews.document.application.ManageDocuments;
import com.econnews.shared.adapter.web.response.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/documents")
public class DocumentController {

    private final ManageDocuments documents;

    public DocumentController(ManageDocuments documents) {
        this.documents = documents;
    }

    @GetMapping
    public ResponseEntity<?> list(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        HttpServletRequest request
    ) {
        var result = documents.list(page, size);
        return ApiResponses.page(
            DocumentMessages.LIST,
            result.items().stream().map(DocumentResponse::from).toList(),
            PageMeta.of(page, size, result.total()),
            request
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> detail(@PathVariable long id, HttpServletRequest request) {
        return ApiResponses.ok(
            DocumentMessages.DETAIL,
            DocumentResponse.from(documents.detail(id)),
            request
        );
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody Create body, HttpServletRequest request) {
        var result = DocumentResponse.from(
            documents.create(
                body.title(),
                body.issuer(),
                body.url(),
                body.issuedOn(),
                body.enabled()
            )
        );
        return ApiResponses.created(
            DocumentMessages.CREATED,
            result,
            URI.create("/api/v1/admin/documents/" + result.id()),
            request
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(
        @PathVariable long id,
        @Valid @RequestBody Update body,
        HttpServletRequest request
    ) {
        return ApiResponses.ok(
            DocumentMessages.UPDATED,
            DocumentResponse.from(
                documents.update(
                    id,
                    body.version(),
                    body.title(),
                    body.issuer(),
                    body.issuedOn(),
                    body.enabled()
                )
            ),
            request
        );
    }
}
