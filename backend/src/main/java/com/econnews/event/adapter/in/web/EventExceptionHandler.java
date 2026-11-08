package com.econnews.event.adapter.in.web;

import com.econnews.event.application.exception.EventException;
import com.econnews.shared.adapter.web.response.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.core.annotation.Order;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice(
    assignableTypes = { EventAdminController.class, PublicEventsController.class }
)
@Order(-1)
public class EventExceptionHandler {

    @ExceptionHandler(EventException.class)
    public ResponseEntity<?> handle(EventException e, HttpServletRequest request) {
        return ApiResponses.error(
            HttpStatusCode.valueOf(e.status()),
            e.code(),
            e.getMessage(),
            List.of(),
            request
        );
    }
}
