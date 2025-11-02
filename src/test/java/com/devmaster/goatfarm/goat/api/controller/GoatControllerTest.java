package com.devmaster.goatfarm.goat.api.controller;

import com.devmaster.goatfarm.goat.api.dto.GoatRequestDTO;
import com.devmaster.goatfarm.goat.api.dto.GoatResponseDTO;
import com.devmaster.goatfarm.goat.business.bo.GoatRequestVO;
import com.devmaster.goatfarm.goat.business.bo.GoatResponseVO;
import com.devmaster.goatfarm.goat.facade.GoatFacade;
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
@Import(GoatMapperImpl.class)
class GoatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GoatFacade goatFacade;

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
        goatResponseDTO.setGender(com.devmaster.goatfarm.goat.enums.Gender.FEMALE);
        goatResponseDTO.setBirthDate(LocalDate.of(2020, 1, 15));
        goatResponseDTO.setColor("Branca");
        goatResponseDTO.setStatus(com.devmaster.goatfarm.goat.enums.GoatStatus.ATIVO);

        goatResponseVO = new GoatResponseVO();
        goatResponseVO.setRegistrationNumber("001");
        goatResponseVO.setName("Cabra Teste");
        goatResponseVO.setBreed(com.devmaster.goatfarm.goat.enums.GoatBreed.SAANEN);
        goatResponseVO.setGender(com.devmaster.goatfarm.goat.enums.Gender.FEMALE);
        goatResponseVO.setBirthDate(LocalDate.of(2020, 1, 15));
        goatResponseVO.setColor("Branca");
        goatResponseVO.setStatus(com.devmaster.goatfarm.goat.enums.GoatStatus.ATIVO);
        
        goatRequestDTO = new GoatRequestDTO();
        goatRequestDTO.setRegistrationNumber("001");
        goatRequestDTO.setName("Cabra Teste");
        goatRequestDTO.setBreed(com.devmaster.goatfarm.goat.enums.GoatBreed.SAANEN);
        goatRequestDTO.setGender(com.devmaster.goatfarm.goat.enums.Gender.FEMALE);
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
        when(goatFacade.findAllGoats(any(Pageable.class))).thenReturn(goatPage);

        // Act & Assert
        mockMvc.perform(get("/api/goats")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].registrationNumber").value("001"))
                .andExpect(jsonPath("$.content[0].name").value("Cabra Teste"))
                .andExpect(jsonPath("$.content[0].breed").value("SAANEN"))
                .andExpect(jsonPath("$.content[0].gender").value("FEMALE"))
                .andExpect(jsonPath("$.content[0].status").value("ATIVO"))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(goatFacade).findAllGoats(any(Pageable.class));
    }

    @Test
@WithMockUser(roles = "OPERATOR")
    void shouldGetGoatByIdSuccessfully() throws Exception {
        // Arrange
        when(goatFacade.findGoatByRegistrationNumber("001")).thenReturn(goatResponseVO);

        // Act & Assert
        mockMvc.perform(get("/api/goats/001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.registrationNumber").value("001"))
                .andExpect(jsonPath("$.name").value("Cabra Teste"))
                .andExpect(jsonPath("$.breed").value("SAANEN"))
                .andExpect(jsonPath("$.gender").value("FEMALE"));

        verify(goatFacade).findGoatByRegistrationNumber("001");
    }

    @Test
@WithMockUser(roles = "OPERATOR")
    void shouldReturnNotFoundWhenGoatDoesNotExist() throws Exception {
        // Arrange
        when(goatFacade.findGoatByRegistrationNumber("999")).thenThrow(new ResourceNotFoundException("Goat not found with id: 999"));

        // Act & Assert
        mockMvc.perform(get("/api/goats/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Goat not found with id: 999"));

        verify(goatFacade).findGoatByRegistrationNumber("999");
    }

    @Test
