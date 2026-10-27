package com.econnews.shared.adapter.web.response;

public record PageMeta(
        int currentPage,
        int pageSize,
        long totalItems,
        int totalPages) {

    public static PageMeta of(
            int currentPage,
            int pageSize,
            long totalItems) {
        if (currentPage < 0 || pageSize <= 0 || totalItems < 0) {
            throw new IllegalArgumentException(
                    "Thông tin phân trang không hợp lệ.");
        }

        long pages = totalItems / pageSize
                + (totalItems % pageSize == 0 ? 0 : 1);

        return new PageMeta(
                currentPage,
                pageSize,
                totalItems,
                Math.toIntExact(pages));
    }
}