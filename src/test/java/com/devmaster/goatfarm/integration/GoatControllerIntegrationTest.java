package com.devmaster.goatfarm.integration;

import com.devmaster.goatfarm.goat.api.controller.GoatController;
import com.devmaster.goatfarm.goat.api.dto.GoatRequestDTO;
import com.devmaster.goatfarm.goat.api.dto.GoatResponseDTO;
import com.devmaster.goatfarm.goat.facade.GoatFacade;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@org.springframework.boot.test.context.SpringBootTest
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = true)
@Import({com.devmaster.goatfarm.config.exceptions.GlobalExceptionHandler.class})
class GoatControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Autowired
    private GoatController goatController;

    @Autowired
    private org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping handlerMapping;

    @MockBean
    private GoatFacade goatFacade;

    @MockBean
    private com.devmaster.goatfarm.authority.business.AdminMaintenanceBusiness adminMaintenanceBusiness;

    private GoatResponseDTO goatResponseDTO;
    private GoatRequestDTO goatRequestDTO;

    @BeforeEach
    void setUp() {
        if (objectMapper == null) {
            objectMapper = new ObjectMapper();
            objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
            objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        }
        goatResponseDTO = new GoatResponseDTO();
        goatResponseDTO.setRegistrationNumber("001");
        goatResponseDTO.setName("Cabra Teste");
        goatResponseDTO.setBreed(com.devmaster.goatfarm.goat.enums.GoatBreed.SAANEN);
        goatResponseDTO.setGender(com.devmaster.goatfarm.goat.enums.Gender.FEMEA);
        goatResponseDTO.setBirthDate(LocalDate.of(2020, 1, 15));
        goatResponseDTO.setColor("Branca");
        goatResponseDTO.setStatus(com.devmaster.goatfarm.goat.enums.GoatStatus.ATIVO);
        
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
    void contextLoads() {
        Assertions.assertNotNull(goatController, "GoatController não foi carregado no contexto de teste");
    }

    @Test
    @WithMockUser(roles = "OPERATOR")
    void shouldGetAllGoatsSuccessfully() throws Exception {
        // Arrange
        List<GoatResponseDTO> goats = Arrays.asList(goatResponseDTO);
        Page<GoatResponseDTO> goatPage = new PageImpl<>(goats, PageRequest.of(0, 10), 1);
        when(goatFacade.findAllGoatsByFarm(eq(1L), any(Pageable.class))).thenReturn(goatPage);

        // Act & Assert
        mockMvc.perform(get("/api/goatfarms/1/goats")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].registrationNumber").value("001"))
                .andExpect(jsonPath("$.content[0].name").value("Cabra Teste"))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(goatFacade).findAllGoatsByFarm(eq(1L), any(Pageable.class));
    }

    @Test
    @WithMockUser(roles = "OPERATOR")
    void shouldGetGoatByIdSuccessfully() throws Exception {
        // Arrange
        when(goatFacade.findGoatById(eq(1L), eq("001"))).thenReturn(goatResponseDTO);

        // Act & Assert
        mockMvc.perform(get("/api/goatfarms/1/goats/001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.registrationNumber").value("001"))
                .andExpect(jsonPath("$.name").value("Cabra Teste"));

        verify(goatFacade).findGoatById(eq(1L), eq("001"));
    }

    @Test
    @WithMockUser(roles = "OPERATOR")
    void shouldReturnNotFoundWhenGoatDoesNotExist() throws Exception {
        // Arrange
        when(goatFacade.findGoatById(eq(1L), eq("999"))).thenThrow(new ResourceNotFoundException("Goat not found with id: 999"));

        // Act & Assert
        mockMvc.perform(get("/api/goatfarms/1/goats/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Recurso não encontrado"));

        verify(goatFacade).findGoatById(eq(1L), eq("999"));
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

        when(goatFacade.createGoat(eq(1L), any(GoatRequestDTO.class))).thenReturn(createdGoatResponseDTO);

        // Act & Assert
        mockMvc.perform(post("/api/goatfarms/1/goats")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newGoatRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.registrationNumber").value("002"))
                .andExpect(jsonPath("$.name").value("Nova Cabra"));

        verify(goatFacade).createGoat(eq(1L), any(GoatRequestDTO.class));
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

        when(goatFacade.updateGoat(eq(1L), eq("001"), any(GoatRequestDTO.class))).thenReturn(updatedGoatResponseDTO);

        // Act & Assert
        mockMvc.perform(put("/api/goatfarms/1/goats/001")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateGoatRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Cabra Atualizada"));

        verify(goatFacade).updateGoat(eq(1L), eq("001"), any(GoatRequestDTO.class));
    }

    @Test
    @WithMockUser(roles = "OPERATOR")
    void shouldDeleteGoatSuccessfully() throws Exception {
        // Arrange
        doNothing().when(goatFacade).deleteGoat(eq(1L), eq("001"));

        // Act & Assert
        mockMvc.perform(delete("/api/goatfarms/1/goats/001")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(goatFacade).deleteGoat(eq(1L), eq("001"));
    }

    @Test
    @WithMockUser(roles = "OPERATOR")
    void shouldReturnNotFoundWhenDeletingNonExistentGoat() throws Exception {
        // Arrange
        doThrow(new ResourceNotFoundException("Goat not found with id: 999"))
                .when(goatFacade).deleteGoat(eq(1L), eq("999"));

        // Act & Assert
        mockMvc.perform(delete("/api/goatfarms/1/goats/999")
                        .with(csrf()))
                .andExpect(status().isNotFound());

        verify(goatFacade).deleteGoat(eq(1L), eq("999"));
    }

    @Test
    void shouldAllowPublicAccessToGoatListWithoutAuth() throws Exception {
        // Arrange
        List<GoatResponseDTO> goats = Arrays.asList(goatResponseDTO);
        Page<GoatResponseDTO> goatPage = new PageImpl<>(goats, PageRequest.of(0, 10), 1);
        when(goatFacade.findAllGoatsByFarm(eq(1L), any(Pageable.class))).thenReturn(goatPage);

        // Act & Assert
        mockMvc.perform(get("/api/goatfarms/1/goats")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());

        verify(goatFacade).findAllGoatsByFarm(eq(1L), any(Pageable.class));
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void shouldReturnForbiddenWhenInsufficientPermissions() throws Exception {
        // Arrange
        GoatRequestDTO newGoatRequestDTO = new GoatRequestDTO();
        newGoatRequestDTO.setRegistrationNumber("003");
        newGoatRequestDTO.setName("Cabra Viewer");
        newGoatRequestDTO.setBreed(com.devmaster.goatfarm.goat.enums.GoatBreed.SAANEN);
        newGoatRequestDTO.setGender(com.devmaster.goatfarm.goat.enums.Gender.FEMEA);
        newGoatRequestDTO.setBirthDate(LocalDate.of(2020, 1, 15));
        newGoatRequestDTO.setColor("Branca");
        newGoatRequestDTO.setStatus(com.devmaster.goatfarm.goat.enums.GoatStatus.ATIVO);

        // Act & Assert
        mockMvc.perform(post("/api/goatfarms/1/goats")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newGoatRequestDTO)))
                .andExpect(status().isForbidden());

        verify(goatFacade, never()).createGoat(eq(1L), any(GoatRequestDTO.class));
    }

    @Test
    @WithMockUser(roles = "OPERATOR")
    void shouldReturnUnprocessableEntityForInvalidGoatData() throws Exception {
        // Arrange
        GoatRequestDTO invalidGoatRequestDTO = new GoatRequestDTO();
        // Deixar campos obrigatórios vazios

        // Act & Assert
        mockMvc.perform(post("/api/goatfarms/1/goats")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidGoatRequestDTO)))
                .andExpect(status().isUnprocessableEntity());

        verify(goatFacade, never()).createGoat(eq(1L), any(GoatRequestDTO.class));
    }
}
