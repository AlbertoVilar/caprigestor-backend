package com.devmaster.goatfarm.goatownership.api;

import com.devmaster.goatfarm.application.exception.AuthorizationDeniedException;
import com.devmaster.goatfarm.config.exceptions.GlobalExceptionHandler;
import com.devmaster.goatfarm.config.exceptions.custom.BusinessRuleException;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goatownership.api.controller.GoatOwnershipHistoryController;
import com.devmaster.goatfarm.goatownership.api.mapper.OwnershipHistoryApiMapper;
import com.devmaster.goatfarm.goatownership.application.model.OwnershipHistory;
import com.devmaster.goatfarm.goatownership.application.model.OwnershipHistoryPeriod;
import com.devmaster.goatfarm.goatownership.application.ports.in.GoatOwnershipHistoryQueryUseCase;
import com.devmaster.goatfarm.goatownership.domain.OwnershipEntryType;
import com.devmaster.goatfarm.goatownership.domain.OwnershipExitType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class GoatOwnershipHistoryControllerTest {
    @Mock GoatOwnershipHistoryQueryUseCase queryUseCase;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mockMvc = MockMvcBuilders.standaloneSetup(new GoatOwnershipHistoryController(
                        queryUseCase, new OwnershipHistoryApiMapper()))
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void positiveStructuralGoatIdDelegatesAndPreservesApprovedResponse() throws Exception {
        when(queryUseCase.findOwnershipHistory(GoatId.of(42L))).thenReturn(history());

        mockMvc.perform(get("/api/v1/goats/42/ownership-history")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.goatId").value(42))
                .andExpect(jsonPath("$.periods.length()").value(2))
                .andExpect(jsonPath("$.periods[0].farmId").value(10))
                .andExpect(jsonPath("$.periods[0].startedAt").value("2025-01-01T00:00:00Z"))
                .andExpect(jsonPath("$.periods[0].endedAt").value("2026-03-01T12:00:00Z"))
                .andExpect(jsonPath("$.periods[0].entryType").value("MANUAL_IMPORT"))
                .andExpect(jsonPath("$.periods[0].exitType").value("TRANSFER_OUT"))
                .andExpect(jsonPath("$.periods[0].current").value(false))
                .andExpect(jsonPath("$.periods[1].farmId").value(20))
                .andExpect(jsonPath("$.periods[1].endedAt").doesNotExist())
                .andExpect(jsonPath("$.periods[1].entryType").value("TRANSFER_IN"))
                .andExpect(jsonPath("$.periods[1].exitType").doesNotExist())
                .andExpect(jsonPath("$.periods[1].current").value(true))
                .andExpect(jsonPath("$.periods[0].id").doesNotExist())
                .andExpect(jsonPath("$.periods[0].version").doesNotExist())
                .andExpect(jsonPath("$.periods[0].source").doesNotExist())
                .andExpect(jsonPath("$.periods[0].transferId").doesNotExist())
                .andExpect(jsonPath("$.periods[0].reason").doesNotExist())
                .andExpect(jsonPath("$.farmId").doesNotExist());

        verify(queryUseCase).findOwnershipHistory(GoatId.of(42L));
    }

    @Test
    void zeroGoatIdReturnsBadRequestWithoutCallingUseCase() throws Exception {
        mockMvc.perform(get("/api/v1/goats/0/ownership-history"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].fieldName").value("goatId"));
        verify(queryUseCase, never()).findOwnershipHistory(any());
    }

    @Test
    void negativeGoatIdReturnsBadRequestWithoutCallingUseCase() throws Exception {
        mockMvc.perform(get("/api/v1/goats/-1/ownership-history"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].fieldName").value("goatId"));
        verify(queryUseCase, never()).findOwnershipHistory(any());
    }

    @Test
    void malformedGoatIdReturnsBadRequestWithoutCallingUseCase() throws Exception {
        mockMvc.perform(get("/api/v1/goats/not-a-number/ownership-history"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].fieldName").value("goatId"));
        verify(queryUseCase, never()).findOwnershipHistory(any());
    }

    @Test
    void overflowingGoatIdReturnsBadRequestWithoutCallingUseCase() throws Exception {
        mockMvc.perform(get("/api/v1/goats/9223372036854775808/ownership-history"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].fieldName").value("goatId"));
        verify(queryUseCase, never()).findOwnershipHistory(any());
    }

    @Test
    void applicationNotFoundIsMappedToNotFound() throws Exception {
        when(queryUseCase.findOwnershipHistory(GoatId.of(42L)))
                .thenThrow(new ResourceNotFoundException("missing"));

        mockMvc.perform(get("/api/v1/goats/42/ownership-history"))
                .andExpect(status().isNotFound());
    }

    @Test
    void authorizationDeniedIsMappedToForbidden() throws Exception {
        when(queryUseCase.findOwnershipHistory(GoatId.of(42L)))
                .thenThrow(new AuthorizationDeniedException("denied"));

        mockMvc.perform(get("/api/v1/goats/42/ownership-history"))
                .andExpect(status().isForbidden());
    }

    @Test
    void canonicalOwnershipInconsistencyIsMappedToUnprocessableEntity() throws Exception {
        when(queryUseCase.findOwnershipHistory(GoatId.of(42L)))
                .thenThrow(new BusinessRuleException("ownershipHistory", "inconsistent"));

        mockMvc.perform(get("/api/v1/goats/42/ownership-history"))
                .andExpect(status().isUnprocessableEntity());
    }

    private OwnershipHistory history() {
        return new OwnershipHistory(GoatId.of(42L), List.of(
                new OwnershipHistoryPeriod(10L,
                        Instant.parse("2025-01-01T00:00:00Z"),
                        Instant.parse("2026-03-01T12:00:00Z"),
                        OwnershipEntryType.MANUAL_IMPORT,
                        OwnershipExitType.TRANSFER_OUT,
                        false),
                new OwnershipHistoryPeriod(20L,
                        Instant.parse("2026-03-01T12:00:00Z"),
                        null,
                        OwnershipEntryType.TRANSFER_IN,
                        null,
                        true)));
    }
}
