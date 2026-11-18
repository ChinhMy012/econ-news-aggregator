package com.econnews.article.adapter.in.web.response;

import com.econnews.article.application.exception.ArticleError;
import com.econnews.shared.adapter.web.error.ErrorDefinition;
import org.springframework.http.HttpStatus;

public final class ArticleErrorResponses {

    private ArticleErrorResponses() {}

    public static ErrorDefinition from(ArticleError error) {
        return switch (error) {
            case INVALID_ARTICLE_STATUS -> new ErrorDefinition(
                HttpStatus.BAD_REQUEST,
                "INVALID_ARTICLE_STATUS",
                "Trạng thái bài viết không hợp lệ."
            );
            case CONTENT_TYPE_REQUIRED -> new ErrorDefinition(
                HttpStatus.BAD_REQUEST,
                "CONTENT_TYPE_REQUIRED",
                "Hãy lưu phân loại trước khi duyệt bài."
            );
            case MODERATION_REASON_REQUIRED -> new ErrorDefinition(
                HttpStatus.BAD_REQUEST,
                "MODERATION_REASON_REQUIRED",
                "Lý do bắt buộc và tối đa 1000 ký tự."
            );
            case SOURCE_REQUIRED -> new ErrorDefinition(
                HttpStatus.CONFLICT,
                "SOURCE_REQUIRED",
                "Không thể ẩn bài là nguồn cuối cùng của nội dung công khai."
            );
            case INVALID_VERSION -> new ErrorDefinition(
                HttpStatus.BAD_REQUEST,
                "INVALID_VERSION",
                "version phải từ 0."
            );
            case INVALID_CONTENT_TYPE -> new ErrorDefinition(
                HttpStatus.BAD_REQUEST,
                "INVALID_CONTENT_TYPE",
                "Phân loại chỉ nhận POLICY hoặc BUSINESS."
            );
            case INVALID_PAGINATION -> new ErrorDefinition(
                HttpStatus.BAD_REQUEST,
                "INVALID_PAGINATION",
                "page phải từ 0; size từ 1 đến 100."
            );
            case RESOURCE_NOT_FOUND -> new ErrorDefinition(
                HttpStatus.NOT_FOUND,
                "RESOURCE_NOT_FOUND",
                "Không tìm thấy bài viết."
            );
            case VERSION_CONFLICT -> new ErrorDefinition(
                HttpStatus.CONFLICT,
                "VERSION_CONFLICT",
                "Bài viết đã thay đổi. Hãy tải lại dữ liệu trước khi thao tác."
            );
            case INVALID_ARTICLE_STATE -> new ErrorDefinition(
                HttpStatus.CONFLICT,
                "INVALID_ARTICLE_STATE",
                "Không thể thực hiện thao tác với trạng thái hiện tại của bài viết."
            );
        };
    }
}