@WithMockUser(roles = "OPERATOR")
    void shouldCreateGoatSuccessfully() throws Exception {
        // Arrange
        GoatRequestDTO newGoatRequestDTO = new GoatRequestDTO();
        newGoatRequestDTO.setRegistrationNumber("002");
        newGoatRequestDTO.setName("Nova Cabra");
        newGoatRequestDTO.setBreed(com.devmaster.goatfarm.goat.enums.GoatBreed.ANGLO_NUBIANA);
        newGoatRequestDTO.setGender(com.devmaster.goatfarm.goat.enums.Gender.MALE);
        newGoatRequestDTO.setBirthDate(LocalDate.of(2021, 3, 10));
        newGoatRequestDTO.setColor("Marrom");
        newGoatRequestDTO.setStatus(com.devmaster.goatfarm.goat.enums.GoatStatus.ATIVO);

        GoatResponseVO createdGoatResponseVO = new GoatResponseVO();
        createdGoatResponseVO.setRegistrationNumber("002");
        createdGoatResponseVO.setName("Nova Cabra");
        createdGoatResponseVO.setBreed(com.devmaster.goatfarm.goat.enums.GoatBreed.ANGLO_NUBIANA);
        createdGoatResponseVO.setGender(com.devmaster.goatfarm.goat.enums.Gender.MALE);
        createdGoatResponseVO.setBirthDate(LocalDate.of(2021, 3, 10));
        createdGoatResponseVO.setColor("Marrom");
        createdGoatResponseVO.setStatus(com.devmaster.goatfarm.goat.enums.GoatStatus.ATIVO);

        when(goatFacade.createGoat(any(GoatRequestVO.class), anyLong(), anyLong())).thenReturn(createdGoatResponseVO);

        // Act & Assert
        mockMvc.perform(post("/api/goats")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newGoatRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.registrationNumber").value("002"))
                .andExpect(jsonPath("$.name").value("Nova Cabra"))
                .andExpect(jsonPath("$.breed").value("ANGLO_NUBIANA"))
                .andExpect(jsonPath("$.gender").value("MALE"))
                .andExpect(jsonPath("$.status").value("ATIVO"));

        verify(goatFacade).createGoat(any(GoatRequestVO.class), anyLong(), anyLong());
    }

    @Test
@WithMockUser(roles = "OPERATOR")
    void shouldUpdateGoatSuccessfully() throws Exception {
        // Arrange
        GoatRequestDTO updateGoatRequestDTO = new GoatRequestDTO();
        updateGoatRequestDTO.setRegistrationNumber("001");
        updateGoatRequestDTO.setName("Cabra Atualizada");
        updateGoatRequestDTO.setBreed(com.devmaster.goatfarm.goat.enums.GoatBreed.BOER);
        updateGoatRequestDTO.setGender(com.devmaster.goatfarm.goat.enums.Gender.FEMALE);
        updateGoatRequestDTO.setBirthDate(LocalDate.of(2020, 5, 15));
        updateGoatRequestDTO.setColor("Branca");
        updateGoatRequestDTO.setStatus(com.devmaster.goatfarm.goat.enums.GoatStatus.ATIVO);

        GoatResponseVO updatedGoatResponseVO = new GoatResponseVO();
        updatedGoatResponseVO.setRegistrationNumber("001");
        updatedGoatResponseVO.setName("Cabra Atualizada");
        updatedGoatResponseVO.setBreed(com.devmaster.goatfarm.goat.enums.GoatBreed.BOER);
        updatedGoatResponseVO.setGender(com.devmaster.goatfarm.goat.enums.Gender.FEMALE);
        updatedGoatResponseVO.setBirthDate(LocalDate.of(2020, 5, 15));
        updatedGoatResponseVO.setColor("Branca");
        updatedGoatResponseVO.setStatus(com.devmaster.goatfarm.goat.enums.GoatStatus.ATIVO);

        when(goatFacade.updateGoat(eq("001"), any(GoatRequestVO.class))).thenReturn(updatedGoatResponseVO);

        // Act & Assert
        mockMvc.perform(put("/api/goats/001")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateGoatRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.registrationNumber").value("001"))
                .andExpect(jsonPath("$.name").value("Cabra Atualizada"))
                .andExpect(jsonPath("$.breed").value("BOER"))
                .andExpect(jsonPath("$.gender").value("FEMALE"))
                .andExpect(jsonPath("$.status").value("ATIVO"));

        verify(goatFacade).updateGoat(eq("001"), any(GoatRequestVO.class));
    }

    @Test
@WithMockUser(roles = "OPERATOR")
    void shouldDeleteGoatSuccessfully() throws Exception {
        // Arrange
        doNothing().when(goatFacade).deleteGoat("001");

        // Act & Assert
        mockMvc.perform(delete("/api/goats/001")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(goatFacade).deleteGoat("001");
    }

    @Test
@WithMockUser(roles = "OPERATOR")
    void shouldReturnNotFoundWhenDeletingNonExistentGoat() throws Exception {
        // Arrange
        doThrow(new ResourceNotFoundException("Goat not found with id: 999"))
                .when(goatFacade).deleteGoat("999");

        // Act & Assert
        mockMvc.perform(delete("/api/goats/999")
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Goat not found with id: 999"));

        verify(goatFacade).deleteGoat("999");
    }

    @Test
    void shouldReturnUnauthorizedWhenNotAuthenticated() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/goats"))
                .andExpect(status().isUnauthorized());

        verify(goatFacade, never()).findAllGoats(any(Pageable.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturnForbiddenWhenInsufficientPermissions() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/goats"))
                .andExpect(status().isForbidden());

        verify(goatFacade, never()).findAllGoats(any(Pageable.class));
    }

    @Test
@WithMockUser(roles = "OPERATOR")
    void shouldReturnBadRequestForInvalidGoatData() throws Exception {
        // Arrange
        GoatRequestDTO invalidGoatRequestDTO = new GoatRequestDTO();
        // Deixar campos obrigatórios vazios

        // Act & Assert
        mockMvc.perform(post("/api/goats")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidGoatRequestDTO)))
                .andExpect(status().isBadRequest());

        verify(goatFacade, never()).createGoat(any(GoatRequestVO.class), anyLong(), anyLong());
    }

    @Test
@WithMockUser(roles = "OPERATOR")
    void shouldGetGoatsByFarmSuccessfully() throws Exception {
        // Arrange
        List<GoatResponseVO> goats = Arrays.asList(goatResponseVO);
        Page<GoatResponseVO> goatPage = new PageImpl<>(goats, PageRequest.of(0, 10), 1);
        when(goatFacade.findGoatsByFarmIdAndRegistrationNumber(eq(1L), isNull(), any(Pageable.class))).thenReturn(goatPage);

        // Act & Assert
        mockMvc.perform(get("/api/goats/farm/1")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].registrationNumber").value("001"))
                .andExpect(jsonPath("$.content[0].name").value("Cabra Teste"))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(goatFacade).findGoatsByFarmIdAndRegistrationNumber(eq(1L), isNull(), any(Pageable.class));
    }
}