package com.devmaster.goatfarm.inventory.api.controller;

import com.devmaster.goatfarm.config.exceptions.DuplicateEntityException;
import com.devmaster.goatfarm.config.exceptions.GlobalExceptionHandler;
import com.devmaster.goatfarm.inventory.api.dto.InventoryItemCreateRequestDTO;
import com.devmaster.goatfarm.inventory.api.dto.InventoryItemResponseDTO;
import com.devmaster.goatfarm.inventory.api.mapper.InventoryItemApiMapper;
import com.devmaster.goatfarm.inventory.application.ports.in.InventoryItemCommandUseCase;
import com.devmaster.goatfarm.inventory.application.ports.in.InventoryItemQueryUseCase;
import com.devmaster.goatfarm.inventory.business.bo.InventoryItemCreateRequestVO;
import com.devmaster.goatfarm.inventory.business.bo.InventoryItemResponseVO;
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
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InventoryItemController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class InventoryItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private InventoryItemCommandUseCase commandUseCase;

    @MockBean
    private InventoryItemQueryUseCase queryUseCase;

    @MockBean
    private InventoryItemApiMapper apiMapper;

    @MockBean(name = "ownershipService")
    private Object ownershipService;

    @Test
    void createItem_shouldReturn201_whenItemIsCreated() throws Exception {
        InventoryItemCreateRequestDTO request = new InventoryItemCreateRequestDTO("Ração Premium", true);
        InventoryItemCreateRequestVO requestVO = new InventoryItemCreateRequestVO("Ração Premium", true);
        InventoryItemResponseVO responseVO = new InventoryItemResponseVO(101L, 1L, "Ração Premium", true, true);
        InventoryItemResponseDTO responseDTO = new InventoryItemResponseDTO(101L, "Ração Premium", true, true);

        when(apiMapper.toCreateRequestVO(request)).thenReturn(requestVO);
        when(commandUseCase.createItem(1L, requestVO)).thenReturn(responseVO);
        when(apiMapper.toResponseDTO(responseVO)).thenReturn(responseDTO);

        mockMvc.perform(post("/api/v1/goatfarms/{farmId}/inventory/items", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(101))
                .andExpect(jsonPath("$.name").value("Ração Premium"))
                .andExpect(jsonPath("$.trackLot").value(true))
                .andExpect(jsonPath("$.active").value(true));

        verify(commandUseCase).createItem(1L, requestVO);
    }

    @Test
    void listItems_shouldReturnPagedResult() throws Exception {
        PageRequest pageable = PageRequest.of(0, 20);
        InventoryItemResponseVO responseVO = new InventoryItemResponseVO(77L, 2L, "Milho", false, true);
        InventoryItemResponseDTO responseDTO = new InventoryItemResponseDTO(77L, "Milho", false, true);

        when(queryUseCase.listItems(eq(2L), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(responseVO), pageable, 1));
        when(apiMapper.toResponseDTO(responseVO)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/v1/goatfarms/{farmId}/inventory/items", 2L)
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(77))
                .andExpect(jsonPath("$.content[0].name").value("Milho"))
                .andExpect(jsonPath("$.page.totalElements").value(1));
    }

    @Test
    void createItem_shouldReturn409_whenDuplicateNameExists() throws Exception {
        InventoryItemCreateRequestDTO request = new InventoryItemCreateRequestDTO("Ração Premium", false);
        InventoryItemCreateRequestVO requestVO = new InventoryItemCreateRequestVO("Ração Premium", false);

        when(apiMapper.toCreateRequestVO(request)).thenReturn(requestVO);
        when(commandUseCase.createItem(1L, requestVO)).thenThrow(
                new DuplicateEntityException("name", "Já existe um item de estoque com esse nome nesta fazenda.")
        );

        mockMvc.perform(post("/api/v1/goatfarms/{farmId}/inventory/items", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errors[0].fieldName").value("name"))
                .andExpect(jsonPath("$.errors[0].message").value("Já existe um item de estoque com esse nome nesta fazenda."));
    }

    @Test
    void controller_shouldExposeDualMapping_andFarmOwnershipGuard() throws NoSuchMethodException {
        RequestMapping requestMapping = InventoryItemController.class.getAnnotation(RequestMapping.class);

        assertThat(requestMapping).isNotNull();
        assertThat(requestMapping.value()).containsExactlyInAnyOrder(
                "/api/v1/goatfarms/{farmId}/inventory/items",
                "/api/goatfarms/{farmId}/inventory/items"
        );

        PreAuthorize createGuard = InventoryItemController.class
                .getMethod("createItem", Long.class, InventoryItemCreateRequestDTO.class)
                .getAnnotation(PreAuthorize.class);
        PreAuthorize listGuard = InventoryItemController.class
                .getMethod("listItems", Long.class, Pageable.class)
                .getAnnotation(PreAuthorize.class);

        assertThat(createGuard).isNotNull();
        assertThat(listGuard).isNotNull();
        assertThat(createGuard.value()).isEqualTo("@ownershipService.canManageFarm(#farmId)");
        assertThat(listGuard.value()).isEqualTo("@ownershipService.canManageFarm(#farmId)");
    }
}
