package com.devmaster.goatfarm.inventory.api.controller;

import com.devmaster.goatfarm.config.exceptions.GlobalExceptionHandler;
import com.devmaster.goatfarm.inventory.api.dto.InventoryLotActivationRequestDTO;
import com.devmaster.goatfarm.inventory.api.dto.InventoryLotCreateRequestDTO;
import com.devmaster.goatfarm.inventory.api.dto.InventoryLotResponseDTO;
import com.devmaster.goatfarm.inventory.api.mapper.InventoryLotApiMapper;
import com.devmaster.goatfarm.inventory.application.ports.in.InventoryLotCommandUseCase;
import com.devmaster.goatfarm.inventory.application.ports.in.InventoryLotQueryUseCase;
import com.devmaster.goatfarm.inventory.business.bo.InventoryLotActivationRequestVO;
import com.devmaster.goatfarm.inventory.business.bo.InventoryLotCreateRequestVO;
import com.devmaster.goatfarm.inventory.business.bo.InventoryLotFilterVO;
import com.devmaster.goatfarm.inventory.business.bo.InventoryLotResponseVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InventoryLotController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class InventoryLotControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private InventoryLotCommandUseCase commandUseCase;

    @MockBean
    private InventoryLotQueryUseCase queryUseCase;

    @MockBean
    private InventoryLotApiMapper apiMapper;

    @MockBean(name = "ownershipService")
    private Object ownershipService;

    @Test
    void createLot_shouldReturn201() throws Exception {
        InventoryLotCreateRequestDTO request = new InventoryLotCreateRequestDTO(
                101L,
                "RACAO-2026-03",
                "Entrega março",
                LocalDate.of(2026, 9, 30),
                true
        );
        InventoryLotCreateRequestVO requestVO = new InventoryLotCreateRequestVO(
                101L,
                "RACAO-2026-03",
                "Entrega março",
                LocalDate.of(2026, 9, 30),
                true
        );
        InventoryLotResponseVO responseVO = new InventoryLotResponseVO(
                501L,
                1L,
                101L,
                "RACAO-2026-03",
                "Entrega março",
                LocalDate.of(2026, 9, 30),
                true
        );
        InventoryLotResponseDTO responseDTO = new InventoryLotResponseDTO(
                501L,
                1L,
                101L,
                "RACAO-2026-03",
                "Entrega março",
                LocalDate.of(2026, 9, 30),
                true
        );

        when(apiMapper.toCreateRequestVO(request)).thenReturn(requestVO);
        when(commandUseCase.createLot(1L, requestVO)).thenReturn(responseVO);
        when(apiMapper.toResponseDTO(responseVO)).thenReturn(responseDTO);

        mockMvc.perform(post("/api/v1/goatfarms/{farmId}/inventory/lots", 1L)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(501))
                .andExpect(jsonPath("$.code").value("RACAO-2026-03"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void listLots_shouldReturnPagedLots() throws Exception {
        InventoryLotResponseVO responseVO = new InventoryLotResponseVO(
                501L,
                1L,
                101L,
                "RACAO-2026-03",
                "Entrega março",
                null,
                true
        );
        InventoryLotResponseDTO responseDTO = new InventoryLotResponseDTO(
                501L,
                1L,
                101L,
                "RACAO-2026-03",
                "Entrega março",
                null,
                true
        );

        when(queryUseCase.listLots(any(InventoryLotFilterVO.class)))
                .thenReturn(new PageImpl<>(List.of(responseVO), PageRequest.of(0, 20), 1));
        when(apiMapper.toResponseDTO(responseVO)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/v1/goatfarms/{farmId}/inventory/lots", 1L)
                        .param("itemId", "101")
                        .param("active", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(501))
                .andExpect(jsonPath("$.content[0].itemId").value(101))
                .andExpect(jsonPath("$.page.totalElements").value(1));

        verify(queryUseCase).listLots(any(InventoryLotFilterVO.class));
    }

    @Test
    void updateLotActive_shouldReturn200() throws Exception {
        InventoryLotActivationRequestDTO request = new InventoryLotActivationRequestDTO(false);
        InventoryLotActivationRequestVO requestVO = new InventoryLotActivationRequestVO(false);
        InventoryLotResponseVO responseVO = new InventoryLotResponseVO(501L, 1L, 101L, "RACAO-2026-03", null, null, false);
        InventoryLotResponseDTO responseDTO = new InventoryLotResponseDTO(501L, 1L, 101L, "RACAO-2026-03", null, null, false);

        when(apiMapper.toActivationRequestVO(request)).thenReturn(requestVO);
        when(commandUseCase.updateLotActive(1L, 501L, requestVO)).thenReturn(responseVO);
        when(apiMapper.toResponseDTO(responseVO)).thenReturn(responseDTO);

        mockMvc.perform(patch("/api/v1/goatfarms/{farmId}/inventory/lots/{lotId}/active", 1L, 501L)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void controller_shouldExposeDualMapping_andFarmOwnershipGuard() throws NoSuchMethodException {
        RequestMapping requestMapping = InventoryLotController.class.getAnnotation(RequestMapping.class);

        assertThat(requestMapping).isNotNull();
        assertThat(requestMapping.value()).containsExactlyInAnyOrder(
                "/api/v1/goatfarms/{farmId}/inventory/lots",
                "/api/goatfarms/{farmId}/inventory/lots"
        );

        PreAuthorize createGuard = InventoryLotController.class
                .getMethod("createLot", Long.class, InventoryLotCreateRequestDTO.class)
                .getAnnotation(PreAuthorize.class);
        PreAuthorize listGuard = InventoryLotController.class
                .getMethod("listLots", Long.class, Long.class, Boolean.class, Pageable.class)
                .getAnnotation(PreAuthorize.class);
        PreAuthorize updateGuard = InventoryLotController.class
                .getMethod("updateLotActive", Long.class, Long.class, InventoryLotActivationRequestDTO.class)
                .getAnnotation(PreAuthorize.class);

        assertThat(createGuard).isNotNull();
        assertThat(listGuard).isNotNull();
        assertThat(updateGuard).isNotNull();
        assertThat(createGuard.value()).isEqualTo("@ownershipService.canManageFarm(#farmId)");
        assertThat(listGuard.value()).isEqualTo("@ownershipService.canManageFarm(#farmId)");
        assertThat(updateGuard.value()).isEqualTo("@ownershipService.canManageFarm(#farmId)");
    }
}
