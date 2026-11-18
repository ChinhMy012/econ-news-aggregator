package com.econnews.event.domain.model;

public record Milestone(
    long id,
    long eventId,
    String title,
    String description,
    String datePrecision,
    int dateYear,
    Integer dateMonth,
    Integer dateDay,
    String status
) {}
