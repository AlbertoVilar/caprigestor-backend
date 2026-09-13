package com.devmaster.goatfarm.milk.api.controller;

import com.devmaster.goatfarm.milk.api.dto.MilkProductionResponseDTO;
import com.devmaster.goatfarm.milk.api.mapper.MilkProductionMapper;
import com.devmaster.goatfarm.milk.application.ports.in.MilkProductionUseCase;
import com.devmaster.goatfarm.application.pagination.PageQuery;
import com.devmaster.goatfarm.application.pagination.PageResult;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MilkProductionController.class)
@AutoConfigureMockMvc(addFilters = false)
class MilkProductionPaginationCharacterizationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MilkProductionUseCase milkProductionUseCase;

    @MockBean
    private MilkProductionMapper milkProductionMapper;

    @Test
    void listUsesCurrentDefaultsAndSpringPageContract() throws Exception {
        Long farmId = 1L;
        String goatId = "BR123";
        MilkProductionResponseVO vo = response(21L, LocalDate.of(2026, 2, 20));
        MilkProductionResponseDTO dto = dto(vo);
        when(milkProductionUseCase.getMilkProductions(
                eq(farmId), eq(goatId), eq(null), eq(null),
                argThat(query -> query.page() == 0
                        && query.size() == 12
                        && query.sort().stream().anyMatch(sort -> sort.field().equals("date"))
                        && query.sort().stream().anyMatch(sort -> sort.field().equals("shift"))
                        && query.sort().stream().anyMatch(sort -> sort.field().equals("id"))),
                eq(false)))
                .thenReturn(new PageResult<>(List.of(vo), 25, 0, 12));
        when(milkProductionMapper.toResponseDTO(vo)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/goatfarms/{farmId}/goats/{goatId}/milk-productions", farmId, goatId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(21))
                .andExpect(jsonPath("$.page.totalElements").value(25))
                .andExpect(jsonPath("$.page.totalPages").value(3))
                .andExpect(jsonPath("$.page.number").value(0))
                .andExpect(jsonPath("$.page.size").value(12));

        verify(milkProductionUseCase).getMilkProductions(
                eq(farmId), eq(goatId), eq(null), eq(null),
                argThat(query -> query.page() == 0 && query.size() == 12), eq(false));
    }

    @Test
    void listPreservesExplicitPageFiltersSortAndIncludeCanceled() throws Exception {
        Long farmId = 2L;
        String goatId = "BR456";
        MilkProductionResponseVO vo = response(22L, LocalDate.of(2026, 2, 22));
        when(milkProductionUseCase.getMilkProductions(
                eq(farmId), eq(goatId), eq(LocalDate.of(2026, 2, 1)), eq(LocalDate.of(2026, 2, 28)),
                argThat(query -> query.page() == 2
                        && query.size() == 5
                        && query.sort().stream().anyMatch(sort -> sort.field().equals("volumeLiters"))
                        && query.sort().stream().anyMatch(sort -> sort.field().equals("date"))),
                eq(true)))
                .thenReturn(new PageResult<>(List.of(vo), 11, 2, 5));
        when(milkProductionMapper.toResponseDTO(vo)).thenReturn(dto(vo));

        mockMvc.perform(get("/api/v1/goatfarms/{farmId}/goats/{goatId}/milk-productions", farmId, goatId)
                        .param("from", "2026-02-01")
                        .param("to", "2026-02-28")
                        .param("includeCanceled", "true")
                        .param("page", "2")
                        .param("size", "5")
                        .param("sort", "volumeLiters,asc")
                        .param("sort", "date,desc")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalElements").value(11))
                .andExpect(jsonPath("$.page.totalPages").value(3))
                .andExpect(jsonPath("$.page.number").value(2))
                .andExpect(jsonPath("$.page.size").value(5));
    }

    private MilkProductionResponseVO response(Long id, LocalDate date) {
        return MilkProductionResponseVO.builder().id(id).date(date).shift(MilkingShift.MORNING)
                .volumeLiters(new BigDecimal("2.45")).status(MilkProductionStatus.ACTIVE).build();
    }

    private MilkProductionResponseDTO dto(MilkProductionResponseVO vo) {
        return MilkProductionResponseDTO.builder().id(vo.getId()).date(vo.getDate()).shift(vo.getShift())
                .volumeLiters(vo.getVolumeLiters()).status(vo.getStatus()).build();
    }
}
