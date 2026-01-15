package com.devmaster.goatfarm.reproduction.api.controller;

import com.devmaster.goatfarm.application.ports.in.ReproductionCommandUseCase;
import com.devmaster.goatfarm.application.ports.in.ReproductionQueryUseCase;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.reproduction.api.dto.PregnancyResponseDTO;
import com.devmaster.goatfarm.reproduction.business.bo.PregnancyResponseVO;
import com.devmaster.goatfarm.reproduction.enums.PregnancyStatus;
import com.devmaster.goatfarm.reproduction.mapper.ReproductionMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReproductionController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReproductionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReproductionCommandUseCase commandUseCase;

    @MockBean
    private ReproductionQueryUseCase queryUseCase;

    @MockBean
    private ReproductionMapper mapper;

    @Test
    void getPregnancyById_shouldReturn200_whenExists() throws Exception {
        Long farmId = 1L;
        String goatId = "GOAT-001";
        Long pregnancyId = 10L;

        PregnancyResponseVO responseVO = PregnancyResponseVO.builder()
                .id(pregnancyId)
                .farmId(farmId)
                .goatId(goatId)
                .status(PregnancyStatus.ACTIVE)
                .breedingDate(LocalDate.of(2026, 1, 1))
                .build();

        PregnancyResponseDTO responseDTO = PregnancyResponseDTO.builder()
                .id(pregnancyId)
                .farmId(farmId)
                .goatId(goatId)
                .status(PregnancyStatus.ACTIVE)
                .breedingDate(LocalDate.of(2026, 1, 1))
                .build();

        when(queryUseCase.getPregnancyById(farmId, pregnancyId)).thenReturn(responseVO);
        when(mapper.toPregnancyResponseDTO(responseVO)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/goatfarms/{farmId}/goats/{goatId}/reproduction/pregnancies/{pregnancyId}",
                        farmId, goatId, pregnancyId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(pregnancyId))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void getPregnancyById_shouldReturn404_whenNotFound() throws Exception {
        Long farmId = 1L;
        String goatId = "GOAT-001";
        Long pregnancyId = 999L;

        when(queryUseCase.getPregnancyById(farmId, pregnancyId))
                .thenThrow(new ResourceNotFoundException("Pregnancy not found"));

        mockMvc.perform(get("/api/goatfarms/{farmId}/goats/{goatId}/reproduction/pregnancies/{pregnancyId}",
                        farmId, goatId, pregnancyId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
