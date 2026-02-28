package com.devmaster.goatfarm.inventory.api.controller;

import com.devmaster.goatfarm.config.exceptions.GlobalExceptionHandler;
import com.devmaster.goatfarm.config.exceptions.custom.InvalidArgumentException;
import com.devmaster.goatfarm.inventory.api.dto.InventoryMovementHistoryResponseDTO;
import com.devmaster.goatfarm.inventory.api.mapper.InventoryMovementApiMapper;
import com.devmaster.goatfarm.inventory.application.ports.in.InventoryMovementCommandUseCase;
import com.devmaster.goatfarm.inventory.application.ports.in.InventoryMovementQueryUseCase;
import com.devmaster.goatfarm.inventory.business.bo.InventoryMovementFilterVO;
import com.devmaster.goatfarm.inventory.business.bo.InventoryMovementHistoryResponseVO;
import com.devmaster.goatfarm.inventory.domain.enums.InventoryMovementType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InventoryMovementController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class InventoryMovementControllerTest {

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
    void listMovements_shouldReturnPagedHistory() throws Exception {
        InventoryMovementHistoryResponseVO responseVO = new InventoryMovementHistoryResponseVO(
                9001L,
                InventoryMovementType.OUT,
                null,
                new BigDecimal("2.000"),
                101L,
                "Ração Premium",
                501L,
                LocalDate.of(2026, 2, 28),
                "Baixa por aplicação",
                new BigDecimal("18.750"),
                OffsetDateTime.parse("2026-02-28T12:15:00Z")
        );
        InventoryMovementHistoryResponseDTO responseDTO = new InventoryMovementHistoryResponseDTO(
                9001L,
                InventoryMovementType.OUT,
                null,
                new BigDecimal("2.000"),
                101L,
                "Ração Premium",
                501L,
                LocalDate.of(2026, 2, 28),
                "Baixa por aplicação",
                new BigDecimal("18.750"),
                OffsetDateTime.parse("2026-02-28T12:15:00Z")
        );

        when(queryUseCase.listMovements(any(InventoryMovementFilterVO.class)))
                .thenReturn(new PageImpl<>(List.of(responseVO), PageRequest.of(0, 20), 1));
        when(apiMapper.toHistoryResponseDTO(responseVO)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/v1/goatfarms/{farmId}/inventory/movements", 1L)
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].movementId").value(9001))
                .andExpect(jsonPath("$.content[0].itemName").value("Ração Premium"))
                .andExpect(jsonPath("$.content[0].type").value("OUT"))
                .andExpect(jsonPath("$.page.totalElements").value(1));

        verify(queryUseCase).listMovements(any(InventoryMovementFilterVO.class));
    }

    @Test
    void listMovements_shouldReturn400_whenDateRangeIsInvalid() throws Exception {
        when(queryUseCase.listMovements(any(InventoryMovementFilterVO.class)))
                .thenThrow(new InvalidArgumentException("fromDate", "Data inicial não pode ser maior que data final."));

        mockMvc.perform(get("/api/v1/goatfarms/{farmId}/inventory/movements", 1L)
                        .param("fromDate", "2026-03-01")
                        .param("toDate", "2026-02-01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].fieldName").value("fromDate"))
                .andExpect(jsonPath("$.errors[0].message").value("Data inicial não pode ser maior que data final."));
    }

    @Test
    void controller_shouldExposeDualMapping_andFarmOwnershipGuard() throws NoSuchMethodException {
        RequestMapping requestMapping = InventoryMovementController.class.getAnnotation(RequestMapping.class);

        assertThat(requestMapping).isNotNull();
        assertThat(requestMapping.value()).containsExactlyInAnyOrder(
                "/api/v1/goatfarms/{farmId}/inventory/movements",
                "/api/goatfarms/{farmId}/inventory/movements"
        );

        PreAuthorize listGuard = InventoryMovementController.class
                .getMethod(
                        "listMovements",
                        Long.class,
                        Long.class,
                        Long.class,
                        InventoryMovementType.class,
                        java.time.LocalDate.class,
                        java.time.LocalDate.class,
                        org.springframework.data.domain.Pageable.class
                )
                .getAnnotation(PreAuthorize.class);

        assertThat(listGuard).isNotNull();
        assertThat(listGuard.value()).isEqualTo("@ownershipService.canManageFarm(#farmId)");
    }
}
