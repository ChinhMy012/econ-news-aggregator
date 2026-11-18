package com.econnews.event.application.model;

public final class EventCommands {

    private EventCommands() {}

    public record EventDraft(String title, String contentType, String overview) {}

    public record MilestoneDraft(
        String title,
        String description,
        String datePrecision,
        int dateYear,
        Integer dateMonth,
        Integer dateDay
    ) {}
}
