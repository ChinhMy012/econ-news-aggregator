package com.econnews.event.adapter.in.web.response;

import com.econnews.event.application.model.EventView;
import com.econnews.event.domain.model.*;
import java.time.Instant;
import java.util.*;

public final class EventResponses {

    private EventResponses() {}

    public record AdminSummary(
        long id,
        String title,
        String contentType,
        String overview,
        String status,
        String hiddenReason,
        long version,
        Instant updatedAt
    ) {
        public static AdminSummary from(EconomicEvent e) {
            return new AdminSummary(
                e.id(),
                e.title(),
                e.contentType(),
                e.overview(),
                e.status(),
                e.hiddenReason(),
                e.version(),
                e.updatedAt()
            );
        }
    }

    public record AdminSource(
        long id,
        String kind,
        String title,
        String url,
        String publisher,
        boolean usable
    ) {
        public static AdminSource from(Citation c) {
            return new AdminSource(c.id(), c.kind(), c.title(), c.url(), c.publisher(), c.usable());
        }
    }

    public record AdminMilestone(
        long id,
        String title,
        String description,
        String datePrecision,
        int dateYear,
        Integer dateMonth,
        Integer dateDay,
        String status,
        List<AdminSource> sources
    ) {
        public static AdminMilestone from(EventView.MilestoneView v) {
            var m = v.milestone();
            return new AdminMilestone(
                m.id(),
                m.title(),
                m.description(),
                m.datePrecision(),
                m.dateYear(),
                m.dateMonth(),
                m.dateDay(),
                m.status(),
                v.sources().stream().map(AdminSource::from).toList()
            );
        }
    }

    public record AdminDetail(
        AdminSummary event,
        List<AdminSource> overviewSources,
        List<AdminMilestone> milestones
    ) {
        public static AdminDetail from(EventView v) {
            return new AdminDetail(
                AdminSummary.from(v.event()),
                v.overviewSources().stream().map(AdminSource::from).toList(),
                v.milestones().stream().map(AdminMilestone::from).toList()
            );
        }
    }

    public record PublicSummary(
        long id,
        String title,
        String contentType,
        String overview,
        Instant updatedAt
    ) {
        public static PublicSummary from(EconomicEvent e) {
            return new PublicSummary(
                e.id(),
                e.title(),
                e.contentType(),
                e.overview(),
                e.updatedAt()
            );
        }
    }

    public record Source(long id, String kind, String title, String url, String publisher) {
        public static Source from(Citation c) {
            return new Source(c.id(), c.kind(), c.title(), c.url(), c.publisher());
        }
    }

    public record PublicMilestone(
        long id,
        String title,
        String description,
        String datePrecision,
        int dateYear,
        Integer dateMonth,
        Integer dateDay,
        List<Source> sources
    ) {
        public static PublicMilestone from(EventView.MilestoneView v) {
            var m = v.milestone();
            return new PublicMilestone(
                m.id(),
                m.title(),
                m.description(),
                m.datePrecision(),
                m.dateYear(),
                m.dateMonth(),
                m.dateDay(),
                v.sources().stream().map(Source::from).toList()
            );
        }
    }

    public record PublicDetail(
        PublicSummary event,
        List<Source> overviewSources,
        List<PublicMilestone> milestones
    ) {
        public static PublicDetail from(EventView v) {
            return new PublicDetail(
                PublicSummary.from(v.event()),
                v.overviewSources().stream().map(Source::from).toList(),
                v.milestones().stream().map(PublicMilestone::from).toList()
            );
        }

        public List<Source> sources(String kind) {
            Map<Long, Source> distinct = new LinkedHashMap<>();
            for (var source : overviewSources)
                if (source.kind().equals(kind)) distinct.put(source.id(), source);
            for (var m : milestones)
                for (var source : m.sources())
                    if (source.kind().equals(kind)) distinct.put(source.id(), source);
            return List.copyOf(distinct.values());
        }
    }
}
