package com.devmaster.goatfarm.commercial.api;

import com.devmaster.goatfarm.commercial.api.controller.OperationalFinanceController;
import com.devmaster.goatfarm.commercial.api.dto.MonthlyOperationalSummaryDTO;
import com.devmaster.goatfarm.commercial.api.dto.OperationalExpenseRequestDTO;
import com.devmaster.goatfarm.commercial.api.dto.OperationalExpenseResponseDTO;
import com.devmaster.goatfarm.commercial.api.mapper.OperationalFinanceApiMapper;
import com.devmaster.goatfarm.commercial.application.ports.in.OperationalFinanceUseCase;
import com.devmaster.goatfarm.commercial.business.bo.MonthlyOperationalSummaryVO;
import com.devmaster.goatfarm.commercial.business.bo.OperationalExpenseRequestVO;
import com.devmaster.goatfarm.commercial.business.bo.OperationalExpenseResponseVO;
import com.devmaster.goatfarm.commercial.enums.OperationalExpenseCategory;
import com.devmaster.goatfarm.config.exceptions.GlobalExceptionHandler;
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
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OperationalFinanceController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class OperationalFinanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OperationalFinanceUseCase operationalFinanceUseCase;

    @MockBean
    private OperationalFinanceApiMapper apiMapper;

    @MockBean(name = "ownershipService")
    private Object ownershipService;

    @Test
    void createOperationalExpense_shouldReturnCreated() throws Exception {
        OperationalExpenseRequestVO requestVO = new OperationalExpenseRequestVO(
                OperationalExpenseCategory.ENERGY,
                "Conta de luz",
                new BigDecimal("210.00"),
                LocalDate.of(2026, 3, 28),
                "Março"
        );
        OperationalExpenseResponseVO responseVO = new OperationalExpenseResponseVO(
                11L,
                OperationalExpenseCategory.ENERGY,
                "Conta de luz",
                new BigDecimal("210.00"),
                LocalDate.of(2026, 3, 28),
                "Março",
                LocalDateTime.of(2026, 3, 28, 10, 30)
        );
        OperationalExpenseResponseDTO responseDTO = new OperationalExpenseResponseDTO(
                11L,
                OperationalExpenseCategory.ENERGY,
                "Conta de luz",
                new BigDecimal("210.00"),
                LocalDate.of(2026, 3, 28),
                "Março",
                LocalDateTime.of(2026, 3, 28, 10, 30)
        );

        when(apiMapper.toVO(any(OperationalExpenseRequestDTO.class))).thenReturn(requestVO);
        when(operationalFinanceUseCase.createOperationalExpense(17L, requestVO)).thenReturn(responseVO);
        when(apiMapper.toDTO(responseVO)).thenReturn(responseDTO);

        mockMvc.perform(post("/api/v1/goatfarms/{farmId}/commercial/operational-expenses", 17L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "category": "ENERGY",
                                  "description": "Conta de luz",
                                  "amount": 210.00,
                                  "expenseDate": "2026-03-28",
                                  "notes": "Marco"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(11))
                .andExpect(jsonPath("$.category").value("ENERGY"));
    }

    @Test
    void getMonthlySummary_shouldReturnAggregatedValues() throws Exception {
        MonthlyOperationalSummaryVO responseVO = new MonthlyOperationalSummaryVO(
                2026,
                3,
                new BigDecimal("1780.00"),
                new BigDecimal("750.00"),
                new BigDecimal("1030.00"),
                new BigDecimal("1400.00"),
                new BigDecimal("380.00"),
                new BigDecimal("250.00"),
                new BigDecimal("500.00")
        );
        MonthlyOperationalSummaryDTO responseDTO = new MonthlyOperationalSummaryDTO(
                2026,
                3,
                new BigDecimal("1780.00"),
                new BigDecimal("750.00"),
                new BigDecimal("1030.00"),
                new BigDecimal("1400.00"),
                new BigDecimal("380.00"),
                new BigDecimal("250.00"),
                new BigDecimal("500.00")
        );

        when(operationalFinanceUseCase.getMonthlySummary(17L, 2026, 3)).thenReturn(responseVO);
        when(apiMapper.toDTO(responseVO)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/v1/goatfarms/{farmId}/commercial/monthly-summary", 17L)
                        .param("year", "2026")
                        .param("month", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRevenue").value(1780.0))
                .andExpect(jsonPath("$.inventoryPurchaseCostsTotal").value(500.0))
                .andExpect(jsonPath("$.balance").value(1030.0));
    }
}
