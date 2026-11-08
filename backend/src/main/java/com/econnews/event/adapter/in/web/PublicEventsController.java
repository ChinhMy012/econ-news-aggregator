package com.econnews.event.adapter.in.web;

import com.econnews.event.adapter.in.web.response.EventResponses.*;
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
        HttpServletRequest request
    ) {
        var result = events.list("PUBLISHED", page, size, true);
        return ApiResponses.page(
            "Sự kiện kinh tế.",
            result.items().stream().map(PublicSummary::from).toList(),
            PageMeta.of(page, size, result.total()),
            request
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> detail(@PathVariable long id, HttpServletRequest request) {
        return ApiResponses.ok(
            "Diễn biến sự kiện.",
            PublicDetail.from(events.detail(id, true)),
            request
        );
    }

    @GetMapping("/{id}/milestones")
    public ResponseEntity<?> milestones(@PathVariable long id, HttpServletRequest request) {
        return ApiResponses.ok(
            "Các mốc diễn biến.",
            PublicDetail.from(events.detail(id, true)).milestones(),
            request
        );
    }

    @GetMapping("/{id}/articles")
    public ResponseEntity<?> articles(@PathVariable long id, HttpServletRequest request) {
        return ApiResponses.ok(
            "Bài viết được trích dẫn.",
            PublicDetail.from(events.detail(id, true)).sources("ARTICLE"),
            request
        );
    }

    @GetMapping("/{id}/documents")
    public ResponseEntity<?> documents(@PathVariable long id, HttpServletRequest request) {
        return ApiResponses.ok(
            "Tài liệu được trích dẫn.",
            PublicDetail.from(events.detail(id, true)).sources("DOCUMENT"),
            request
        );
    }
}
