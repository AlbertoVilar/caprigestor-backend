package com.devmaster.goatfarm.health.api.controller;

import com.devmaster.goatfarm.health.api.dto.FarmHealthAlertsResponseDTO;
import com.devmaster.goatfarm.health.api.mapper.FarmHealthAlertsApiMapper;
import com.devmaster.goatfarm.health.api.mapper.HealthEventApiMapper;
import com.devmaster.goatfarm.health.application.ports.in.FarmHealthAlertsQueryUseCase;
import com.devmaster.goatfarm.health.application.ports.in.HealthEventQueryUseCase;
import com.devmaster.goatfarm.health.business.bo.FarmHealthAlertsResponseVO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FarmHealthEventController.class)
@AutoConfigureMockMvc(addFilters = false)
class FarmHealthEventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HealthEventQueryUseCase queryUseCase;

    @MockBean
    private FarmHealthAlertsQueryUseCase alertsQueryUseCase;

    @MockBean
    private HealthEventApiMapper apiMapper;

    @MockBean
    private FarmHealthAlertsApiMapper alertsMapper;

    @Test
    void getAlerts_shouldReturn200_forCanonicalRoute() throws Exception {
        Long farmId = 1L;
        FarmHealthAlertsResponseVO responseVO = FarmHealthAlertsResponseVO.builder()
                .dueTodayCount(2)
                .upcomingCount(5)
                .overdueCount(1)
                .activeMilkWithdrawalCount(1)
                .activeMeatWithdrawalCount(0)
                .dueTodayTop(List.of())
                .upcomingTop(List.of())
                .overdueTop(List.of())
                .milkWithdrawalTop(List.of())
                .meatWithdrawalTop(List.of())
                .windowDays(7)
                .build();
        FarmHealthAlertsResponseDTO responseDTO = new FarmHealthAlertsResponseDTO(
                2,
                5,
                1,
                1,
                0,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                7
        );

        when(alertsQueryUseCase.getAlerts(farmId, 7)).thenReturn(responseVO);
        when(alertsMapper.toDTO(responseVO)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/v1/goatfarms/{farmId}/health-events/alerts", farmId)
                        .param("windowDays", "7")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dueTodayCount").value(2))
                .andExpect(jsonPath("$.activeMilkWithdrawalCount").value(1))
                .andExpect(jsonPath("$.windowDays").value(7));
    }

    @Test
    void getAlerts_shouldReturn200_forLegacyRoute() throws Exception {
        Long farmId = 1L;
        FarmHealthAlertsResponseVO responseVO = FarmHealthAlertsResponseVO.builder()
                .dueTodayCount(1)
                .upcomingCount(3)
                .overdueCount(0)
                .activeMilkWithdrawalCount(0)
                .activeMeatWithdrawalCount(2)
                .dueTodayTop(List.of())
                .upcomingTop(List.of())
                .overdueTop(List.of())
                .milkWithdrawalTop(List.of())
                .meatWithdrawalTop(List.of())
                .windowDays(5)
                .build();
        FarmHealthAlertsResponseDTO responseDTO = new FarmHealthAlertsResponseDTO(
                1,
                3,
                0,
                0,
                2,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                5
        );

        when(alertsQueryUseCase.getAlerts(farmId, 5)).thenReturn(responseVO);
        when(alertsMapper.toDTO(responseVO)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/goatfarms/{farmId}/health-events/alerts", farmId)
                        .param("windowDays", "5")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dueTodayCount").value(1))
                .andExpect(jsonPath("$.activeMeatWithdrawalCount").value(2))
                .andExpect(jsonPath("$.windowDays").value(5));
    }
}
