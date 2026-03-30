package com.devmaster.goatfarm.milk.api;

import com.devmaster.goatfarm.config.exceptions.GlobalExceptionHandler;
import com.devmaster.goatfarm.config.security.OwnershipService;
import com.devmaster.goatfarm.milk.api.controller.FarmMilkProductionController;
import com.devmaster.goatfarm.milk.api.dto.FarmMilkProductionDailySummaryDTO;
import com.devmaster.goatfarm.milk.api.dto.FarmMilkProductionUpsertRequestDTO;
import com.devmaster.goatfarm.milk.api.mapper.FarmMilkProductionMapper;
import com.devmaster.goatfarm.milk.application.ports.in.FarmMilkProductionUseCase;
import com.devmaster.goatfarm.milk.business.bo.FarmMilkProductionDailySummaryVO;
import com.devmaster.goatfarm.milk.business.bo.FarmMilkProductionUpsertRequestVO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = true)
@Import(GlobalExceptionHandler.class)
@ActiveProfiles("test")
class FarmMilkProductionControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FarmMilkProductionController controller;

    @MockBean
    private FarmMilkProductionUseCase useCase;

    @MockBean
    private FarmMilkProductionMapper mapper;

    @MockBean
    private OwnershipService ownershipService;

    @MockBean
    private com.devmaster.goatfarm.authority.business.AdminMaintenanceBusiness adminMaintenanceBusiness;

    @BeforeEach
    void setUp() {
        when(ownershipService.canManageFarm(17L)).thenReturn(true);
        when(ownershipService.canManageFarm(18L)).thenReturn(false);
    }

    @Test
    void contextLoads() {
        Assertions.assertNotNull(controller);
    }

    @Test
    @WithMockUser(roles = "FARM_OWNER")
    void shouldReturnForbiddenWhenUserCannotManageFarm() throws Exception {
        mockMvc.perform(get("/api/v1/goatfarms/{farmId}/milk-consolidated-productions/daily", 18)
                        .param("date", "2026-03-30"))
                .andExpect(status().isForbidden());

        verify(useCase, never()).getDailySummary(eq(18L), any(LocalDate.class));
    }

    @Test
    @WithMockUser(roles = "OPERATOR")
    void shouldAllowOperatorLinkedToFarm() throws Exception {
        FarmMilkProductionUpsertRequestVO requestVO = new FarmMilkProductionUpsertRequestVO(
                new BigDecimal("180.00"),
                new BigDecimal("25.00"),
                null,
                "QA"
        );
        FarmMilkProductionDailySummaryVO responseVO = new FarmMilkProductionDailySummaryVO(
                LocalDate.of(2026, 3, 30),
                true,
                new BigDecimal("180.00"),
                new BigDecimal("25.00"),
                new BigDecimal("155.00"),
                "QA",
                null
        );
        FarmMilkProductionDailySummaryDTO responseDTO = new FarmMilkProductionDailySummaryDTO(
                LocalDate.of(2026, 3, 30),
                true,
                new BigDecimal("180.00"),
                new BigDecimal("25.00"),
                new BigDecimal("155.00"),
                "QA",
                null
        );

        when(mapper.toRequestVO(any(FarmMilkProductionUpsertRequestDTO.class))).thenReturn(requestVO);
        when(useCase.upsertDailyProduction(17L, LocalDate.of(2026, 3, 30), requestVO)).thenReturn(responseVO);
        when(mapper.toDailySummaryDTO(responseVO)).thenReturn(responseDTO);

        mockMvc.perform(put("/api/v1/goatfarms/{farmId}/milk-consolidated-productions/{productionDate}", 17, "2026-03-30")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "totalProduced": 180.00,
                                  "withdrawalProduced": 25.00,
                                  "notes": "QA"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.marketableProduced").value(155.00));
    }
}
