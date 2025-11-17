package com.devmaster.goatfarm.goat.api.controller;

import com.devmaster.goatfarm.goat.api.dto.GoatRequestDTO;
import com.devmaster.goatfarm.goat.api.dto.GoatResponseDTO;
import com.devmaster.goatfarm.goat.business.bo.GoatRequestVO;
import com.devmaster.goatfarm.goat.business.bo.GoatResponseVO;
import com.devmaster.goatfarm.application.ports.in.GoatManagementUseCase;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.devmaster.goatfarm.goat.mapper.GoatMapperImpl;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;

@WebMvcTest(GoatController.class)
@Import({GoatMapperImpl.class, GoatControllerTest.TestMethodSecurityConfig.class})
class GoatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GoatManagementUseCase goatUseCase;

    @MockBean
    private JpaMetamodelMappingContext jpaMappingContext;

    private GoatResponseDTO goatResponseDTO;
    private GoatResponseVO goatResponseVO;
    private GoatRequestDTO goatRequestDTO;

    @BeforeEach
    void setUp() {
        goatResponseDTO = new GoatResponseDTO();
        goatResponseDTO.setRegistrationNumber("001");
        goatResponseDTO.setName("Cabra Teste");
        goatResponseDTO.setBreed(com.devmaster.goatfarm.goat.enums.GoatBreed.SAANEN);
        goatResponseDTO.setGender(com.devmaster.goatfarm.goat.enums.Gender.FEMEA);
        goatResponseDTO.setBirthDate(LocalDate.of(2020, 1, 15));
        goatResponseDTO.setColor("Branca");
        goatResponseDTO.setStatus(com.devmaster.goatfarm.goat.enums.GoatStatus.ATIVO);

        goatResponseVO = new GoatResponseVO();
        goatResponseVO.setRegistrationNumber("001");
        goatResponseVO.setName("Cabra Teste");
        goatResponseVO.setBreed(com.devmaster.goatfarm.goat.enums.GoatBreed.SAANEN);
        goatResponseVO.setGender(com.devmaster.goatfarm.goat.enums.Gender.FEMEA);
        goatResponseVO.setBirthDate(LocalDate.of(2020, 1, 15));
        goatResponseVO.setColor("Branca");
        goatResponseVO.setStatus(com.devmaster.goatfarm.goat.enums.GoatStatus.ATIVO);
        
        goatRequestDTO = new GoatRequestDTO();
        goatRequestDTO.setRegistrationNumber("001");
        goatRequestDTO.setName("Cabra Teste");
        goatRequestDTO.setBreed(com.devmaster.goatfarm.goat.enums.GoatBreed.SAANEN);
        goatRequestDTO.setGender(com.devmaster.goatfarm.goat.enums.Gender.FEMEA);
        goatRequestDTO.setBirthDate(LocalDate.of(2020, 1, 15));
        goatRequestDTO.setColor("Branca");
        goatRequestDTO.setStatus(com.devmaster.goatfarm.goat.enums.GoatStatus.ATIVO);
    }

    @Test
@WithMockUser(roles = "OPERATOR")
    void shouldGetAllGoatsSuccessfully() throws Exception {
        // Arrange
        List<GoatResponseVO> goats = Arrays.asList(goatResponseVO);
        Page<GoatResponseVO> goatPage = new PageImpl<>(goats, PageRequest.of(0, 10), 1);
        when(goatUseCase.findAllGoatsByFarm(eq(1L), any(Pageable.class))).thenReturn(goatPage);

        // Act & Assert
        mockMvc.perform(get("/api/goatfarms/1/goats")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].registrationNumber").value("001"))
                .andExpect(jsonPath("$.content[0].name").value("Cabra Teste"))
                .andExpect(jsonPath("$.content[0].breed").value("SAANEN"))
                .andExpect(jsonPath("$.content[0].gender").value("FEMEA"))
                .andExpect(jsonPath("$.content[0].status").value("ATIVO"))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(goatUseCase).findAllGoatsByFarm(eq(1L), any(Pageable.class));
    }

    @Test
