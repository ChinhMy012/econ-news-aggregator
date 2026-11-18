package com.econnews.event.application;

import com.econnews.event.application.exception.EventError;
import com.econnews.event.application.exception.EventException;
import com.econnews.event.application.model.*;
import com.econnews.event.application.model.EventCommands.*;
import com.econnews.event.domain.model.*;
import com.econnews.event.domain.repository.EventRepository;
import com.econnews.shared.application.port.*;
import java.time.*;
import java.util.*;
import java.util.function.Consumer;

public class ManageEvents {

    private final EventRepository repo;
    private final ContentTransaction tx;
    private final PublicationReferences refs;

    public ManageEvents(EventRepository repo, ContentTransaction tx, PublicationReferences refs) {
        this.repo = repo;
        this.tx = tx;
        this.refs = refs;
    }

    public EventRepository.Slice list(String status, int page, int size, boolean publicOnly) {
        paging(page, size);
        String filter = publicOnly ? "PUBLISHED" : ("ALL".equals(status) ? null : status);
        if (filter != null && !Set.of("DRAFT", "PUBLISHED", "HIDDEN").contains(filter)) throw new EventException(EventError.INVALID_EVENT_STATUS);
        return tx.read(() -> repo.list(filter, null, page, size));
    }

    public EventRepository.Slice listPublished(String contentType, int page, int size) {
        paging(page, size);
        if (contentType != null && !Set.of("POLICY", "BUSINESS").contains(contentType)) {
            throw new EventException(EventError.INVALID_CONTENT_TYPE);
        }
        return tx.read(() -> repo.list("PUBLISHED", contentType, page, size));
    }

    public EventView detail(long id, boolean publicOnly) {
        return tx.read(() -> {
            var event = find(id);
            if (publicOnly && !event.status().equals("PUBLISHED")) throw missing();
            var view = view(event);
            if (!publicOnly) return view;
            return new EventView(
                event,
                view.overviewSources().stream().filter(Citation::usable).toList(),
                view
                    .milestones()
                    .stream()
                    .filter(m -> m.milestone().status().equals("PUBLISHED"))
                    .map(m ->
                        new EventView.MilestoneView(
                            m.milestone(),
                            m.sources().stream().filter(Citation::usable).toList()
                        )
                    )
                    .toList()
            );
        });
    }

    public EventView create(EventDraft draft) {
        var clean = eventDraft(draft);
        return tx.write(() -> {
            refs.lock();
            return view(repo.create(clean.title(), clean.contentType(), clean.overview()));
        });
    }

    public EventView edit(long id, long version, EventDraft draft) {
        var clean = eventDraft(draft);
        return change(id, version, event -> {
            if (event.status().equals("PUBLISHED") && clean.overview().isBlank()) throw new EventException(EventError.EVENT_OVERVIEW_REQUIRED);
            repo.edit(id, clean.title(), clean.contentType(), clean.overview());
        });
    }

    public EventView publish(long id, long version) {
        return tx.write(() -> {
            refs.lock();
            EconomicEvent event = find(id);
            checkVersion(event, version);
            if (event.status().equals("PUBLISHED")) {
                throw new EventException(EventError.EVENT_ALREADY_PUBLISHED);
            }

            checkReadyForPublication(view(event));
            repo.status(id, "PUBLISHED", null);
            if (!repo.bump(id, version)) throw conflict();
            return view(find(id));
        });
    }

    public EventView hide(long id, long version, String reason) {
        String clean = text(reason, 1000, true, "Lý do ẩn");
        return change(id, version, event -> {
            if (!event.status().equals("PUBLISHED")) throw new EventException(EventError.EVENT_NOT_PUBLISHED);
            repo.status(id, "HIDDEN", clean);
        });
    }

    public EventView createMilestone(long eventId, long version, MilestoneDraft draft) {
        var clean = milestoneDraft(draft);
        return change(eventId, version, event -> {
            if (repo.milestones(eventId).size() >= 100) throw new EventException(EventError.MILESTONE_LIMIT);
            repo.createMilestone(
                eventId,
                clean.title(),
                clean.description(),
                clean.datePrecision(),
                clean.dateYear(),
                clean.dateMonth(),
                clean.dateDay()
            );
        });
    }

    public EventView editMilestone(
        long eventId,
        long milestoneId,
        long version,
        MilestoneDraft draft
    ) {
        var clean = milestoneDraft(draft);
        return change(eventId, version, event -> {
            var milestone = milestone(eventId, milestoneId);
            if (
                milestone.status().equals("PUBLISHED") && clean.description().isBlank()
            ) throw new EventException(EventError.MILESTONE_CONTENT_REQUIRED);
            repo.editMilestone(
                milestoneId,
                clean.title(),
                clean.description(),
                clean.datePrecision(),
                clean.dateYear(),
                clean.dateMonth(),
                clean.dateDay()
            );
        });
    }

    public EventView milestoneStatus(
        long eventId,
        long milestoneId,
        long version,
        boolean publish
    ) {
        return change(eventId, version, event -> {
            var milestone = milestone(eventId, milestoneId);
            if (publish) validateMilestone(milestone);
            String status = publish ? "PUBLISHED" : "DRAFT";
            if (milestone.status().equals(status)) throw new EventException(EventError.INVALID_MILESTONE_STATE);
            repo.milestoneStatus(milestoneId, status);
        });
    }

