package com.econnews.collection.adapter.out.rss;

import com.econnews.collection.application.port.FeedReader;
import com.econnews.collection.domain.model.FeedEntry;
import java.util.List;

public class HttpFeedReader implements FeedReader {

    private final SafeRssDownloader downloader;
    private final SecureFeedParser parser;

    public HttpFeedReader(SafeRssDownloader downloader, SecureFeedParser parser) {
        this.downloader = downloader;
        this.parser = parser;
    }

    public List<FeedEntry> read(String url) {
        return parser.parse(downloader.download(url));
    }
}
