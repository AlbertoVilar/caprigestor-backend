package com.devmaster.goatfarm.inventory.api.controller;

import com.devmaster.goatfarm.config.exceptions.GlobalExceptionHandler;
import com.devmaster.goatfarm.inventory.api.dto.InventoryMovementCreateRequestDTO;
import com.devmaster.goatfarm.inventory.api.dto.InventoryMovementResponseDTO;
import com.devmaster.goatfarm.inventory.api.mapper.InventoryMovementApiMapper;
import com.devmaster.goatfarm.inventory.application.ports.in.InventoryMovementCommandUseCase;
import com.devmaster.goatfarm.inventory.application.ports.in.InventoryMovementQueryUseCase;
import com.devmaster.goatfarm.inventory.business.bo.InventoryMovementCreateRequestVO;
import com.devmaster.goatfarm.inventory.business.bo.InventoryMovementResultVO;
import com.devmaster.goatfarm.inventory.business.bo.InventoryMovementResponseVO;
import com.devmaster.goatfarm.inventory.domain.enums.InventoryMovementType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InventoryMovementController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class InventoryMovementCommandControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InventoryMovementCommandUseCase commandUseCase;

    @MockBean
    private InventoryMovementQueryUseCase queryUseCase;

    @MockBean
    private InventoryMovementApiMapper apiMapper;

    @MockBean(name = "ownershipService")
    private Object ownershipService;

    @Test
    void createMovement_shouldAcceptPurchaseCostPayload() throws Exception {
        InventoryMovementCreateRequestVO requestVO = new InventoryMovementCreateRequestVO(
                InventoryMovementType.IN,
                new BigDecimal("10.000"),
                101L,
                null,
                null,
                LocalDate.of(2026, 3, 28),
                "compra de racao",
                new BigDecimal("18.5000"),
                new BigDecimal("185.00"),
                LocalDate.of(2026, 3, 28),
                "Casa do Campo"
        );
        InventoryMovementResponseVO responseVO = new InventoryMovementResponseVO(
                9001L,
                InventoryMovementType.IN,
                new BigDecimal("10.000"),
                101L,
                null,
                LocalDate.of(2026, 3, 28),
                new BigDecimal("10.000"),
                new BigDecimal("18.5000"),
                new BigDecimal("185.00"),
                LocalDate.of(2026, 3, 28),
                "Casa do Campo",
                OffsetDateTime.parse("2026-03-28T14:10:00Z")
        );
        InventoryMovementResponseDTO responseDTO = new InventoryMovementResponseDTO(
                9001L,
                InventoryMovementType.IN,
                new BigDecimal("10.000"),
                101L,
                null,
                LocalDate.of(2026, 3, 28),
                new BigDecimal("10.000"),
                new BigDecimal("18.5000"),
                new BigDecimal("185.00"),
                LocalDate.of(2026, 3, 28),
                "Casa do Campo",
                OffsetDateTime.parse("2026-03-28T14:10:00Z")
        );

        when(apiMapper.toRequestVO(any(InventoryMovementCreateRequestDTO.class))).thenReturn(requestVO);
        when(commandUseCase.createMovement(eq(17L), eq("inventory-cost-key"), eq(requestVO)))
                .thenReturn(new InventoryMovementResultVO(responseVO, false));
        when(apiMapper.toResponseDTO(responseVO)).thenReturn(responseDTO);

        mockMvc.perform(post("/api/v1/goatfarms/{farmId}/inventory/movements", 17L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Idempotency-Key", "inventory-cost-key")
                        .content("""
                                {
                                  "type": "IN",
                                  "quantity": 10.0,
                                  "itemId": 101,
                                  "movementDate": "2026-03-28",
                                  "reason": "compra de racao",
                                  "unitCost": 18.5,
                                  "totalCost": 185.0,
                                  "purchaseDate": "2026-03-28",
                                  "supplierName": "Casa do Campo"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.movementId").value(9001))
                .andExpect(jsonPath("$.unitCost").value(18.5))
                .andExpect(jsonPath("$.totalCost").value(185.0))
                .andExpect(jsonPath("$.supplierName").value("Casa do Campo"));
    }
}
