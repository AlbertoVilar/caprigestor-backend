package com.devmaster.goatfarm.farm.api;

import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.config.security.OwnershipService;
import com.devmaster.goatfarm.farm.api.controller.GoatFarmController;
import com.devmaster.goatfarm.farm.application.ports.in.GoatFarmManagementUseCase;
import com.devmaster.goatfarm.farm.business.bo.GoatFarmFullResponseVO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = true)
@Import({com.devmaster.goatfarm.config.exceptions.GlobalExceptionHandler.class})
@ActiveProfiles("test")
class GoatFarmControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GoatFarmController goatFarmController;

    @MockBean
    private GoatFarmManagementUseCase farmUseCase;

    @MockBean
    private com.devmaster.goatfarm.authority.business.AdminMaintenanceBusiness adminMaintenanceBusiness;

    @MockBean
    private OwnershipService ownershipService;

    private GoatFarmFullResponseVO farmResponse;

    @BeforeEach
    void setUp() {
        farmResponse = new GoatFarmFullResponseVO();
        farmResponse.setId(1L);
        farmResponse.setName("Capril Vilar");
        farmResponse.setTod("CV001");
    }

    @Test
    void contextLoads() {
        Assertions.assertNotNull(goatFarmController, "GoatFarmController não foi carregado no contexto de teste");
    }

    @Test
    void shouldListGoatFarmsThroughCanonicalRoute() throws Exception {
        Page<GoatFarmFullResponseVO> farms = new PageImpl<>(
                List.of(farmResponse),
                PageRequest.of(0, 10),
                1
        );

        when(farmUseCase.findAllGoatFarm(any(Pageable.class))).thenReturn(farms);

        mockMvc.perform(get("/api/v1/goatfarms")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Capril Vilar"))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(farmUseCase).findAllGoatFarm(any(Pageable.class));
    }

    @Test
    void shouldAllowLegacyGoatFarmListRouteDuringCompatibilityWindow() throws Exception {
        Page<GoatFarmFullResponseVO> farms = new PageImpl<>(
                List.of(farmResponse),
                PageRequest.of(0, 10),
                1
        );

        when(farmUseCase.findAllGoatFarm(any(Pageable.class))).thenReturn(farms);

        mockMvc.perform(get("/api/goatfarms")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Capril Vilar"))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(farmUseCase).findAllGoatFarm(any(Pageable.class));
    }

    @Test
    void shouldReturnNotFoundWhenGoatFarmDoesNotExist() throws Exception {
        when(farmUseCase.findGoatFarmById(eq(999L)))
                .thenThrow(new ResourceNotFoundException("Fazenda não encontrada com id: 999"));

        mockMvc.perform(get("/api/v1/goatfarms/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Recurso não encontrado"))
                .andExpect(jsonPath("$.errors[0].fieldName").value("resource"))
                .andExpect(jsonPath("$.errors[0].message").value("Fazenda não encontrada com id: 999"));

        verify(farmUseCase).findGoatFarmById(999L);
    }
}
