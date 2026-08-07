package com.devmaster.goatfarm.config.logging;

import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class HttpRequestLoggingFilterTest {

    private final HttpRequestLoggingFilter filter = new HttpRequestLoggingFilter();

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void shouldPreserveSafeCorrelationId() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/goatfarms");
        request.addHeader(HttpRequestLoggingFilter.CORRELATION_ID_HEADER, "interview-demo-123");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (servletRequest, servletResponse) ->
                assertEquals("interview-demo-123",
                        MDC.get(HttpRequestLoggingFilter.CORRELATION_ID_MDC_KEY)));

        assertEquals("interview-demo-123",
                response.getHeader(HttpRequestLoggingFilter.CORRELATION_ID_HEADER));
        assertNull(MDC.get(HttpRequestLoggingFilter.CORRELATION_ID_MDC_KEY));
    }

    @Test
    void shouldGenerateCorrelationIdWhenHeaderIsMissing() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/actuator/health");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        String correlationId = response.getHeader(HttpRequestLoggingFilter.CORRELATION_ID_HEADER);
        assertDoesNotThrow(() -> UUID.fromString(correlationId));
        assertNull(MDC.get(HttpRequestLoggingFilter.CORRELATION_ID_MDC_KEY));
    }

    @Test
    void shouldReplaceUnsafeCorrelationId() throws ServletException, IOException {
        String unsafeCorrelationId = "invalid value with spaces";
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/auth/login");
        request.addHeader(HttpRequestLoggingFilter.CORRELATION_ID_HEADER, unsafeCorrelationId);
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        String generatedCorrelationId = response.getHeader(HttpRequestLoggingFilter.CORRELATION_ID_HEADER);
        assertNotEquals(unsafeCorrelationId, generatedCorrelationId);
        assertDoesNotThrow(() -> UUID.fromString(generatedCorrelationId));
        assertNull(MDC.get(HttpRequestLoggingFilter.CORRELATION_ID_MDC_KEY));
    }
}
