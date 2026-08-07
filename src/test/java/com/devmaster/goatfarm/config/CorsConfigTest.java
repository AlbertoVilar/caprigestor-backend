package com.devmaster.goatfarm.config;

import com.devmaster.goatfarm.config.logging.HttpRequestLoggingFilter;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CorsConfigTest {

    @Test
    void shouldExposeCorrelationIdHeaderToBrowserClients() {
        CorsConfig config = new CorsConfig();
        ReflectionTestUtils.setField(config, "corsOrigins", "http://localhost:5173");
        CorsConfigurationSource source = config.corsConfigurationSource();

        CorsConfiguration cors = source.getCorsConfiguration(new MockHttpServletRequest("GET", "/api/v1/test"));

        assertNotNull(cors);
        assertNotNull(cors.getExposedHeaders());
        assertTrue(cors.getExposedHeaders().contains(HttpRequestLoggingFilter.CORRELATION_ID_HEADER));
    }
}
