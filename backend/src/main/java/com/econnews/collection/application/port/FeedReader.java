package com.econnews.collection.application.port;

import com.econnews.collection.domain.model.FeedEntry;
import java.util.List;

public interface FeedReader {
    List<FeedEntry> read(String url);
}
