package com.econnews.event.adapter.in.web;

import com.econnews.event.adapter.in.web.request.EventRequests.*;
import com.econnews.event.adapter.in.web.response.EventResponses.*;
import com.econnews.event.adapter.in.web.response.EventMessages;
import com.econnews.event.application.ManageEvents;
import com.econnews.shared.adapter.web.response.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/events")
public class EventAdminController {

    private final ManageEvents events;

    public EventAdminController(ManageEvents events) {
        this.events = events;
    }

    @GetMapping
    public ResponseEntity<?> list(
        @RequestParam(defaultValue = "ALL") String status,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        HttpServletRequest request
    ) {
        var result = events.list(status, page, size, false);
        return ApiResponses.page(
            EventMessages.ADMIN_LIST,
            result.items().stream().map(AdminSummary::from).toList(),
            PageMeta.of(page, size, result.total()),
            request
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> detail(@PathVariable long id, HttpServletRequest request) {
        return ApiResponses.ok(
            EventMessages.ADMIN_DETAIL,
            AdminDetail.from(events.detail(id, false)),
            request
        );
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody Create body, HttpServletRequest request) {
        var result = AdminDetail.from(events.create(body.command()));
        return ApiResponses.created(
            EventMessages.CREATED,
            result,
            URI.create("/api/v1/admin/events/" + result.event().id()),
            request
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> edit(
        @PathVariable long id,
        @Valid @RequestBody Update body,
        HttpServletRequest request
    ) {
        return ApiResponses.ok(
            EventMessages.UPDATED,
            AdminDetail.from(events.edit(id, body.version(), body.command())),
            request
        );
    }

    @PostMapping("/{id}/publish")
    public ResponseEntity<?> publish(
        @PathVariable long id,
        @Valid @RequestBody Version body,
        HttpServletRequest request
    ) {
        return ApiResponses.ok(
            EventMessages.PUBLISHED,
            AdminDetail.from(events.publish(id, body.version())),
            request
        );
    }

    @PostMapping("/{id}/hide")
    public ResponseEntity<?> hide(
        @PathVariable long id,
        @Valid @RequestBody Hide body,
        HttpServletRequest request
    ) {
        return ApiResponses.ok(
            EventMessages.HIDDEN,
            AdminDetail.from(events.hide(id, body.version(), body.reason())),
            request
        );
    }

    @PutMapping("/{id}/overview-sources")
    public ResponseEntity<?> overviewSources(
        @PathVariable long id,
        @Valid @RequestBody Sources body,
        HttpServletRequest request
    ) {
        return ApiResponses.ok(
            EventMessages.OVERVIEW_SOURCES_UPDATED,
            AdminDetail.from(
                events.sources(id, null, body.version(), body.articleIds(), body.documentIds())
            ),
            request
        );
    }

    @PostMapping("/{id}/milestones")
    public ResponseEntity<?> createMilestone(
        @PathVariable long id,
        @Valid @RequestBody MilestoneBody body,
        HttpServletRequest request
    ) {
        return ApiResponses.ok(
            EventMessages.MILESTONE_CREATED,
            AdminDetail.from(events.createMilestone(id, body.version(), body.command())),
            request
        );
    }

    @PutMapping("/{id}/milestones/{milestoneId}")
    public ResponseEntity<?> editMilestone(
        @PathVariable long id,
        @PathVariable long milestoneId,
        @Valid @RequestBody MilestoneBody body,
        HttpServletRequest request
    ) {
        return ApiResponses.ok(
            EventMessages.MILESTONE_UPDATED,
            AdminDetail.from(events.editMilestone(id, milestoneId, body.version(), body.command())),
            request
        );
    }

    @PutMapping("/{id}/milestones/{milestoneId}/sources")
    public ResponseEntity<?> milestoneSources(
        @PathVariable long id,
        @PathVariable long milestoneId,
        @Valid @RequestBody Sources body,
        HttpServletRequest request
    ) {
        return ApiResponses.ok(
            EventMessages.MILESTONE_SOURCES_UPDATED,
            AdminDetail.from(
                events.sources(
                    id,
                    milestoneId,
                    body.version(),
                    body.articleIds(),
                    body.documentIds()
                )
            ),
            request
        );
    }

    @PostMapping("/{id}/milestones/{milestoneId}/publish")
    public ResponseEntity<?> publishMilestone(
        @PathVariable long id,
        @PathVariable long milestoneId,
        @Valid @RequestBody Version body,
        HttpServletRequest request
    ) {
        return ApiResponses.ok(
            EventMessages.MILESTONE_PUBLISHED,
            AdminDetail.from(events.milestoneStatus(id, milestoneId, body.version(), true)),
            request
        );
    }

    @PostMapping("/{id}/milestones/{milestoneId}/unpublish")
    public ResponseEntity<?> unpublishMilestone(
        @PathVariable long id,
        @PathVariable long milestoneId,
        @Valid @RequestBody Version body,
        HttpServletRequest request
    ) {
        return ApiResponses.ok(
            EventMessages.MILESTONE_UNPUBLISHED,
            AdminDetail.from(events.milestoneStatus(id, milestoneId, body.version(), false)),
            request
        );
    }
}
