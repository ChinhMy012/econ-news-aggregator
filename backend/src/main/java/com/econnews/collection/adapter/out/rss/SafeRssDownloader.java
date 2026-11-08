package com.econnews.collection.adapter.out.rss;

import com.econnews.collection.application.exception.FeedReadException;
import java.io.*;
import java.net.URI;
import java.util.Locale;
import java.util.concurrent.*;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.config.*;
import org.apache.hc.client5.http.impl.classic.*;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.util.Timeout;

public class SafeRssDownloader implements AutoCloseable {

    private static final int MAX_BYTES = 2 * 1024 * 1024;
    private final CloseableHttpClient client;
    private final ExecutorService workers;

    public SafeRssDownloader() {
        var manager = PoolingHttpClientConnectionManagerBuilder.create()
            .setDnsResolver(new PublicDnsResolver())
            .setMaxConnTotal(2)
            .setMaxConnPerRoute(2)
            .setDefaultConnectionConfig(
                ConnectionConfig.custom()
                    .setConnectTimeout(Timeout.ofSeconds(3))
                    .setSocketTimeout(Timeout.ofSeconds(5))
                    .build()
            )
            .build();
        client = HttpClients.custom()
            .setConnectionManager(manager)
            .setDefaultRequestConfig(
                RequestConfig.custom()
                    .setConnectionRequestTimeout(Timeout.ofSeconds(1))
                    .setResponseTimeout(Timeout.ofSeconds(5))
                    .build()
            )
            .disableRedirectHandling()
            .disableAutomaticRetries()
            .disableCookieManagement()
            .disableContentCompression()
            .setUserAgent("EconNewsPersonalRSS/0.1")
            .build();
        workers = new ThreadPoolExecutor(
            2,
            2,
            0,
            TimeUnit.MILLISECONDS,
            new SynchronousQueue<>(),
            task -> {
                Thread thread = new Thread(task, "rss-download");
                thread.setDaemon(true);
                return thread;
            },
            new ThreadPoolExecutor.AbortPolicy()
        );
    }

    public byte[] download(String url) {
        URI uri;
        try {
            uri = URI.create(url);
            String host = uri.getHost();
            if (
                !"https".equalsIgnoreCase(uri.getScheme()) ||
                host == null ||
                !host.contains(".") ||
                host.contains(":") ||
                host.matches("[0-9.]+") ||
                uri.getUserInfo() != null ||
                uri.getFragment() != null ||
                (uri.getPort() != -1 && uri.getPort() != 443)
            ) throw new IllegalArgumentException();
        } catch (RuntimeException e) {
            throw new FeedReadException("RSS_URL_BLOCKED");
        }
        HttpGet request = new HttpGet(uri);
        request.setHeader(
            "Accept",
            "application/rss+xml, application/atom+xml, application/xml, text/xml"
        );
        request.setHeader("Accept-Encoding", "identity");
        Future<byte[]> future;
        try {
            future = workers.submit(() ->
                client.execute(request, response -> {
                    if (response.getCode() != 200) throw new FeedReadException(
                        response.getCode() >= 300 && response.getCode() < 400
                            ? "RSS_REDIRECT_BLOCKED"
                            : "RSS_HTTP_ERROR"
                    );
                    var entity = response.getEntity();
                    if (entity == null) throw new FeedReadException("RSS_EMPTY_RESPONSE");
                    String type = entity.getContentType();
                    if (
                        type != null &&
                        !type.toLowerCase(Locale.ROOT).contains("xml") &&
                        !type.toLowerCase(Locale.ROOT).contains("rss") &&
                        !type.toLowerCase(Locale.ROOT).startsWith("text/plain")
                    ) throw new FeedReadException("RSS_CONTENT_TYPE_INVALID");
                    if (entity.getContentLength() > MAX_BYTES) throw new FeedReadException(
                        "RSS_TOO_LARGE"
                    );
                    try (InputStream input = entity.getContent()) {
                        byte[] data = input.readNBytes(MAX_BYTES + 1);
                        if (data.length > MAX_BYTES) throw new FeedReadException("RSS_TOO_LARGE");
                        return data;
                    }
                })
            );
        } catch (RejectedExecutionException e) {
            throw new FeedReadException("RSS_DOWNLOADER_BUSY");
        }
        try {
            return future.get(10, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            request.cancel();
            future.cancel(true);
            throw new FeedReadException("RSS_TIMEOUT");
        } catch (InterruptedException e) {
            request.cancel();
            future.cancel(true);
            Thread.currentThread().interrupt();
            throw new FeedReadException("RSS_INTERRUPTED");
        } catch (ExecutionException e) {
            if (e.getCause() instanceof FeedReadException failure) throw failure;
            throw new FeedReadException("RSS_FETCH_FAILED");
        }
    }

    @Override
    public void close() throws IOException {
        workers.shutdownNow();
        client.close();
    }
}
