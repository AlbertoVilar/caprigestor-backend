package com.devmaster.goatfarm.integration;

import com.devmaster.goatfarm.config.exceptions.GlobalExceptionHandler;
import com.devmaster.goatfarm.config.exceptions.custom.BusinessRuleException;
import com.devmaster.goatfarm.config.exceptions.custom.ValidationError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ValidationPathIntegrationTest {

    private MockMvc mockMvc;

    @RestController
    static class TestController {
        @GetMapping("/test/validation-exception")
        public void throwValidationException() {
            // Simulate Business Layer throwing BusinessRuleException (which replaces ValidationException usage)
            throw new BusinessRuleException("business_rule", "Business Error");
        }
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldPopulatePathInValidationError() throws Exception {
        mockMvc.perform(get("/test/validation-exception")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.error").value("Regra de negócio violada"))
                .andExpect(jsonPath("$.path").value("/test/validation-exception")) // This verifies GlobalExceptionHandler populated the path
                .andExpect(jsonPath("$.errors[0].fieldName").value("business_rule"))
                .andExpect(jsonPath("$.errors[0].message").value("Business Error"));
    }
}
