package com.econnews.event.application;

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
        if (filter != null && !Set.of("DRAFT", "PUBLISHED", "HIDDEN").contains(filter)) throw error(
            400,
            "INVALID_EVENT_STATUS",
            "Trạng thái sự kiện không hợp lệ."
        );
        return tx.read(() -> repo.list(filter, page, size));
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
            if (event.status().equals("PUBLISHED") && clean.overview().isBlank()) throw error(
                409,
                "EVENT_OVERVIEW_REQUIRED",
                "Sự kiện công khai phải có tổng quan."
            );
            repo.edit(id, clean.title(), clean.contentType(), clean.overview());
        });
    }

    public EventView publish(long id, long version) {
        return change(id, version, event -> {
            if (event.status().equals("PUBLISHED")) throw error(
                409,
                "INVALID_EVENT_STATE",
                "Sự kiện đã công khai."
            );
            validate(event, true);
            repo.status(id, "PUBLISHED", null);
        });
    }

    public EventView hide(long id, long version, String reason) {
        String clean = text(reason, 1000, true, "Lý do ẩn");
        return change(id, version, event -> {
            if (!event.status().equals("PUBLISHED")) throw error(
                409,
                "INVALID_EVENT_STATE",
                "Chỉ ẩn sự kiện đang công khai."
            );
            repo.status(id, "HIDDEN", clean);
        });
    }

    public EventView createMilestone(long eventId, long version, MilestoneDraft draft) {
        var clean = milestoneDraft(draft);
        return change(eventId, version, event -> {
            if (repo.milestones(eventId).size() >= 100) throw error(
                400,
                "MILESTONE_LIMIT",
                "MVP hỗ trợ tối đa 100 mốc cho một sự kiện."
            );
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
            ) throw error(409, "MILESTONE_CONTENT_REQUIRED", "Mốc công khai phải có nội dung.");
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
            if (milestone.status().equals(status)) throw error(
                409,
                "INVALID_MILESTONE_STATE",
                "Mốc đã ở trạng thái được yêu cầu."
            );
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
        ) throw error(400, "INVALID_SOURCES", "Chọn tối đa 50 nguồn cho tổng quan hoặc một mốc.");
        var articles = ids(articleIds);
        var documents = ids(documentIds);
        return change(eventId, version, event -> {
            if (milestoneId != null) milestone(eventId, milestoneId);
            for (long id : articles)
                if (!repo.usableArticle(id)) throw error(
                    409,
                    "SOURCE_NOT_USABLE",
                    "Bài làm nguồn phải tồn tại và đã công khai."
                );
            for (long id : documents)
                if (!repo.usableDocument(id)) throw error(
                    409,
                    "SOURCE_NOT_USABLE",
                    "Tài liệu làm nguồn phải tồn tại và đang bật."
                );
            repo.replaceCitations(eventId, milestoneId, articles, documents);
        });
    }

    private EventView change(long id, long version, Consumer<EconomicEvent> work) {
        return tx.write(() -> {
            refs.lock();
            var event = find(id);
            if (version < 0) throw error(400, "INVALID_VERSION", "version phải từ 0.");
            if (event.version() != version) throw conflict();
            work.accept(event);
            validate(find(id), false);
            if (!repo.bump(id, version)) throw conflict();
            return view(find(id));
        });
    }

    private void validate(EconomicEvent event, boolean forcePublished) {
        var milestones = repo.milestones(event.id());
        for (var milestone : milestones)
            if (milestone.status().equals("PUBLISHED")) validateMilestone(milestone);
        if (forcePublished || event.status().equals("PUBLISHED")) {
            if (event.overview().isBlank()) throw error(
                409,
                "EVENT_OVERVIEW_REQUIRED",
                "Sự kiện công khai phải có tổng quan."
            );
            if (repo.citations(event.id(), null).stream().noneMatch(Citation::usable)) throw error(
                409,
                "EVENT_SOURCE_REQUIRED",
                "Tổng quan sự kiện phải có ít nhất một nguồn sử dụng được."
            );
            if (milestones.stream().noneMatch(m -> m.status().equals("PUBLISHED"))) throw error(
                409,
                "EVENT_MILESTONE_REQUIRED",
                "Sự kiện phải có ít nhất một mốc công khai."
            );
        }
    }

    private void validateMilestone(Milestone milestone) {
        if (milestone.description().isBlank()) throw error(
            409,
            "MILESTONE_CONTENT_REQUIRED",
            "Mốc công khai phải có nội dung."
        );
        if (
            repo.citations(milestone.eventId(), milestone.id()).stream().noneMatch(Citation::usable)
        ) throw error(
            409,
            "MILESTONE_SOURCE_REQUIRED",
            "Mốc công khai phải có ít nhất một nguồn sử dụng được."
        );
    }

    private EventView view(EconomicEvent event) {
        return new EventView(
            event,
            repo.citations(event.id(), null),
            repo
                .milestones(event.id())
                .stream()
                .map(m -> new EventView.MilestoneView(m, repo.citations(event.id(), m.id())))
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
        if (values.stream().anyMatch(id -> id == null || id <= 0)) throw error(
            400,
            "INVALID_SOURCES",
            "Mã nguồn tham chiếu phải là số dương."
        );
        return List.copyOf(new LinkedHashSet<>(values));
    }

    private EventDraft eventDraft(EventDraft d) {
        if (
            d == null ||
            !Set.of("POLICY", "BUSINESS").contains(d.contentType() == null ? "" : d.contentType())
        ) throw error(400, "INVALID_CONTENT_TYPE", "Phân loại chỉ nhận POLICY hoặc BUSINESS.");
        return new EventDraft(
            text(d.title(), 300, true, "Tiêu đề"),
            d.contentType(),
            text(d.overview(), 10000, false, "Tổng quan")
        );
    }

    private MilestoneDraft milestoneDraft(MilestoneDraft d) {
        if (d == null) throw error(400, "INVALID_MILESTONE_DATE", "Thiếu thông tin mốc.");
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
            throw error(
                400,
                "INVALID_MILESTONE_DATE",
                "Thời gian mốc không hợp lệ với độ chính xác đã chọn."
            );
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
        if ((required && clean.isBlank()) || clean.length() > max) throw error(
            400,
            "INVALID_CONTENT",
            field + " không hợp lệ hoặc vượt độ dài cho phép."
        );
        return clean;
    }

    private void paging(int page, int size) {
        if (page < 0 || size < 1 || size > 100) throw error(
            400,
            "INVALID_PAGINATION",
            "page phải từ 0; size từ 1 đến 100."
        );
    }

    private EventException missing() {
        return error(404, "RESOURCE_NOT_FOUND", "Không tìm thấy sự kiện hoặc mốc thuộc sự kiện.");
    }

    private EventException conflict() {
        return error(
            409,
            "VERSION_CONFLICT",
            "Sự kiện đã thay đổi. Hãy tải lại dữ liệu trước khi thao tác."
        );
    }

    private EventException error(int status, String code, String message) {
        return new EventException(status, code, message);
    }
}
