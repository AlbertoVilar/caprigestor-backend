package com.devmaster.goatfarm.genealogy.api;

import com.devmaster.goatfarm.genealogy.api.controller.GenealogyController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import com.devmaster.goatfarm.application.ports.in.GenealogyManagementUseCase;

@WebMvcTest(GenealogyController.class)
public class GenealogyControllerWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GenealogyManagementUseCase genealogyUseCase;

    @Test
    void contextLoads() {
    }
}