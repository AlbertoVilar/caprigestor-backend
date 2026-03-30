package com.devmaster.goatfarm.milk.api.controller;

import com.devmaster.goatfarm.config.exceptions.GlobalExceptionHandler;
import com.devmaster.goatfarm.milk.api.dto.FarmMilkProductionAnnualSummaryDTO;
import com.devmaster.goatfarm.milk.api.dto.FarmMilkProductionDailySummaryDTO;
import com.devmaster.goatfarm.milk.api.dto.FarmMilkProductionMonthlySummaryDTO;
import com.devmaster.goatfarm.milk.api.dto.FarmMilkProductionUpsertRequestDTO;
import com.devmaster.goatfarm.milk.api.mapper.FarmMilkProductionMapper;
import com.devmaster.goatfarm.milk.application.ports.in.FarmMilkProductionUseCase;
import com.devmaster.goatfarm.milk.business.bo.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FarmMilkProductionController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class FarmMilkProductionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FarmMilkProductionUseCase useCase;

    @MockBean
    private FarmMilkProductionMapper mapper;

    @MockBean(name = "ownershipService")
    private Object ownershipService;

    @Test
    void shouldUpsertDailyProduction() throws Exception {
        FarmMilkProductionUpsertRequestDTO requestDTO = new FarmMilkProductionUpsertRequestDTO(
                new BigDecimal("185.50"),
                new BigDecimal("12.30"),
                null,
                "Total do dia"
        );
        FarmMilkProductionUpsertRequestVO requestVO = new FarmMilkProductionUpsertRequestVO(
                new BigDecimal("185.50"),
                new BigDecimal("12.30"),
                null,
                "Total do dia"
        );
        FarmMilkProductionDailySummaryVO responseVO = new FarmMilkProductionDailySummaryVO(
                LocalDate.of(2026, 3, 30),
                true,
                new BigDecimal("185.50"),
                new BigDecimal("12.30"),
                new BigDecimal("173.20"),
                "Total do dia",
                null
        );
        FarmMilkProductionDailySummaryDTO responseDTO = new FarmMilkProductionDailySummaryDTO(
                LocalDate.of(2026, 3, 30),
                true,
                new BigDecimal("185.50"),
                new BigDecimal("12.30"),
                new BigDecimal("173.20"),
                "Total do dia",
                null
        );

        when(mapper.toRequestVO(requestDTO)).thenReturn(requestVO);
        when(useCase.upsertDailyProduction(17L, LocalDate.of(2026, 3, 30), requestVO)).thenReturn(responseVO);
        when(mapper.toDailySummaryDTO(responseVO)).thenReturn(responseDTO);

        mockMvc.perform(put("/api/v1/goatfarms/{farmId}/milk-consolidated-productions/{productionDate}", 17, "2026-03-30")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "totalProduced": 185.50,
                                  "withdrawalProduced": 12.30,
                                  "notes": "Total do dia"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.registered").value(true))
                .andExpect(jsonPath("$.marketableProduced").value(173.20));
    }

    @Test
    void shouldReturnMonthlySummary() throws Exception {
        FarmMilkProductionMonthlySummaryVO responseVO = new FarmMilkProductionMonthlySummaryVO(
                2026,
                3,
                new BigDecimal("315.50"),
                new BigDecimal("15.50"),
                new BigDecimal("300.00"),
                2,
                List.of(
                        new FarmMilkProductionMonthlyDayItemVO(
                                LocalDate.of(2026, 3, 28),
                                new BigDecimal("150.00"),
                                new BigDecimal("10.00"),
                                new BigDecimal("140.00"),
                                "QA"
                        )
                )
        );
        FarmMilkProductionMonthlySummaryDTO responseDTO = new FarmMilkProductionMonthlySummaryDTO(
                2026,
                3,
                new BigDecimal("315.50"),
                new BigDecimal("15.50"),
                new BigDecimal("300.00"),
                2,
                List.of()
        );

        when(useCase.getMonthlySummary(17L, 2026, 3)).thenReturn(responseVO);
        when(mapper.toMonthlySummaryDTO(responseVO)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/v1/goatfarms/{farmId}/milk-consolidated-productions/monthly", 17)
                        .param("year", "2026")
                        .param("month", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProduced").value(315.50))
                .andExpect(jsonPath("$.daysRegistered").value(2));
    }

    @Test
    void shouldReturnAnnualSummary() throws Exception {
        FarmMilkProductionAnnualSummaryVO responseVO = new FarmMilkProductionAnnualSummaryVO(
                2026,
                new BigDecimal("415.50"),
                new BigDecimal("15.50"),
                new BigDecimal("400.00"),
                3,
                List.of(
                        new FarmMilkProductionAnnualMonthItemVO(
                                3,
                                new BigDecimal("315.50"),
                                new BigDecimal("15.50"),
                                new BigDecimal("300.00"),
                                2
                        )
                )
        );
        FarmMilkProductionAnnualSummaryDTO responseDTO = new FarmMilkProductionAnnualSummaryDTO(
                2026,
                new BigDecimal("415.50"),
                new BigDecimal("15.50"),
                new BigDecimal("400.00"),
                3,
                List.of()
        );

        when(useCase.getAnnualSummary(17L, 2026)).thenReturn(responseVO);
        when(mapper.toAnnualSummaryDTO(responseVO)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/v1/goatfarms/{farmId}/milk-consolidated-productions/annual", 17)
                        .param("year", "2026"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProduced").value(415.50))
                .andExpect(jsonPath("$.daysRegistered").value(3));
    }

    @Test
    void shouldReturn422WhenPayloadHasTooManyDecimalPlaces() throws Exception {
        mockMvc.perform(put("/api/v1/goatfarms/{farmId}/milk-consolidated-productions/{productionDate}", 17, "2026-03-30")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "totalProduced": 185.505,
                                  "withdrawalProduced": 12.30,
                                  "notes": "Total do dia"
                                }
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errors[0].fieldName").value("totalProduced"));
    }

    @Test
    void controllerShouldExposeCanonicalMappingAndOwnershipGuard() throws NoSuchMethodException {
        RequestMapping requestMapping = FarmMilkProductionController.class.getAnnotation(RequestMapping.class);

        assertThat(requestMapping).isNotNull();
        assertThat(requestMapping.value()).containsExactly("/api/v1/goatfarms/{farmId}/milk-consolidated-productions");

        PreAuthorize upsertGuard = FarmMilkProductionController.class
                .getMethod(
                        "upsertDailyProduction",
                        Long.class,
                        LocalDate.class,
                        FarmMilkProductionUpsertRequestDTO.class
                )
                .getAnnotation(PreAuthorize.class);
        PreAuthorize dailyGuard = FarmMilkProductionController.class
                .getMethod("getDailySummary", Long.class, LocalDate.class)
                .getAnnotation(PreAuthorize.class);
        PreAuthorize monthlyGuard = FarmMilkProductionController.class
                .getMethod("getMonthlySummary", Long.class, int.class, int.class)
                .getAnnotation(PreAuthorize.class);
        PreAuthorize annualGuard = FarmMilkProductionController.class
                .getMethod("getAnnualSummary", Long.class, int.class)
                .getAnnotation(PreAuthorize.class);

        assertThat(upsertGuard.value()).isEqualTo("@ownershipService.canManageFarm(#farmId)");
        assertThat(dailyGuard.value()).isEqualTo("@ownershipService.canManageFarm(#farmId)");
        assertThat(monthlyGuard.value()).isEqualTo("@ownershipService.canManageFarm(#farmId)");
        assertThat(annualGuard.value()).isEqualTo("@ownershipService.canManageFarm(#farmId)");
    }
}