@WithMockUser(roles = "OPERATOR")
    void shouldGetGoatByIdSuccessfully() throws Exception {
        // Arrange
        when(goatUseCase.findGoatById(eq(1L), eq("001"))).thenReturn(goatResponseVO);

        // Act & Assert
        mockMvc.perform(get("/api/goatfarms/1/goats/001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.registrationNumber").value("001"))
                .andExpect(jsonPath("$.name").value("Cabra Teste"))
                .andExpect(jsonPath("$.breed").value("SAANEN"))
                .andExpect(jsonPath("$.gender").value("FEMEA"));

        verify(goatUseCase).findGoatById(eq(1L), eq("001"));
    }

    @Test
@WithMockUser(roles = "OPERATOR")
    void shouldReturnNotFoundWhenGoatDoesNotExist() throws Exception {
        // Arrange
        when(goatUseCase.findGoatById(eq(1L), eq("999"))).thenThrow(new ResourceNotFoundException("Goat not found with id: 999"));

        // Act & Assert
        mockMvc.perform(get("/api/goatfarms/1/goats/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Goat not found with id: 999"));

        verify(goatUseCase).findGoatById(eq(1L), eq("999"));
    }

    @Test
@WithMockUser(roles = "OPERATOR")
    void shouldCreateGoatSuccessfully() throws Exception {
        // Arrange
        GoatRequestDTO newGoatRequestDTO = new GoatRequestDTO();
        newGoatRequestDTO.setRegistrationNumber("002");
        newGoatRequestDTO.setName("Nova Cabra");
        newGoatRequestDTO.setBreed(com.devmaster.goatfarm.goat.enums.GoatBreed.ANGLO_NUBIANA);
        newGoatRequestDTO.setGender(com.devmaster.goatfarm.goat.enums.Gender.MACHO);
        newGoatRequestDTO.setBirthDate(LocalDate.of(2021, 3, 10));
        newGoatRequestDTO.setColor("Marrom");
        newGoatRequestDTO.setStatus(com.devmaster.goatfarm.goat.enums.GoatStatus.ATIVO);

        GoatResponseDTO createdGoatResponseDTO = new GoatResponseDTO();
        createdGoatResponseDTO.setRegistrationNumber("002");
        createdGoatResponseDTO.setName("Nova Cabra");
        createdGoatResponseDTO.setBreed(com.devmaster.goatfarm.goat.enums.GoatBreed.ANGLO_NUBIANA);
        createdGoatResponseDTO.setGender(com.devmaster.goatfarm.goat.enums.Gender.MACHO);
        createdGoatResponseDTO.setBirthDate(LocalDate.of(2021, 3, 10));
        createdGoatResponseDTO.setColor("Marrom");
        createdGoatResponseDTO.setStatus(com.devmaster.goatfarm.goat.enums.GoatStatus.ATIVO);

        when(goatUseCase.createGoat(eq(1L), any(GoatRequestVO.class))).thenReturn(goatResponseVO);

        // Act & Assert
        mockMvc.perform(post("/api/goatfarms/1/goats")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newGoatRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.registrationNumber").value("002"))
                .andExpect(jsonPath("$.name").value("Nova Cabra"))
                .andExpect(jsonPath("$.breed").value("ANGLO_NUBIANA"))
                .andExpect(jsonPath("$.gender").value("MACHO"))
                .andExpect(jsonPath("$.status").value("ATIVO"));

        verify(goatUseCase).createGoat(eq(1L), any(GoatRequestVO.class));
    }

    @Test
@WithMockUser(roles = "OPERATOR")
    void shouldUpdateGoatSuccessfully() throws Exception {
        // Arrange
        GoatRequestDTO updateGoatRequestDTO = new GoatRequestDTO();
        updateGoatRequestDTO.setRegistrationNumber("001");
        updateGoatRequestDTO.setName("Cabra Atualizada");
        updateGoatRequestDTO.setBreed(com.devmaster.goatfarm.goat.enums.GoatBreed.BOER);
        updateGoatRequestDTO.setGender(com.devmaster.goatfarm.goat.enums.Gender.FEMEA);
        updateGoatRequestDTO.setBirthDate(LocalDate.of(2020, 5, 15));
        updateGoatRequestDTO.setColor("Branca");
        updateGoatRequestDTO.setStatus(com.devmaster.goatfarm.goat.enums.GoatStatus.ATIVO);

        GoatResponseDTO updatedGoatResponseDTO = new GoatResponseDTO();
        updatedGoatResponseDTO.setRegistrationNumber("001");
        updatedGoatResponseDTO.setName("Cabra Atualizada");
        updatedGoatResponseDTO.setBreed(com.devmaster.goatfarm.goat.enums.GoatBreed.BOER);
        updatedGoatResponseDTO.setGender(com.devmaster.goatfarm.goat.enums.Gender.FEMEA);
        updatedGoatResponseDTO.setBirthDate(LocalDate.of(2020, 5, 15));
        updatedGoatResponseDTO.setColor("Branca");
        updatedGoatResponseDTO.setStatus(com.devmaster.goatfarm.goat.enums.GoatStatus.ATIVO);

        when(goatUseCase.updateGoat(eq(1L), eq("001"), any(GoatRequestVO.class))).thenReturn(goatResponseVO);

        // Act & Assert
        mockMvc.perform(put("/api/goatfarms/1/goats/001")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateGoatRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.registrationNumber").value("001"))
                .andExpect(jsonPath("$.name").value("Cabra Atualizada"))
                .andExpect(jsonPath("$.breed").value("BOER"))
                .andExpect(jsonPath("$.gender").value("FEMEA"))
                .andExpect(jsonPath("$.status").value("ATIVO"));

        verify(goatUseCase).updateGoat(eq(1L), eq("001"), any(GoatRequestVO.class));
    }

    @Test
@WithMockUser(roles = "OPERATOR")
    void shouldDeleteGoatSuccessfully() throws Exception {
        // Arrange
        doNothing().when(goatUseCase).deleteGoat(eq(1L), eq("001"));

        // Act & Assert
        mockMvc.perform(delete("/api/goatfarms/1/goats/001")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(goatUseCase).deleteGoat(eq(1L), eq("001"));
    }

    @Test
@WithMockUser(roles = "OPERATOR")
    void shouldReturnNotFoundWhenDeletingNonExistentGoat() throws Exception {
        // Arrange
        doThrow(new ResourceNotFoundException("Goat not found with id: 999"))
                .when(goatUseCase).deleteGoat(eq(1L), eq("999"));

        // Act & Assert
        mockMvc.perform(delete("/api/goatfarms/1/goats/999")
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Goat not found with id: 999"));

        verify(goatUseCase).deleteGoat(eq(1L), eq("999"));
    }

    @Test
    void shouldReturnUnauthorizedWhenNotAuthenticated() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/goatfarms/1/goats"))
                .andExpect(status().isUnauthorized());

        verify(goatUseCase, never()).findAllGoatsByFarm(eq(1L), any(Pageable.class));
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void shouldReturnForbiddenWhenInsufficientPermissions() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/goatfarms/1/goats"))
                .andExpect(status().isForbidden());

        verify(goatUseCase, never()).findAllGoatsByFarm(eq(1L), any(Pageable.class));
    }

    @org.springframework.context.annotation.Configuration
    @org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity(prePostEnabled = true)
    static class TestMethodSecurityConfig {}

    @Test
@WithMockUser(roles = "OPERATOR")
    void shouldReturnBadRequestForInvalidGoatData() throws Exception {
        // Arrange
        GoatRequestDTO invalidGoatRequestDTO = new GoatRequestDTO();
        // Deixar campos obrigatórios vazios

        // Act & Assert
        mockMvc.perform(post("/api/goatfarms/1/goats")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidGoatRequestDTO)))
                .andExpect(status().isBadRequest());

        verify(goatUseCase, never()).createGoat(eq(1L), any(GoatRequestVO.class));
    }

    @Test
@WithMockUser(roles = "OPERATOR")
    void shouldGetGoatsByFarmSuccessfully() throws Exception {
        // Arrange
        List<GoatResponseVO> goats2 = Arrays.asList(goatResponseVO);
        Page<GoatResponseVO> goatPage2 = new PageImpl<>(goats2, PageRequest.of(0, 10), 1);
        when(goatUseCase.findAllGoatsByFarm(eq(1L), any(Pageable.class))).thenReturn(goatPage2);

        // Act & Assert
        mockMvc.perform(get("/api/goatfarms/1/goats")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].registrationNumber").value("001"))
                .andExpect(jsonPath("$.content[0].name").value("Cabra Teste"))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(goatUseCase).findAllGoatsByFarm(eq(1L), any(Pageable.class));
}
}
