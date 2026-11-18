package com.econnews.source.adapter.in.web;

import com.econnews.source.adapter.in.web.response.CatalogMessages;
import com.econnews.shared.adapter.web.response.*;
import com.econnews.source.adapter.in.web.request.CatalogRequests.*;
import com.econnews.source.adapter.in.web.response.CatalogResponses.*;
import com.econnews.source.application.usecase.ManageSourceCatalog;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/sources")
public class SourceCatalogController {

    private final ManageSourceCatalog service;

    public SourceCatalogController(ManageSourceCatalog service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<?> list(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        HttpServletRequest req
    ) {
        var result = service.sources(page, size);
        return ApiResponses.page(
            CatalogMessages.SOURCE_LIST,
            result.items().stream().map(Source::from).toList(),
            PageMeta.of(page, size, result.total()),
            req
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> detail(@PathVariable long id, HttpServletRequest req) {
        return ApiResponses.ok(CatalogMessages.SOURCE_DETAIL, Source.from(service.source(id)), req);
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody CreateSource b, HttpServletRequest req) {
        var s = Source.from(service.createSource(b.code(), b.name(), b.websiteUrl(), b.enabled()));
        return ApiResponses.created(
            CatalogMessages.SOURCE_CREATED,
            s,
            URI.create("/api/v1/admin/sources/" + s.id()),
            req
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(
        @PathVariable long id,
        @Valid @RequestBody UpdateSource b,
        HttpServletRequest req
    ) {
        return ApiResponses.ok(
            CatalogMessages.SOURCE_UPDATED,
            Source.from(
                service.updateSource(id, b.name(), b.websiteUrl(), b.enabled(), b.version())
            ),
            req
        );
    }

    @GetMapping("/{sourceId}/feeds")
    public ResponseEntity<?> feeds(
        @PathVariable long sourceId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        HttpServletRequest req
    ) {
        var result = service.feeds(sourceId, page, size);
        return ApiResponses.page(
            CatalogMessages.FEED_LIST,
            result.items().stream().map(Feed::from).toList(),
            PageMeta.of(page, size, result.total()),
            req
        );
    }

    @GetMapping("/{sourceId}/feeds/{id}")
    public ResponseEntity<?> feed(
        @PathVariable long sourceId,
        @PathVariable long id,
        HttpServletRequest req
    ) {
        return ApiResponses.ok(CatalogMessages.FEED_DETAIL, Feed.from(service.feed(sourceId, id)), req);
    }

    @PostMapping("/{sourceId}/feeds")
    public ResponseEntity<?> createFeed(
        @PathVariable long sourceId,
        @Valid @RequestBody CreateFeed b,
        HttpServletRequest req
    ) {
        var f = Feed.from(service.createFeed(sourceId, b.name(), b.url(), b.enabled()));
        return ApiResponses.created(
            CatalogMessages.FEED_CREATED,
            f,
            URI.create("/api/v1/admin/sources/" + sourceId + "/feeds/" + f.id()),
            req
        );
    }

    @PutMapping("/{sourceId}/feeds/{id}")
    public ResponseEntity<?> updateFeed(
        @PathVariable long sourceId,
        @PathVariable long id,
        @Valid @RequestBody UpdateFeed b,
        HttpServletRequest req
    ) {
        return ApiResponses.ok(
            CatalogMessages.FEED_UPDATED,
            Feed.from(
                service.updateFeed(sourceId, id, b.name(), b.url(), b.enabled(), b.version())
            ),
            req
        );
    }
}
