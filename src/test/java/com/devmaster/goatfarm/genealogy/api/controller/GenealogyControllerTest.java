package com.devmaster.goatfarm.genealogy.api.controller;

import com.devmaster.goatfarm.genealogy.application.ports.in.GenealogyQueryUseCase;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.genealogy.business.bo.GenealogyResponseVO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GenealogyController.class)
@AutoConfigureMockMvc(addFilters = false)
public class GenealogyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GenealogyQueryUseCase genealogyQueryUseCase;

    @Test
    void shouldReturnGenealogy_WhenGoatExists() throws Exception {
        GenealogyResponseVO responseVO = GenealogyResponseVO.builder()
                .goatRegistration("123")
                .goatName("Test Goat")
                .build();

        when(genealogyQueryUseCase.findGenealogy(anyLong(), anyString())).thenReturn(responseVO);

        mockMvc.perform(get("/api/goatfarms/1/goats/123/genealogies")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.goatRegistration").value("123"))
                .andExpect(jsonPath("$.goatName").value("Test Goat"));
    }

    @Test
    void shouldReturn404_WhenGoatNotFound() throws Exception {
        when(genealogyQueryUseCase.findGenealogy(anyLong(), anyString()))
                .thenThrow(new ResourceNotFoundException("Not Found"));

        mockMvc.perform(get("/api/goatfarms/1/goats/999/genealogies")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}

