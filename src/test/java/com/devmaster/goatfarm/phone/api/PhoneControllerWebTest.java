package com.devmaster.goatfarm.phone.api;

import com.devmaster.goatfarm.phone.api.controller.PhoneController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import com.devmaster.goatfarm.application.ports.in.PhoneManagementUseCase;

@WebMvcTest(PhoneController.class)
public class PhoneControllerWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PhoneManagementUseCase phoneUseCase;

    @Test
    void contextLoads() {
    }
}