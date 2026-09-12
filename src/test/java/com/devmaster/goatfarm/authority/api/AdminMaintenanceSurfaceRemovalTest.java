package com.devmaster.goatfarm.authority.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The global database reset operations are deliberately not part of the HTTP API.
 * This test prevents the destructive routes from being reintroduced accidentally.
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = true)
@ActiveProfiles("test")
class AdminMaintenanceSurfaceRemovalTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void cleanAdminRouteIsNotMapped() throws Exception {
        mockMvc.perform(post("/api/v1/admin/maintenance/clean-admin")
                        .param("adminId", "1"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void automaticCleanAdminRouteIsNotMapped() throws Exception {
        mockMvc.perform(post("/api/v1/admin/maintenance/clean-admin-auto"))
                .andExpect(status().isNotFound());
    }
}
