package com.econnews.collection.adapter.in.web;

import com.econnews.collection.application.exception.CollectionException;
import com.econnews.shared.adapter.web.response.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.core.annotation.Order;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice(assignableTypes = CollectionController.class)
@Order(-1)
public class CollectionExceptionHandler {

    @ExceptionHandler(CollectionException.class)
    public ResponseEntity<?> handle(CollectionException e, HttpServletRequest request) {
        var response = ApiResponses.error(
            HttpStatusCode.valueOf(e.status()),
            e.code(),
            e.getMessage(),
            List.of(),
            request
        );
        return e.status() == 429
            ? ResponseEntity.status(429).header("Retry-After", "30").body(response.getBody())
            : response;
    }
}
