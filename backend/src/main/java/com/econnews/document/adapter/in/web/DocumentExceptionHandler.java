package com.econnews.document.adapter.in.web;

import com.econnews.document.application.exception.DocumentException;
import com.econnews.shared.adapter.web.response.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice(assignableTypes = DocumentController.class)
@Order(-1)
public class DocumentExceptionHandler {

    @ExceptionHandler(DocumentException.class)
    public ResponseEntity<?> handle(DocumentException e, HttpServletRequest request) {
        return ApiResponses.error(
            HttpStatusCode.valueOf(e.status()),
            e.code(),
            e.getMessage(),
            List.of(),
            request
        );
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<?> duplicate(DuplicateKeyException e, HttpServletRequest request) {
        return ApiResponses.error(
            HttpStatus.CONFLICT,
            "DOCUMENT_DUPLICATE",
            "URL tài liệu đã tồn tại.",
            List.of(),
            request
        );
    }
}
