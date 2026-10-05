package com.devmaster.goatfarm.goatownership.api;

import com.devmaster.goatfarm.application.pagination.PageResult;
import com.devmaster.goatfarm.config.exceptions.GlobalExceptionHandler;
import com.devmaster.goatfarm.goatownership.api.controller.OwnershipMovementController;
import com.devmaster.goatfarm.goatownership.api.mapper.OwnershipMovementApiMapper;
import com.devmaster.goatfarm.goatownership.application.model.OwnershipMovementDirection;
import com.devmaster.goatfarm.goatownership.application.model.OwnershipMovementItem;
import com.devmaster.goatfarm.goatownership.application.model.OwnershipMovementKind;
import com.devmaster.goatfarm.goatownership.application.model.OwnershipMovementPageQuery;
import com.devmaster.goatfarm.goatownership.application.ports.in.OwnershipMovementQueryUseCase;
import com.devmaster.goatfarm.goatownership.domain.OwnershipTransferStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class OwnershipMovementControllerTest {
    @Mock OwnershipMovementQueryUseCase queryUseCase;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new OwnershipMovementController(
                        queryUseCase, new OwnershipMovementApiMapper()))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void mapsUnifiedCompletedInternalSaleMovementAndPagination() throws Exception {
        var movement = new OwnershipMovementItem(71L, 55L, "Isidra", "RG-55", 19L, "Capril Bocaina", 1L, "Capril Vilar",
                OwnershipMovementKind.INTERNAL_SALE, OwnershipTransferStatus.COMPLETED,
                OwnershipMovementDirection.INCOMING, "Sale", Instant.parse("2026-09-29T12:00:00Z"),
                null, Instant.parse("2026-09-29T13:00:00Z"), Instant.parse("2026-09-29T13:00:00Z"),
                null, true, 18L, LocalDate.parse("2026-09-29"), new BigDecimal("500.00"),
                "PAID", LocalDate.parse("2026-09-29"));
        when(queryUseCase.listForFarm(1L, OwnershipMovementDirection.INCOMING,
                OwnershipTransferStatus.COMPLETED, OwnershipMovementKind.INTERNAL_SALE,
                new OwnershipMovementPageQuery(0, 20)))
                .thenReturn(new PageResult<>(List.of(movement), 1, 0, 20));

        mockMvc.perform(get("/api/v1/goatfarms/1/ownership-movements")
                        .param("direction", "incoming")
                        .param("status", "completed")
                        .param("kind", "internal_sale"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].movementId").value(71))
                .andExpect(jsonPath("$.content[0].movementKind").value("INTERNAL_SALE"))
                .andExpect(jsonPath("$.content[0].goatName").value("Isidra"))
                .andExpect(jsonPath("$.content[0].goatRegistrationNumber").value("RG-55"))
                .andExpect(jsonPath("$.content[0].sourceFarmName").value("Capril Bocaina"))
                .andExpect(jsonPath("$.content[0].targetFarmName").value("Capril Vilar"))
                .andExpect(jsonPath("$.content[0].realized").value(true))
                .andExpect(jsonPath("$.content[0].saleId").value(18))
                .andExpect(jsonPath("$.content[0].paymentStatus").value("PAID"))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(queryUseCase).listForFarm(1L, OwnershipMovementDirection.INCOMING,
                OwnershipTransferStatus.COMPLETED, OwnershipMovementKind.INTERNAL_SALE,
                new OwnershipMovementPageQuery(0, 20));
    }

    @Test
    void rejectsKindsOutsideInitialReadModel() throws Exception {
        mockMvc.perform(get("/api/v1/goatfarms/1/ownership-movements")
                        .param("direction", "OUTGOING")
                        .param("kind", "RETURN"))
                .andExpect(status().isBadRequest());
    }
}
