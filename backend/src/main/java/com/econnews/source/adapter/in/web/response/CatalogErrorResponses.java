package com.econnews.source.adapter.in.web.response;

import com.econnews.source.application.exception.CatalogError;
import com.econnews.shared.adapter.web.error.ErrorDefinition;
import org.springframework.http.HttpStatus;

public final class CatalogErrorResponses {

    public static final ErrorDefinition DUPLICATE = new ErrorDefinition(
        HttpStatus.CONFLICT,
        "CATALOG_DUPLICATE",
        "Mã nguồn hoặc URL kênh RSS đã tồn tại."
    );

    private CatalogErrorResponses() {}

    public static ErrorDefinition from(CatalogError error) {
        return switch (error) {
            case RESOURCE_NOT_FOUND -> new ErrorDefinition(
                HttpStatus.NOT_FOUND,
                "RESOURCE_NOT_FOUND",
                "Không tìm thấy nguồn tin hoặc kênh RSS."
            );
            case VERSION_CONFLICT -> new ErrorDefinition(
                HttpStatus.CONFLICT,
                "VERSION_CONFLICT",
                "Dữ liệu đã thay đổi. Hãy tải lại trước khi sửa."
            );
            case INVALID_PAGINATION -> new ErrorDefinition(
                HttpStatus.BAD_REQUEST,
                "INVALID_PAGINATION",
                "page phải từ 0; size từ 1 đến 100."
            );
            case INVALID_SOURCE_CODE -> new ErrorDefinition(
                HttpStatus.BAD_REQUEST,
                "INVALID_SOURCE_CODE",
                "Mã nguồn gồm chữ thường, số và dấu gạch ngang; tối đa 50 ký tự."
            );
            case INVALID_NAME -> new ErrorDefinition(
                HttpStatus.BAD_REQUEST,
                "INVALID_NAME",
                "Tên bắt buộc và tối đa 200 ký tự."
            );
            case INVALID_URL -> new ErrorDefinition(
                HttpStatus.BAD_REQUEST,
                "INVALID_URL",
                "URL phải là HTTPS hợp lệ, cổng mặc định, không chứa tài khoản hoặc fragment; tối đa 2048 ký tự."
            );
        };
    }
}
