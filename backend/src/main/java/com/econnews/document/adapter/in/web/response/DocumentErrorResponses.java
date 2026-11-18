package com.econnews.document.adapter.in.web.response;

import com.econnews.document.application.exception.DocumentError;
import com.econnews.shared.adapter.web.error.ErrorDefinition;
import org.springframework.http.HttpStatus;

public final class DocumentErrorResponses {

    public static final ErrorDefinition DUPLICATE = new ErrorDefinition(
        HttpStatus.CONFLICT,
        "DOCUMENT_DUPLICATE",
        "URL tài liệu đã tồn tại."
    );

    private DocumentErrorResponses() {}

    public static ErrorDefinition from(DocumentError error) {
        return switch (error) {
            case INVALID_PAGINATION -> new ErrorDefinition(
                HttpStatus.BAD_REQUEST,
                "INVALID_PAGINATION",
                "page phải từ 0; size từ 1 đến 100."
            );
            case RESOURCE_NOT_FOUND -> new ErrorDefinition(
                HttpStatus.NOT_FOUND,
                "RESOURCE_NOT_FOUND",
                "Không tìm thấy tài liệu."
            );
            case VERSION_CONFLICT -> new ErrorDefinition(
                HttpStatus.CONFLICT,
                "VERSION_CONFLICT",
                "Tài liệu đã thay đổi. Hãy tải lại dữ liệu."
            );
            case SOURCE_REQUIRED -> new ErrorDefinition(
                HttpStatus.CONFLICT,
                "SOURCE_REQUIRED",
                "Không thể tắt tài liệu là nguồn cuối cùng của nội dung công khai."
            );
            case INVALID_DOCUMENT -> new ErrorDefinition(
                HttpStatus.BAD_REQUEST,
                "INVALID_DOCUMENT",
                "Tên và cơ quan ban hành bắt buộc, trong giới hạn độ dài."
            );
            case INVALID_DOCUMENT_URL -> new ErrorDefinition(
                HttpStatus.BAD_REQUEST,
                "INVALID_DOCUMENT_URL",
                "URL tài liệu phải là HTTPS hợp lệ, tối đa 2048 ký tự và không chứa tài khoản/fragment."
            );
        };
    }
}
