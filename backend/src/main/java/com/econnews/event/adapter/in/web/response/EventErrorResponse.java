package com.econnews.event.adapter.in.web.response;

import com.econnews.event.application.exception.EventException;
import com.econnews.shared.adapter.web.error.ErrorDefinition;
import org.springframework.http.HttpStatus;

public final class EventErrorResponse {

    private EventErrorResponse() {}

    public static ErrorDefinition from(EventException e) {
        return switch (e.error()) {
            case INVALID_EVENT_STATUS -> new ErrorDefinition(
                HttpStatus.BAD_REQUEST,
                "INVALID_EVENT_STATUS",
                "Trạng thái sự kiện không hợp lệ."
            );
            case EVENT_OVERVIEW_REQUIRED -> new ErrorDefinition(
                HttpStatus.CONFLICT,
                "EVENT_OVERVIEW_REQUIRED",
                "Sự kiện công khai phải có tổng quan."
            );
            case EVENT_ALREADY_PUBLISHED -> new ErrorDefinition(
                HttpStatus.CONFLICT,
                "INVALID_EVENT_STATE",
                "Sự kiện đã công khai."
            );
            case EVENT_NOT_PUBLISHED -> new ErrorDefinition(
                HttpStatus.CONFLICT,
                "INVALID_EVENT_STATE",
                "Chỉ ẩn sự kiện đang công khai."
            );
            case MILESTONE_LIMIT -> new ErrorDefinition(
                HttpStatus.BAD_REQUEST,
                "MILESTONE_LIMIT",
                "MVP hỗ trợ tối đa 100 mốc cho một sự kiện."
            );
            case MILESTONE_CONTENT_REQUIRED -> new ErrorDefinition(
                HttpStatus.CONFLICT,
                "MILESTONE_CONTENT_REQUIRED",
                "Mốc công khai phải có nội dung."
            );
            case INVALID_MILESTONE_STATE -> new ErrorDefinition(
                HttpStatus.CONFLICT,
                "INVALID_MILESTONE_STATE",
                "Mốc đã ở trạng thái được yêu cầu."
            );
            case TOO_MANY_SOURCES -> new ErrorDefinition(
                HttpStatus.BAD_REQUEST,
                "INVALID_SOURCES",
                "Chọn tối đa 50 nguồn cho tổng quan hoặc một mốc."
            );
            case ARTICLE_NOT_USABLE -> new ErrorDefinition(
                HttpStatus.CONFLICT,
                "SOURCE_NOT_USABLE",
                "Bài làm nguồn phải tồn tại và đã công khai."
            );
            case DOCUMENT_NOT_USABLE -> new ErrorDefinition(
                HttpStatus.CONFLICT,
                "SOURCE_NOT_USABLE",
                "Tài liệu làm nguồn phải tồn tại và đang bật."
            );
            case INVALID_VERSION -> new ErrorDefinition(
                HttpStatus.BAD_REQUEST,
                "INVALID_VERSION",
                "version phải từ 0."
            );
            case EVENT_SOURCE_REQUIRED -> new ErrorDefinition(
                HttpStatus.CONFLICT,
                "EVENT_SOURCE_REQUIRED",
                "Tổng quan sự kiện phải có ít nhất một nguồn sử dụng được."
            );
            case EVENT_MILESTONE_REQUIRED -> new ErrorDefinition(
                HttpStatus.CONFLICT,
                "EVENT_MILESTONE_REQUIRED",
                "Sự kiện phải có ít nhất một mốc công khai."
            );
            case MILESTONE_SOURCE_REQUIRED -> new ErrorDefinition(
                HttpStatus.CONFLICT,
                "MILESTONE_SOURCE_REQUIRED",
                "Mốc công khai phải có ít nhất một nguồn sử dụng được."
            );
            case INVALID_SOURCE_ID -> new ErrorDefinition(
                HttpStatus.BAD_REQUEST,
                "INVALID_SOURCES",
                "Mã nguồn tham chiếu phải là số dương."
            );
            case INVALID_CONTENT_TYPE -> new ErrorDefinition(
                HttpStatus.BAD_REQUEST,
                "INVALID_CONTENT_TYPE",
                "Phân loại chỉ nhận POLICY hoặc BUSINESS."
            );
            case MILESTONE_DATA_REQUIRED -> new ErrorDefinition(
                HttpStatus.BAD_REQUEST,
                "INVALID_MILESTONE_DATE",
                "Thiếu thông tin mốc."
            );
            case INVALID_MILESTONE_DATE -> new ErrorDefinition(
                HttpStatus.BAD_REQUEST,
                "INVALID_MILESTONE_DATE",
                "Thời gian mốc không hợp lệ với độ chính xác đã chọn."
            );
            case INVALID_CONTENT -> new ErrorDefinition(
                HttpStatus.BAD_REQUEST,
                "INVALID_CONTENT",
                e.field() + " không hợp lệ hoặc vượt độ dài cho phép."
            );
            case INVALID_PAGINATION -> new ErrorDefinition(
                HttpStatus.BAD_REQUEST,
                "INVALID_PAGINATION",
                "page phải từ 0; size từ 1 đến 100."
            );
            case RESOURCE_NOT_FOUND -> new ErrorDefinition(
                HttpStatus.NOT_FOUND,
                "RESOURCE_NOT_FOUND",
                "Không tìm thấy sự kiện hoặc mốc thuộc sự kiện."
            );
            case VERSION_CONFLICT -> new ErrorDefinition(
                HttpStatus.CONFLICT,
                "VERSION_CONFLICT",
                "Sự kiện đã thay đổi. Hãy tải lại dữ liệu trước khi thao tác."
            );
        };
    }
}
