package com.econnews.shared.adapter.web.diagnostic;

import java.util.Map;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.econnews.shared.adapter.web.response.ApiResponses;
import com.econnews.shared.adapter.web.response.ApiSuccessResponse;
import com.econnews.shared.application.exception.BusinessRuleException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

@Profile("dev")
@RestController
@RequestMapping("/api/v1/dev/diagnostics")
public class DiagnosticController {

    @GetMapping("/ok")
    public ResponseEntity<ApiSuccessResponse<Map<String, String>>> ok(
            HttpServletRequest request) {

        return ApiResponses.ok(
                "Kiểm tra backend thành công.",
                Map.of("application", "econnews", "status", "UP"),
                request);
    }

    @GetMapping("/business-error")
    public void businessError() {
        throw new BusinessRuleException(
                "DEMO_BUSINESS_ERROR",
                "Đây là lỗi nghiệp vụ minh họa.");
    }

    @PostMapping("/validation")
    public Map<String, String> validate(
            @Valid @RequestBody DiagnosticRequest request) {
        return Map.of("name", request.name());
    }

    public record DiagnosticRequest(
            @NotBlank(message = "Tên không được để trống.") String name) {
    }
}
