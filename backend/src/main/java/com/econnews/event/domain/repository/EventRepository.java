package com.econnews.event.domain.repository;

import com.econnews.event.domain.model.*;
import java.util.*;

public interface EventRepository {
    record Slice(List<EconomicEvent> items, long total) {}

    Slice list(String status, String contentType, int page, int size);
    Optional<EconomicEvent> find(long id);
    EconomicEvent create(String title, String contentType, String overview);
    void edit(long id, String title, String contentType, String overview);
    boolean bump(long id, long version);
    void status(long id, String status, String reason);
    List<Milestone> milestones(long eventId);
    Optional<Milestone> milestone(long eventId, long id);
    long createMilestone(
        long eventId,
        String title,
        String description,
        String precision,
        int year,
        Integer month,
        Integer day
    );
    void editMilestone(
        long id,
        String title,
        String description,
        String precision,
        int year,
        Integer month,
        Integer day
    );
    void milestoneStatus(long id, String status);
    boolean usableArticle(long id);
    boolean usableDocument(long id);
    Map<Long, List<Citation>> milestoneCitations(long eventId);

    List<Citation> citations(long eventId, Long milestoneId);
    void replaceCitations(
        long eventId,
        Long milestoneId,
        List<Long> articleIds,
        List<Long> documentIds
    );
}
