package com.devmaster.goatfarm.reproduction.api.controller;

import com.devmaster.goatfarm.reproduction.api.dto.PregnancyDiagnosisAlertItemDTO;
import com.devmaster.goatfarm.reproduction.api.dto.PregnancyDueAlertItemDTO;
import com.devmaster.goatfarm.reproduction.api.mapper.ReproductionMapper;
import com.devmaster.goatfarm.reproduction.application.ports.in.ReproductionQueryUseCase;
import com.devmaster.goatfarm.reproduction.business.bo.PregnancyDiagnosisAlertVO;
import com.devmaster.goatfarm.reproduction.business.bo.PregnancyDueAlertVO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FarmReproductionAlertsController.class)
@AutoConfigureMockMvc(addFilters = false)
class FarmReproductionAlertsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReproductionQueryUseCase queryUseCase;

    @MockBean
    private ReproductionMapper mapper;

    @Test
    void securityGuard_shouldUseFarmManagementPolicy() {
        PreAuthorize guard = FarmReproductionAlertsController.class.getAnnotation(PreAuthorize.class);

        assertThat(guard).isNotNull();
        assertThat(guard.value()).isEqualTo("@ownershipService.canManageFarm(#farmId)");
    }

    @Test
    void getPendingPregnancyDiagnosisAlerts_shouldReturn200_forCanonicalRoute() throws Exception {
        Long farmId = 1L;
        LocalDate referenceDate = LocalDate.of(2026, 2, 28);
        PregnancyDiagnosisAlertVO alertVO = PregnancyDiagnosisAlertVO.builder()
                .goatId("BR123")
                .eligibleDate(LocalDate.of(2026, 2, 25))
                .daysOverdue(3)
                .lastCoverageDate(LocalDate.of(2026, 1, 15))
                .build();
        PregnancyDiagnosisAlertItemDTO alertDTO = PregnancyDiagnosisAlertItemDTO.builder()
                .goatId(alertVO.getGoatId())
                .eligibleDate(alertVO.getEligibleDate())
                .daysOverdue(alertVO.getDaysOverdue())
                .lastCoverageDate(alertVO.getLastCoverageDate())
                .build();

        when(queryUseCase.getPendingPregnancyDiagnosisAlerts(eq(farmId), eq(referenceDate), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(alertVO), PageRequest.of(0, 20), 1));
        when(mapper.toPregnancyDiagnosisAlertItemDTO(alertVO)).thenReturn(alertDTO);

        mockMvc.perform(get("/api/v1/goatfarms/{farmId}/reproduction/alerts/pregnancy-diagnosis", farmId)
                        .param("referenceDate", "2026-02-28")
                        .param("page", "0")
                        .param("size", "20")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPending").value(1))
                .andExpect(jsonPath("$.alerts[0].goatId").value("BR123"))
                .andExpect(jsonPath("$.alerts[0].daysOverdue").value(3));
    }

    @Test
    void getPendingPregnancyDiagnosisAlerts_shouldReturn200_forLegacyRoute() throws Exception {
        Long farmId = 1L;
        PregnancyDiagnosisAlertVO alertVO = PregnancyDiagnosisAlertVO.builder()
                .goatId("BR999")
                .eligibleDate(LocalDate.of(2026, 2, 20))
                .daysOverdue(8)
                .build();
        PregnancyDiagnosisAlertItemDTO alertDTO = PregnancyDiagnosisAlertItemDTO.builder()
                .goatId(alertVO.getGoatId())
                .eligibleDate(alertVO.getEligibleDate())
                .daysOverdue(alertVO.getDaysOverdue())
                .build();

        when(queryUseCase.getPendingPregnancyDiagnosisAlerts(eq(farmId), isNull(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(alertVO), PageRequest.of(0, 20), 1));
        when(mapper.toPregnancyDiagnosisAlertItemDTO(alertVO)).thenReturn(alertDTO);

        mockMvc.perform(get("/api/v1/goatfarms/{farmId}/reproduction/alerts/pregnancy-diagnosis", farmId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPending").value(1))
                .andExpect(jsonPath("$.alerts[0].goatId").value("BR999"));
    }

    @Test
    void getPendingBirthAlerts_shouldReturn200() throws Exception {
        Long farmId = 14L;
        LocalDate referenceDate = LocalDate.of(2026, 7, 3);
        PregnancyDueAlertVO alertVO = PregnancyDueAlertVO.builder()
                .pregnancyId(27L)
                .goatId("1615325001")
                .expectedDueDate(LocalDate.of(2026, 7, 1))
                .daysOverdue(2)
                .build();
        PregnancyDueAlertItemDTO alertDTO = PregnancyDueAlertItemDTO.builder()
                .pregnancyId(alertVO.getPregnancyId())
                .goatId(alertVO.getGoatId())
                .expectedDueDate(alertVO.getExpectedDueDate())
                .daysOverdue(alertVO.getDaysOverdue())
                .build();

        when(queryUseCase.getPendingBirthAlerts(eq(farmId), eq(referenceDate), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(alertVO), PageRequest.of(0, 20), 1));
        when(mapper.toPregnancyDueAlertItemDTO(alertVO)).thenReturn(alertDTO);

        mockMvc.perform(get("/api/v1/goatfarms/{farmId}/reproduction/alerts/births-due", farmId)
                        .param("referenceDate", "2026-07-03")
                        .param("page", "0")
                        .param("size", "20")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPending").value(1))
                .andExpect(jsonPath("$.alerts[0].pregnancyId").value(27))
                .andExpect(jsonPath("$.alerts[0].goatId").value("1615325001"))
                .andExpect(jsonPath("$.alerts[0].daysOverdue").value(2));
    }

    @Test
    void getPendingBirthAlerts_shouldReturn400_whenPageSizeExceedsLimit() throws Exception {
        mockMvc.perform(get("/api/v1/goatfarms/{farmId}/reproduction/alerts/births-due", 14L)
                        .param("page", "0")
                        .param("size", "101")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getPendingPregnancyDiagnosisAlerts_shouldReturn400_whenPageIsNegative() throws Exception {
        mockMvc.perform(get("/api/v1/goatfarms/{farmId}/reproduction/alerts/pregnancy-diagnosis", 14L)
                        .param("page", "-1")
                        .param("size", "20")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
