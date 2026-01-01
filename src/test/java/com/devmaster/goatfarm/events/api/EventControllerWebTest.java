package com.devmaster.goatfarm.events.api;

import com.devmaster.goatfarm.events.api.controller.EventController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import com.devmaster.goatfarm.application.ports.in.EventManagementUseCase;

@WebMvcTest(EventController.class)
public class EventControllerWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EventManagementUseCase eventUseCase;

    @Test
    void contextLoads() {
    }
}