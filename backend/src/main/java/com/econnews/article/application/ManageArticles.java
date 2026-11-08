package com.econnews.article.application;

import com.econnews.article.application.exception.ArticleException;
import com.econnews.article.application.port.*;
import com.econnews.article.domain.model.*;
import com.econnews.article.domain.repository.ArticleModerationRepository;
import com.econnews.shared.application.port.PublicationReferences;

public class ManageArticles {

    private final ArticleQuery query;
    private final ArticleModerationRepository moderation;
    private final ArticleTransaction tx;
    private final PublicationReferences references;

    public ManageArticles(
        ArticleQuery query,
        ArticleModerationRepository moderation,
        ArticleTransaction tx,
        PublicationReferences references
    ) {
        this.query = query;
        this.moderation = moderation;
        this.tx = tx;
        this.references = references;
    }

    public ArticleQuery.Slice adminList(String status, int page, int size) {
        paging(page, size);
        ArticleStatus filter = null;
        if (!"ALL".equals(status)) {
            try {
                filter = ArticleStatus.valueOf(status);
            } catch (RuntimeException e) {
                throw new ArticleException(
                    400,
                    "INVALID_ARTICLE_STATUS",
                    "Trạng thái bài viết không hợp lệ."
                );
            }
        }
        return query.list(filter, null, page, size);
    }

    public ArticleQuery.Slice publishedList(String contentType, int page, int size) {
        paging(page, size);
        return query.list(
            ArticleStatus.PUBLISHED,
            contentType == null ? null : type(contentType),
            page,
            size
        );
    }

    public ArticleDetails detail(long id) {
        return query.find(id).orElseThrow(this::missing);
    }

    public ArticleDetails publishedDetail(long id) {
        var article = detail(id);
        if (article.status() != ArticleStatus.PUBLISHED) throw missing();
        return article;
    }

    public ArticleDetails classify(long id, String contentType, long version) {
        ContentType type = type(contentType);
        return tx.execute(() -> {
            var article = current(id, version);
            if (article.status() != ArticleStatus.PENDING) throw invalidState();
            return save(article, ArticleStatus.PENDING, type, null);
        });
    }

    public ArticleDetails publish(long id, long version) {
        return tx.execute(() -> {
            var article = current(id, version);
            transition(article, ArticleStatus.PUBLISHED);
            if (article.contentType() == null) throw new ArticleException(
                400,
                "CONTENT_TYPE_REQUIRED",
                "Hãy lưu phân loại trước khi duyệt bài."
            );
            return save(article, ArticleStatus.PUBLISHED, article.contentType(), null);
        });
    }

    public ArticleDetails reject(long id, String reason, long version) {
        return conclude(id, reason, version, ArticleStatus.REJECTED);
    }

    public ArticleDetails hide(long id, String reason, long version) {
        return conclude(id, reason, version, ArticleStatus.HIDDEN);
    }

    private ArticleDetails conclude(long id, String reason, long version, ArticleStatus target) {
        if (
            reason == null || reason.isBlank() || reason.strip().length() > 1000
        ) throw new ArticleException(
            400,
            "MODERATION_REASON_REQUIRED",
            "Lý do bắt buộc và tối đa 1000 ký tự."
        );
        String clean = reason.strip();
        return tx.execute(() -> {
            if (target == ArticleStatus.HIDDEN) references.lock();
            var article = current(id, version);
            transition(article, target);
            if (target == ArticleStatus.HIDDEN && references.articleIsEssential(id)) {
                throw new ArticleException(
                    409,
                    "SOURCE_REQUIRED",
                    "Không thể ẩn bài là nguồn cuối cùng của nội dung công khai."
                );
            }
            return save(article, target, article.contentType(), clean);
        });
    }

    private ArticleDetails current(long id, long version) {
        if (version < 0) throw new ArticleException(400, "INVALID_VERSION", "version phải từ 0.");
        var article = detail(id);
        if (article.version() != version) throw conflict();
        return article;
    }

    private ArticleDetails save(
        ArticleDetails article,
        ArticleStatus status,
        ContentType contentType,
        String reason
    ) {
        if (
            !moderation.update(
                article.id(),
                article.version(),
                article.status(),
                status,
                contentType,
                reason
            )
        ) throw conflict();
        return detail(article.id());
    }

    private void transition(ArticleDetails article, ArticleStatus target) {
        if (!article.status().canTransitionTo(target)) throw invalidState();
    }

    private ContentType type(String value) {
        try {
            return ContentType.valueOf(value);
        } catch (RuntimeException e) {
            throw new ArticleException(
                400,
                "INVALID_CONTENT_TYPE",
                "Phân loại chỉ nhận POLICY hoặc BUSINESS."
            );
        }
    }

    private void paging(int page, int size) {
        if (page < 0 || size < 1 || size > 100) throw new ArticleException(
            400,
            "INVALID_PAGINATION",
            "page phải từ 0; size từ 1 đến 100."
        );
    }

    private ArticleException missing() {
        return new ArticleException(404, "RESOURCE_NOT_FOUND", "Không tìm thấy bài viết.");
    }

    private ArticleException conflict() {
        return new ArticleException(
            409,
            "VERSION_CONFLICT",
            "Bài viết đã thay đổi. Hãy tải lại dữ liệu trước khi thao tác."
        );
    }

    private ArticleException invalidState() {
        return new ArticleException(
            409,
            "INVALID_ARTICLE_STATE",
            "Không thể thực hiện thao tác với trạng thái hiện tại của bài viết."
        );
    }
}
