package com.econnews.source.domain.repository;

import com.econnews.source.domain.model.*;
import java.util.*;

public interface SourceCatalog {
    record Slice<T>(List<T> items, long total) {}

    Slice<NewsSource> sources(int page, int size);
    Slice<RssFeed> feeds(long sourceId, int page, int size);
    Optional<NewsSource> source(long id);
    Optional<RssFeed> feed(long sourceId, long id);
    NewsSource createSource(String code, String name, String url, boolean enabled);
    Optional<NewsSource> updateSource(
        long id,
        String name,
        String url,
        boolean enabled,
        long version
    );
    RssFeed createFeed(long sourceId, String name, String url, boolean enabled);
    Optional<RssFeed> updateFeed(
        long sourceId,
        long id,
        String name,
        String url,
        boolean enabled,
        long version
    );
}
