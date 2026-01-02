package com.devmaster.goatfarm.farm.api;

import com.devmaster.goatfarm.farm.api.controller.GoatFarmController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import com.devmaster.goatfarm.farm.facade.GoatFarmFacade;

@WebMvcTest(GoatFarmController.class)
public class GoatFarmControllerWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GoatFarmFacade goatFarmFacade;

    @Test
    void contextLoads() {
    }
}