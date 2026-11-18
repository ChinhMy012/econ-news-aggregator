package com.econnews.collection.adapter.in.web.response;

import com.econnews.collection.application.exception.CollectionError;
import com.econnews.shared.adapter.web.error.ErrorDefinition;
import org.springframework.http.HttpStatus;

public final class CollectionErrorResponses {

    private CollectionErrorResponses() {}

    public static ErrorDefinition from(CollectionError error) {
        return switch (error) {
            case COLLECTION_BUSY -> new ErrorDefinition(
                HttpStatus.TOO_MANY_REQUESTS,
                "COLLECTION_BUSY",
                "Hệ thống đang thu thập hai kênh. Vui lòng thử lại sau."
            );
            case RESOURCE_NOT_FOUND -> new ErrorDefinition(
                HttpStatus.NOT_FOUND,
                "RESOURCE_NOT_FOUND",
                "Không tìm thấy kênh RSS của nguồn."
            );
            case FEED_DISABLED -> new ErrorDefinition(
                HttpStatus.CONFLICT,
                "FEED_DISABLED",
                "Nguồn tin hoặc kênh RSS đang tắt."
            );
            case COLLECTION_ALREADY_RUNNING -> new ErrorDefinition(
                HttpStatus.CONFLICT,
                "COLLECTION_ALREADY_RUNNING",
                "Kênh đang được thu thập."
            );
            case COLLECTION_COOLDOWN -> new ErrorDefinition(
                HttpStatus.TOO_MANY_REQUESTS,
                "COLLECTION_COOLDOWN",
                "Vui lòng đợi thêm trước khi thu thập lại kênh."
            );
            case INVALID_PAGINATION -> new ErrorDefinition(
                HttpStatus.BAD_REQUEST,
                "INVALID_PAGINATION",
                "page phải từ 0; size từ 1 đến 100."
            );
        };
    }
}
