package com.devmaster.goatfarm.health.api.controller;

import com.devmaster.goatfarm.health.api.dto.HealthEventCreateRequestDTO;
import com.devmaster.goatfarm.health.api.dto.HealthEventDoneRequestDTO;
import com.devmaster.goatfarm.health.api.dto.HealthEventCancelRequestDTO;
import com.devmaster.goatfarm.health.api.mapper.HealthEventApiMapper;
import com.devmaster.goatfarm.health.application.ports.in.HealthEventCommandUseCase;
import com.devmaster.goatfarm.health.application.ports.in.HealthEventQueryUseCase;
import com.devmaster.goatfarm.health.domain.enums.HealthEventType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HealthEventController.class)
@AutoConfigureMockMvc(addFilters = false)
class HealthEventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private HealthEventCommandUseCase commandUseCase;

    @MockBean
    private HealthEventQueryUseCase queryUseCase;

    @MockBean
    private HealthEventApiMapper apiMapper;

    @MockBean(name = "ownershipService")
    private Object ownershipService; // Mocking to avoid ApplicationContext failure if checked

    @Test
    void create_shouldReturn422_whenMandatoryFieldsAreMissing() throws Exception {
        HealthEventCreateRequestDTO request = HealthEventCreateRequestDTO.builder()
                .type(null) // Mandatory
                .title("") // Mandatory
                .scheduledDate(null) // Mandatory
                .build();

        mockMvc.perform(post("/api/goatfarms/{farmId}/goats/{goatId}/health-events", 1L, "GOAT-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity()) // 422 for validation error
                .andExpect(jsonPath("$.errors[?(@.fieldName=='type')].message").value("O tipo do evento é obrigatório"))
                .andExpect(jsonPath("$.errors[?(@.fieldName=='title')].message").value("O título é obrigatório"))
                .andExpect(jsonPath("$.errors[?(@.fieldName=='scheduledDate')].message").value("A data agendada é obrigatória"));
    }

    @Test
    void create_shouldReturn422_whenDoseHasInvalidDigits() throws Exception {
        HealthEventCreateRequestDTO request = HealthEventCreateRequestDTO.builder()
                .type(HealthEventType.VACINA)
                .title("Vacinação")
                .scheduledDate(java.time.LocalDate.now())
                .dose(new java.math.BigDecimal("123.4567")) // 4 decimals, max is 3
                .build();

        mockMvc.perform(post("/api/goatfarms/{farmId}/goats/{goatId}/health-events", 1L, "GOAT-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errors[?(@.fieldName=='dose')].message").value("A dose deve ter no máximo 6 dígitos inteiros e 3 decimais"));
    }

    @Test
    void create_shouldReturn422_whenFieldsExceedSize() throws Exception {
        String longTitle = "a".repeat(101);
        HealthEventCreateRequestDTO request = HealthEventCreateRequestDTO.builder()
                .type(HealthEventType.VACINA)
                .title(longTitle)
                .scheduledDate(java.time.LocalDate.now())
                .build();

        mockMvc.perform(post("/api/goatfarms/{farmId}/goats/{goatId}/health-events", 1L, "GOAT-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errors[?(@.fieldName=='title')].message").value("O título deve ter no máximo 100 caracteres"));
    }

    @Test
    void markAsDone_shouldReturn422_whenPerformedAtIsFuture() throws Exception {
        HealthEventDoneRequestDTO request = HealthEventDoneRequestDTO.builder()
                .performedAt(LocalDateTime.now().plusDays(1))
                .responsible("John Doe")
                .build();

        mockMvc.perform(patch("/api/goatfarms/{farmId}/goats/{goatId}/health-events/{eventId}/done", 1L, "GOAT-001", 100L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errors[?(@.fieldName=='performedAt')].message").value("A data de realização não pode ser futura"));
    }

    @Test
    void markAsDone_shouldReturn200_whenPerformedAtIsPast() throws Exception {
        HealthEventDoneRequestDTO request = HealthEventDoneRequestDTO.builder()
                .performedAt(LocalDateTime.now().minusMinutes(1)) // Avoid flake
                .responsible("John Doe")
                .build();

        mockMvc.perform(patch("/api/goatfarms/{farmId}/goats/{goatId}/health-events/{eventId}/done", 1L, "GOAT-001", 100L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void cancel_shouldReturn422_whenNotesAreBlank() throws Exception {
        HealthEventCancelRequestDTO request = HealthEventCancelRequestDTO.builder()
                .notes("") // Mandatory
                .build();

        mockMvc.perform(patch("/api/goatfarms/{farmId}/goats/{goatId}/health-events/{eventId}/cancel", 1L, "GOAT-001", 100L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errors[?(@.fieldName=='notes')].message").value("O motivo do cancelamento é obrigatório"));
    }
}
