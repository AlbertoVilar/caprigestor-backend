package com.devmaster.goatfarm.authority.api.controller;

import com.devmaster.goatfarm.application.ports.in.UserManagementUseCase;
import com.devmaster.goatfarm.authority.mapper.UserMapper;
import com.devmaster.goatfarm.authority.model.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserManagementUseCase userUseCase;

    @MockBean
    private UserMapper userMapper;

    @Test
    public void whenGetUserById_thenReturns200() throws Exception {
        // Supondo que o use case retorne um VO e o mapper o converta
        // A lógica exata do teste dependerá da implementação
        
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk());
    }
}
