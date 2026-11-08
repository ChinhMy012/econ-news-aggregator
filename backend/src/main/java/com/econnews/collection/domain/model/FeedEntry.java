package com.econnews.collection.domain.model;

import java.time.Instant;

public record FeedEntry(String title, String url, String summary, Instant publishedAt) {}
