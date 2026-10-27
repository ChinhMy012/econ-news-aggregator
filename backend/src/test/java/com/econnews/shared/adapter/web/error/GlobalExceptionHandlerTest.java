package com.econnews.shared.adapter.web.error;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import com.econnews.shared.adapter.web.logging.RequestIdFilter;
import com.econnews.shared.application.exception.BusinessRuleException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

class GlobalExceptionHandlerTest {
    private MockMvc mvc;
    private final JsonMapper mapper = new JsonMapper();

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .addFilters(new RequestIdFilter()).build();
    }

    private JsonNode verify(MvcResult result, int status, String code) throws Exception {
        assertEquals(status, result.getResponse().getStatus());
        JsonNode body = mapper.readTree(result.getResponse().getContentAsString());
        assertFalse(body.get("success").asBoolean());
        assertEquals(status, body.get("statusCode").asInt());
        assertEquals(code, body.get("errorCode").asText());
        assertTrue(body.get("errors").isArray());
        assertNotNull(body.get("timestamp"));
        assertEquals(result.getResponse().getHeader("X-Request-ID"), body.get("requestId").asText());
        assertFalse(body.has("stack"));
        return body;
    }

    @Test void businessError() throws Exception {
        verify(mvc.perform(get("/test/business")).andReturn(), 400, "SOURCE_REQUIRED");
    }
    @Test void validation() throws Exception {
        JsonNode body = verify(mvc.perform(post("/test/validate")
                .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"\"}"))
                .andReturn(), 400, "VALIDATION_FAILED");
        assertEquals("name", body.get("errors").get(0).get("field").asText());
        assertEquals("NOT_BLANK", body.get("errors").get(0).get("rule").asText());
    }
    @Test void invalidJson() throws Exception {
        verify(mvc.perform(post("/test/validate").contentType(MediaType.APPLICATION_JSON)
                .content("{broken")).andReturn(), 400, "BAD_REQUEST");
    }
    @Test void preservesAllowHeader() throws Exception {
        MvcResult result = mvc.perform(post("/test/business")).andReturn();
        verify(result, 405, "METHOD_NOT_ALLOWED");
        assertTrue(result.getResponse().getHeader(HttpHeaders.ALLOW).contains("GET"));
    }
    @Test void notFound() throws Exception {
        verify(mvc.perform(get("/test/missing")).andReturn(), 404, "RESOURCE_NOT_FOUND");
    }
    @Test void doesNotLeakInternalMessage() throws Exception {
        JsonNode body = verify(mvc.perform(get("/test/unexpected")).andReturn(), 500,
                "INTERNAL_SERVER_ERROR");
        assertFalse(body.toString().contains("database-secret"));
    }

    @RestController
    static class TestController {
        @GetMapping("/test/business") void business() {
            throw new BusinessRuleException("SOURCE_REQUIRED", "Cần nguồn.");
        }
        @PostMapping("/test/validate") void validate(@Valid @RequestBody Input input) {}
        @GetMapping("/test/missing") void missing() {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "internal-detail");
        }
        @GetMapping("/test/unexpected") void unexpected() {
            throw new IllegalStateException("database-secret");
        }
    }
    record Input(@NotBlank String name) {}
}
