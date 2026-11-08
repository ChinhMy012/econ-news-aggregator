package com.econnews.source.application.usecase;

import com.econnews.source.application.exception.CatalogException;
import com.econnews.source.application.port.CatalogTransaction;
import com.econnews.source.domain.model.*;
import com.econnews.source.domain.repository.SourceCatalog;
import java.net.URI;
import java.util.Locale;

public class ManageSourceCatalog {

    private final SourceCatalog catalog;
    private final CatalogTransaction tx;

    public ManageSourceCatalog(SourceCatalog catalog, CatalogTransaction tx) {
        this.catalog = catalog;
        this.tx = tx;
    }

    private CatalogException missing() {
        return new CatalogException(
            404,
            "RESOURCE_NOT_FOUND",
            "Không tìm thấy nguồn tin hoặc kênh RSS."
        );
    }

    private CatalogException conflict() {
        return new CatalogException(
            409,
            "VERSION_CONFLICT",
            "Dữ liệu đã thay đổi. Hãy tải lại trước khi sửa."
        );
    }

    private void paging(int page, int size) {
        if (page < 0 || size < 1 || size > 100) throw new CatalogException(
            400,
            "INVALID_PAGINATION",
            "page phải từ 0; size từ 1 đến 100."
        );
    }

    public SourceCatalog.Slice<NewsSource> sources(int page, int size) {
        paging(page, size);
        return tx.execute(() -> catalog.sources(page, size));
    }

    public SourceCatalog.Slice<RssFeed> feeds(long sid, int page, int size) {
        paging(page, size);
        return tx.execute(() -> {
            source(sid);
            return catalog.feeds(sid, page, size);
        });
    }

    public NewsSource source(long id) {
        return catalog.source(id).orElseThrow(this::missing);
    }

    public RssFeed feed(long sid, long id) {
        return catalog.feed(sid, id).orElseThrow(this::missing);
    }

    public NewsSource createSource(String code, String name, String url, boolean enabled) {
        if (
            code == null || code.length() > 50 || !code.matches("[a-z0-9]+(-[a-z0-9]+)*")
        ) throw new CatalogException(
            400,
            "INVALID_SOURCE_CODE",
            "Mã nguồn gồm chữ thường, số và dấu gạch ngang; tối đa 50 ký tự."
        );
        String n = name(name),
            u = url(url);
        return tx.execute(() -> catalog.createSource(code, n, u, enabled));
    }

    public NewsSource updateSource(
        long id,
        String name,
        String url,
        boolean enabled,
        long version
    ) {
        String n = name(name),
            u = url(url);
        return tx.execute(() -> {
            source(id);
            return catalog.updateSource(id, n, u, enabled, version).orElseThrow(this::conflict);
        });
    }

    public RssFeed createFeed(long sid, String name, String url, boolean enabled) {
        String n = name(name),
            u = url(url);
        return tx.execute(() -> {
            source(sid);
            return catalog.createFeed(sid, n, u, enabled);
        });
    }

    public RssFeed updateFeed(
        long sid,
        long id,
        String name,
        String url,
        boolean enabled,
        long version
    ) {
        String n = name(name),
            u = url(url);
        return tx.execute(() -> {
            feed(sid, id);
            return catalog.updateFeed(sid, id, n, u, enabled, version).orElseThrow(this::conflict);
        });
    }

    private String name(String s) {
        if (s == null || s.isBlank() || s.strip().length() > 200) throw new CatalogException(
            400,
            "INVALID_NAME",
            "Tên bắt buộc và tối đa 200 ký tự."
        );
        return s.strip();
    }

    private String url(String s) {
        try {
            URI u = URI.create(s.strip());
            if (
                !"https".equalsIgnoreCase(u.getScheme()) ||
                u.getHost() == null ||
                u.getUserInfo() != null ||
                u.getFragment() != null ||
                (u.getPort() != -1 && u.getPort() != 443)
            ) throw new IllegalArgumentException();
            String p = u.getRawPath();
            String result =
                "https://" +
                u.getHost().toLowerCase(Locale.ROOT) +
                (p == null || p.isEmpty() ? "/" : p) +
                (u.getRawQuery() == null ? "" : "?" + u.getRawQuery());
            if (result.length() > 2048) throw new IllegalArgumentException();
            return result;
        } catch (RuntimeException e) {
            throw new CatalogException(
                400,
                "INVALID_URL",
                "URL phải là HTTPS hợp lệ, cổng mặc định, không chứa tài khoản hoặc fragment; tối đa 2048 ký tự."
            );
        }
    }
}
