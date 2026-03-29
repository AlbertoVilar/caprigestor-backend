package com.devmaster.goatfarm.milk.api.controller;

import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.milk.api.dto.MilkProductionResponseDTO;
import com.devmaster.goatfarm.milk.api.mapper.MilkProductionMapper;
import com.devmaster.goatfarm.milk.application.ports.in.MilkProductionUseCase;
import com.devmaster.goatfarm.milk.business.bo.MilkProductionResponseVO;
import com.devmaster.goatfarm.milk.enums.MilkProductionStatus;
import com.devmaster.goatfarm.milk.enums.MilkingShift;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MilkProductionController.class)
@AutoConfigureMockMvc(addFilters = false)
class MilkProductionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MilkProductionUseCase milkProductionUseCase;

    @MockBean
    private MilkProductionMapper milkProductionMapper;

    @Test
    void findById_shouldReturn200_whenProductionExists() throws Exception {
        Long farmId = 1L;
        String goatId = "BR123";
        Long productionId = 21L;

        MilkProductionResponseVO responseVO = MilkProductionResponseVO.builder()
                .id(productionId)
                .date(LocalDate.of(2026, 2, 20))
                .shift(MilkingShift.MORNING)
                .volumeLiters(new BigDecimal("2.45"))
                .status(MilkProductionStatus.ACTIVE)
                .recordedDuringMilkWithdrawal(true)
                .milkWithdrawalEventId(90L)
                .milkWithdrawalEndDate(LocalDate.of(2026, 2, 24))
                .milkWithdrawalSource("Antibiotico QA")
                .build();
        MilkProductionResponseDTO responseDTO = MilkProductionResponseDTO.builder()
                .id(responseVO.getId())
                .date(responseVO.getDate())
                .shift(responseVO.getShift())
                .volumeLiters(responseVO.getVolumeLiters())
                .status(responseVO.getStatus())
                .recordedDuringMilkWithdrawal(responseVO.isRecordedDuringMilkWithdrawal())
                .milkWithdrawalEventId(responseVO.getMilkWithdrawalEventId())
                .milkWithdrawalEndDate(responseVO.getMilkWithdrawalEndDate())
                .milkWithdrawalSource(responseVO.getMilkWithdrawalSource())
                .build();

        when(milkProductionUseCase.findById(farmId, goatId, productionId)).thenReturn(responseVO);
        when(milkProductionMapper.toResponseDTO(responseVO)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/v1/goatfarms/{farmId}/goats/{goatId}/milk-productions/{id}", farmId, goatId, productionId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(21))
                .andExpect(jsonPath("$.shift").value(MilkingShift.MORNING.name()))
                .andExpect(jsonPath("$.recordedDuringMilkWithdrawal").value(true))
                .andExpect(jsonPath("$.milkWithdrawalSource").value("Antibiotico QA"));
    }

    @Test
    void findById_shouldReturn404_whenProductionDoesNotExist() throws Exception {
        Long farmId = 1L;
        String goatId = "BR123";
        Long productionId = 999L;

        when(milkProductionUseCase.findById(farmId, goatId, productionId))
                .thenThrow(new ResourceNotFoundException("Milk production not found with id: " + productionId));

        mockMvc.perform(get("/api/v1/goatfarms/{farmId}/goats/{goatId}/milk-productions/{id}", farmId, goatId, productionId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
