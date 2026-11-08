package com.econnews.source.adapter.in.web;

import com.econnews.shared.adapter.web.response.ApiResponses;
import com.econnews.source.application.exception.CatalogException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice(assignableTypes = SourceCatalogController.class)
@Order(-1)
public class CatalogExceptionHandler {

    @ExceptionHandler(CatalogException.class)
    public ResponseEntity<?> catalog(CatalogException e, HttpServletRequest req) {
        return ApiResponses.error(
            HttpStatusCode.valueOf(e.status()),
            e.code(),
            e.getMessage(),
            List.of(),
            req
        );
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<?> duplicate(DuplicateKeyException e, HttpServletRequest req) {
        return ApiResponses.error(
            HttpStatus.CONFLICT,
            "CATALOG_DUPLICATE",
            "Mã nguồn hoặc URL kênh RSS đã tồn tại.",
            List.of(),
            req
        );
    }
}