    public EventView sources(
        long eventId,
        Long milestoneId,
        long version,
        List<Long> articleIds,
        List<Long> documentIds
    ) {
        if (
            articleIds == null || documentIds == null || articleIds.size() + documentIds.size() > 50
        ) throw new EventException(EventError.TOO_MANY_SOURCES);
        var articles = ids(articleIds);
        var documents = ids(documentIds);
        return change(eventId, version, event -> {
            if (milestoneId != null) milestone(eventId, milestoneId);
            for (long id : articles)
                if (!repo.usableArticle(id)) throw new EventException(EventError.ARTICLE_NOT_USABLE);
            for (long id : documents)
                if (!repo.usableDocument(id)) throw new EventException(EventError.DOCUMENT_NOT_USABLE);
            repo.replaceCitations(eventId, milestoneId, articles, documents);
        });
    }

    private EventView change(long id, long version, Consumer<EconomicEvent> work) {
        return tx.write(() -> {
            refs.lock();
            var event = find(id);
            checkVersion(event, version);
            work.accept(event);
            EventView updated = view(find(id));
            if (updated.event().status().equals("PUBLISHED")) {
                checkReadyForPublication(updated);
            } else {
                checkPublishedMilestones(updated);
            }
            if (!repo.bump(id, version)) throw conflict();
            return new EventView(find(id), updated.overviewSources(), updated.milestones());
        });
    }

    private void checkVersion(EconomicEvent event, long version) {
        if (version < 0) throw new EventException(EventError.INVALID_VERSION);
        if (event.version() != version) throw conflict();
    }

    private void checkReadyForPublication(EventView view) {
        checkPublishedMilestones(view);
        if (view.event().overview().isBlank()) {
            throw new EventException(EventError.EVENT_OVERVIEW_REQUIRED);
        }
        if (view.overviewSources().stream().noneMatch(Citation::usable)) {
            throw new EventException(EventError.EVENT_SOURCE_REQUIRED);
        }
        if (view.milestones().stream().noneMatch(m -> m.milestone().status().equals("PUBLISHED"))) {
            throw new EventException(EventError.EVENT_MILESTONE_REQUIRED);
        }
    }

    private void checkPublishedMilestones(EventView view) {
        for (var item : view.milestones()) {
            if (item.milestone().status().equals("PUBLISHED")) {
                checkMilestoneContent(item.milestone(), item.sources());
            }
        }
    }

    private void validateMilestone(Milestone milestone) {
        checkMilestoneContent(milestone, repo.citations(milestone.eventId(), milestone.id()));
    }

    private void checkMilestoneContent(Milestone milestone, List<Citation> sources) {
        if (milestone.description().isBlank()) {
            throw new EventException(EventError.MILESTONE_CONTENT_REQUIRED);
        }
        if (sources.stream().noneMatch(Citation::usable)) {
            throw new EventException(EventError.MILESTONE_SOURCE_REQUIRED);
        }
    }

    private EventView view(EconomicEvent event) {
        var sources = repo.milestoneCitations(event.id());
        return new EventView(
            event,
            repo.citations(event.id(), null),
            repo.milestones(event.id()).stream()
                .map(m -> new EventView.MilestoneView(m, sources.getOrDefault(m.id(), List.of())))
                .toList()
        );
    }

    private EconomicEvent find(long id) {
        return repo.find(id).orElseThrow(this::missing);
    }

    private Milestone milestone(long eventId, long id) {
        return repo.milestone(eventId, id).orElseThrow(this::missing);
    }

    private List<Long> ids(List<Long> values) {
        if (values.stream().anyMatch(id -> id == null || id <= 0)) throw new EventException(EventError.INVALID_SOURCE_ID);
        return List.copyOf(new LinkedHashSet<>(values));
    }

    private EventDraft eventDraft(EventDraft d) {
        if (
            d == null ||
            !Set.of("POLICY", "BUSINESS").contains(d.contentType() == null ? "" : d.contentType())
        ) throw new EventException(EventError.INVALID_CONTENT_TYPE);
        return new EventDraft(
            text(d.title(), 300, true, "Tiêu đề"),
            d.contentType(),
            text(d.overview(), 10000, false, "Tổng quan")
        );
    }

    private MilestoneDraft milestoneDraft(MilestoneDraft d) {
        if (d == null) throw new EventException(EventError.MILESTONE_DATA_REQUIRED);
        try {
            if (d.dateYear() < 1 || d.dateYear() > 9999) throw new IllegalArgumentException();
            switch (d.datePrecision() == null ? "" : d.datePrecision()) {
                case "YEAR" -> {
                    if (
                        d.dateMonth() != null || d.dateDay() != null
                    ) throw new IllegalArgumentException();
                }
                case "MONTH" -> {
                    if (d.dateDay() != null) throw new IllegalArgumentException();
                    YearMonth.of(d.dateYear(), d.dateMonth());
                }
                case "DAY" -> LocalDate.of(d.dateYear(), d.dateMonth(), d.dateDay());
                default -> throw new IllegalArgumentException();
            }
        } catch (RuntimeException e) {
            throw new EventException(EventError.INVALID_MILESTONE_DATE);
        }
        return new MilestoneDraft(
            text(d.title(), 300, true, "Tiêu đề mốc"),
            text(d.description(), 4000, false, "Nội dung mốc"),
            d.datePrecision(),
            d.dateYear(),
            d.dateMonth(),
            d.dateDay()
        );
    }

    private String text(String value, int max, boolean required, String field) {
        String clean = value == null ? "" : value.strip();
        if ((required && clean.isBlank()) || clean.length() > max) throw new EventException(EventError.INVALID_CONTENT, field);
        return clean;
    }

    private void paging(int page, int size) {
        if (page < 0 || size < 1 || size > 100) throw new EventException(EventError.INVALID_PAGINATION);
    }

    private EventException missing() {
        return new EventException(EventError.RESOURCE_NOT_FOUND);
    }

    private EventException conflict() {
        return new EventException(EventError.VERSION_CONFLICT);
    }
}
