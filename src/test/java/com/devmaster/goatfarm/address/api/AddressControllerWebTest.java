package com.devmaster.goatfarm.address.api;

import com.devmaster.goatfarm.address.api.controller.AddressController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import com.devmaster.goatfarm.application.ports.in.AddressManagementUseCase;

@WebMvcTest(AddressController.class)
public class AddressControllerWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AddressManagementUseCase addressUseCase;

    @Test
    void contextLoads() {
    }
}