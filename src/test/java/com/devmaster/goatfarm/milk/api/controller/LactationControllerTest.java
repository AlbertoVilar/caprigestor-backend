package com.devmaster.goatfarm.milk.api.controller;

import com.devmaster.goatfarm.milk.api.dto.LactationResponseDTO;
import com.devmaster.goatfarm.milk.api.mapper.LactationMapper;
import com.devmaster.goatfarm.milk.application.ports.in.LactationCommandUseCase;
import com.devmaster.goatfarm.milk.application.ports.in.LactationQueryUseCase;
import com.devmaster.goatfarm.milk.business.bo.LactationResponseVO;
import com.devmaster.goatfarm.milk.enums.LactationStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LactationController.class)
@AutoConfigureMockMvc(addFilters = false)
class LactationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LactationQueryUseCase lactationQueryUseCase;

    @MockBean
    private LactationCommandUseCase lactationCommandUseCase;

    @MockBean
    private LactationMapper lactationMapper;

    @Test
    void getActiveLactation_shouldReturn200_forCanonicalRoute() throws Exception {
        Long farmId = 1L;
        String goatId = "BR123";

        LactationResponseVO responseVO = LactationResponseVO.builder()
                .id(12L)
                .farmId(farmId)
                .goatId(goatId)
                .status(LactationStatus.ACTIVE)
                .startDate(LocalDate.of(2026, 1, 10))
                .build();
        LactationResponseDTO responseDTO = LactationResponseDTO.builder()
                .id(responseVO.getId())
                .farmId(responseVO.getFarmId())
                .goatId(responseVO.getGoatId())
                .status(responseVO.getStatus())
                .startDate(responseVO.getStartDate())
                .build();

        when(lactationQueryUseCase.getActiveLactation(farmId, goatId)).thenReturn(responseVO);
        when(lactationMapper.toResponseDTO(responseVO)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/v1/goatfarms/{farmId}/goats/{goatId}/lactations/active", farmId, goatId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(12))
                .andExpect(jsonPath("$.status").value(LactationStatus.ACTIVE.name()));
    }

    @Test
    void getActiveLactation_shouldReturn200_forLegacyRoute() throws Exception {
        Long farmId = 1L;
        String goatId = "BR123";

        LactationResponseVO responseVO = LactationResponseVO.builder()
                .id(13L)
                .farmId(farmId)
                .goatId(goatId)
                .status(LactationStatus.ACTIVE)
                .startDate(LocalDate.of(2026, 1, 11))
                .build();
        LactationResponseDTO responseDTO = LactationResponseDTO.builder()
                .id(responseVO.getId())
                .farmId(responseVO.getFarmId())
                .goatId(responseVO.getGoatId())
                .status(responseVO.getStatus())
                .startDate(responseVO.getStartDate())
                .build();

        when(lactationQueryUseCase.getActiveLactation(farmId, goatId)).thenReturn(responseVO);
        when(lactationMapper.toResponseDTO(responseVO)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/goatfarms/{farmId}/goats/{goatId}/lactations/active", farmId, goatId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(13));
    }

    @Test
    void resumeLactation_shouldReturn200_forCanonicalRoute() throws Exception {
        Long farmId = 1L;
        String goatId = "BR123";
        Long lactationId = 14L;

        LactationResponseVO responseVO = LactationResponseVO.builder()
                .id(lactationId)
                .farmId(farmId)
                .goatId(goatId)
                .status(LactationStatus.ACTIVE)
                .startDate(LocalDate.of(2025, 11, 15))
                .build();
        LactationResponseDTO responseDTO = LactationResponseDTO.builder()
                .id(responseVO.getId())
                .farmId(responseVO.getFarmId())
                .goatId(responseVO.getGoatId())
                .status(responseVO.getStatus())
                .startDate(responseVO.getStartDate())
                .build();

        when(lactationCommandUseCase.resumeLactation(farmId, goatId, lactationId)).thenReturn(responseVO);
        when(lactationMapper.toResponseDTO(responseVO)).thenReturn(responseDTO);

        mockMvc.perform(patch("/api/v1/goatfarms/{farmId}/goats/{goatId}/lactations/{lactationId}/resume", farmId, goatId, lactationId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(14))
                .andExpect(jsonPath("$.status").value(LactationStatus.ACTIVE.name()));
    }
}
