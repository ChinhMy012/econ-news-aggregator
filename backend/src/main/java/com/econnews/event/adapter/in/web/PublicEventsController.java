package com.econnews.event.adapter.in.web;

import com.econnews.event.adapter.in.web.response.EventResponses.*;
import com.econnews.event.adapter.in.web.response.EventMessages;
import com.econnews.event.application.ManageEvents;
import com.econnews.shared.adapter.web.response.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/events")
public class PublicEventsController {

    private final ManageEvents events;

    public PublicEventsController(ManageEvents events) {
        this.events = events;
    }

    @GetMapping
    public ResponseEntity<?> list(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(required = false) String contentType,
        HttpServletRequest request
    ) {
        var result = events.listPublished(contentType, page, size);
        return ApiResponses.page(
            EventMessages.PUBLIC_LIST,
            result.items().stream().map(PublicSummary::from).toList(),
            PageMeta.of(page, size, result.total()),
            request
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> detail(@PathVariable long id, HttpServletRequest request) {
        return ApiResponses.ok(
            EventMessages.PUBLIC_DETAIL,
            PublicDetail.from(events.detail(id, true)),
            request
        );
    }

    @GetMapping("/{id}/milestones")
    public ResponseEntity<?> milestones(@PathVariable long id, HttpServletRequest request) {
        return ApiResponses.ok(
            EventMessages.PUBLIC_MILESTONES,
            PublicDetail.from(events.detail(id, true)).milestones(),
            request
        );
    }

    @GetMapping("/{id}/articles")
    public ResponseEntity<?> articles(@PathVariable long id, HttpServletRequest request) {
        return ApiResponses.ok(
            EventMessages.PUBLIC_ARTICLES,
            PublicDetail.from(events.detail(id, true)).sources("ARTICLE"),
            request
        );
    }

    @GetMapping("/{id}/documents")
    public ResponseEntity<?> documents(@PathVariable long id, HttpServletRequest request) {
        return ApiResponses.ok(
            EventMessages.PUBLIC_DOCUMENTS,
            PublicDetail.from(events.detail(id, true)).sources("DOCUMENT"),
            request
        );
    }
}
