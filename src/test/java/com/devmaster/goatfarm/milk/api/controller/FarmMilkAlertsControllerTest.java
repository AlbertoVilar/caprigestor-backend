package com.devmaster.goatfarm.milk.api.controller;

import com.devmaster.goatfarm.milk.api.dto.LactationDryOffAlertItemDTO;
import com.devmaster.goatfarm.milk.api.mapper.LactationMapper;
import com.devmaster.goatfarm.milk.application.ports.in.LactationQueryUseCase;
import com.devmaster.goatfarm.milk.business.bo.LactationDryOffAlertVO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FarmMilkAlertsController.class)
@AutoConfigureMockMvc(addFilters = false)
class FarmMilkAlertsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LactationQueryUseCase lactationQueryUseCase;

    @MockBean
    private LactationMapper lactationMapper;

    @Test
    void getDryOffAlerts_shouldReturn200_forCanonicalRoute() throws Exception {
        Long farmId = 1L;
        LocalDate referenceDate = LocalDate.of(2026, 2, 28);
        LactationDryOffAlertVO alertVO = LactationDryOffAlertVO.builder()
                .lactationId(120L)
                .goatId("BR123")
                .gestationDays(145)
                .daysOverdue(5)
                .dryOffRecommendation(true)
                .build();
        LactationDryOffAlertItemDTO alertDTO = LactationDryOffAlertItemDTO.builder()
                .lactationId(alertVO.getLactationId())
                .goatId(alertVO.getGoatId())
                .gestationDays(alertVO.getGestationDays())
                .daysOverdue(alertVO.getDaysOverdue())
                .dryOffRecommendation(alertVO.isDryOffRecommendation())
                .build();

        when(lactationQueryUseCase.getDryOffAlerts(eq(farmId), eq(referenceDate), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(alertVO), PageRequest.of(0, 20), 1));
        when(lactationMapper.toDryOffAlertItemDTO(alertVO)).thenReturn(alertDTO);

        mockMvc.perform(get("/api/v1/goatfarms/{farmId}/milk/alerts/dry-off", farmId)
                        .param("referenceDate", "2026-02-28")
                        .param("page", "0")
                        .param("size", "20")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPending").value(1))
                .andExpect(jsonPath("$.alerts[0].goatId").value("BR123"));
    }

    @Test
    void getDryOffAlerts_shouldReturn200_forLegacyRoute() throws Exception {
        Long farmId = 1L;
        LocalDate referenceDate = LocalDate.of(2026, 2, 28);
        LactationDryOffAlertVO alertVO = LactationDryOffAlertVO.builder()
                .lactationId(121L)
                .goatId("BR999")
                .gestationDays(150)
                .daysOverdue(2)
                .dryOffRecommendation(true)
                .build();
        LactationDryOffAlertItemDTO alertDTO = LactationDryOffAlertItemDTO.builder()
                .lactationId(alertVO.getLactationId())
                .goatId(alertVO.getGoatId())
                .gestationDays(alertVO.getGestationDays())
                .daysOverdue(alertVO.getDaysOverdue())
                .dryOffRecommendation(alertVO.isDryOffRecommendation())
                .build();

        when(lactationQueryUseCase.getDryOffAlerts(eq(farmId), eq(referenceDate), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(alertVO), PageRequest.of(0, 20), 1));
        when(lactationMapper.toDryOffAlertItemDTO(alertVO)).thenReturn(alertDTO);

        mockMvc.perform(get("/api/goatfarms/{farmId}/milk/alerts/dry-off", farmId)
                        .param("referenceDate", "2026-02-28")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPending").value(1))
                .andExpect(jsonPath("$.alerts[0].goatId").value("BR999"));
    }
}