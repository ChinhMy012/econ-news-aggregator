package com.econnews.event.domain.model;

public record Citation(
    long id,
    String kind,
    String title,
    String url,
    String publisher,
    boolean usable
) {}
