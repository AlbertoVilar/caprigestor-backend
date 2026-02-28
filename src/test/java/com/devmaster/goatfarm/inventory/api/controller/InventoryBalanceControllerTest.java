package com.devmaster.goatfarm.inventory.api.controller;

import com.devmaster.goatfarm.config.exceptions.GlobalExceptionHandler;
import com.devmaster.goatfarm.inventory.api.dto.InventoryBalanceResponseDTO;
import com.devmaster.goatfarm.inventory.api.mapper.InventoryBalanceApiMapper;
import com.devmaster.goatfarm.inventory.application.ports.in.InventoryBalanceQueryUseCase;
import com.devmaster.goatfarm.inventory.business.bo.InventoryBalanceFilterVO;
import com.devmaster.goatfarm.inventory.business.bo.InventoryBalanceResponseVO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InventoryBalanceController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class InventoryBalanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InventoryBalanceQueryUseCase queryUseCase;

    @MockBean
    private InventoryBalanceApiMapper apiMapper;

    @MockBean(name = "ownershipService")
    private Object ownershipService;

    @Test
    void listBalances_shouldReturnPagedResult() throws Exception {
        InventoryBalanceResponseVO responseVO = new InventoryBalanceResponseVO(
                101L,
                "Ração Premium",
                true,
                501L,
                new BigDecimal("18.750")
        );
        InventoryBalanceResponseDTO responseDTO = new InventoryBalanceResponseDTO(
                101L,
                "Ração Premium",
                true,
                501L,
                new BigDecimal("18.750")
        );

        when(queryUseCase.listBalances(any(InventoryBalanceFilterVO.class)))
                .thenReturn(new PageImpl<>(List.of(responseVO), PageRequest.of(0, 20), 1));
        when(apiMapper.toResponseDTO(responseVO)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/v1/goatfarms/{farmId}/inventory/balances", 1L)
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].itemId").value(101))
                .andExpect(jsonPath("$.content[0].itemName").value("Ração Premium"))
                .andExpect(jsonPath("$.content[0].trackLot").value(true))
                .andExpect(jsonPath("$.page.totalElements").value(1));

        verify(queryUseCase).listBalances(any(InventoryBalanceFilterVO.class));
    }

    @Test
    void controller_shouldExposeDualMapping_andFarmOwnershipGuard() throws NoSuchMethodException {
        RequestMapping requestMapping = InventoryBalanceController.class.getAnnotation(RequestMapping.class);

        assertThat(requestMapping).isNotNull();
        assertThat(requestMapping.value()).containsExactlyInAnyOrder(
                "/api/v1/goatfarms/{farmId}/inventory/balances",
                "/api/goatfarms/{farmId}/inventory/balances"
        );

        PreAuthorize listGuard = InventoryBalanceController.class
                .getMethod("listBalances", Long.class, Long.class, Long.class, boolean.class, org.springframework.data.domain.Pageable.class)
                .getAnnotation(PreAuthorize.class);

        assertThat(listGuard).isNotNull();
        assertThat(listGuard.value()).isEqualTo("@ownershipService.canManageFarm(#farmId)");
    }
}
