package com.devmaster.goatfarm.health.api.controller;

import com.devmaster.goatfarm.authority.persistence.entity.Role;
import com.devmaster.goatfarm.authority.persistence.entity.User;
import com.devmaster.goatfarm.authority.persistence.repository.RoleRepository;
import com.devmaster.goatfarm.authority.persistence.repository.UserRepository;
import com.devmaster.goatfarm.health.api.dto.HealthEventCreateRequestDTO;
import com.devmaster.goatfarm.health.domain.enums.HealthEventType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class HealthExceptionHandlingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private String userToken;
    private String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        userRepository.deleteAll();
        roleRepository.deleteAll();

        // Create Roles
        Role operatorRole = new Role();
        operatorRole.setAuthority("ROLE_OPERATOR");
        roleRepository.save(operatorRole);

        Role adminRole = new Role();
        adminRole.setAuthority("ROLE_ADMIN");
        roleRepository.save(adminRole);

        // Create User
        User user = new User();
        user.setEmail("user@example.com");
        user.setPassword(passwordEncoder.encode("password"));
        user.setName("Regular User");
        user.setCpf("12345678901");
        user.addRole(operatorRole);
        userRepository.save(user);

        // Create Admin
        User admin = new User();
        admin.setEmail("admin@example.com");
        admin.setPassword(passwordEncoder.encode("password"));
        admin.setName("Admin User");
        admin.setCpf("00000000000");
        admin.addRole(adminRole);
        userRepository.save(admin);

        // Get Tokens
        userToken = obtainToken("user@example.com", "password");
        adminToken = obtainToken("admin@example.com", "password");
    }

    private String obtainToken(String email, String password) throws Exception {
        String loginPayload = "{\"email\":\"" + email + "\", \"password\":\"" + password + "\"}";
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload))
                .andReturn().getResponse().getContentAsString();
        
        return objectMapper.readTree(response).get("accessToken").asText();
    }

    @Test
    @DisplayName("1. POST without body -> 400 Bad Request")
    void shouldReturn400WhenBodyIsMissing() throws Exception {
        mockMvc.perform(post("/api/goatfarms/1/goats/99999/health-events")
                        .header("Authorization", "Bearer " + adminToken) // Use Admin to bypass ownership check for now
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Erro de leitura do corpo da requisição"));
    }

    @Test
    @DisplayName("2. POST with invalid JSON -> 400 Bad Request")
    void shouldReturn400WhenJsonIsInvalid() throws Exception {
        String invalidJson = "{ \"type\": \"INVALID_TYPE\", \"date\": \"2025-01-01\" }"; // Enum invalid

        mockMvc.perform(post("/api/goatfarms/1/goats/99999/health-events")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest()) // HttpMessageNotReadableException
                .andExpect(jsonPath("$.status").value(400));
    }
    
    @Test
    @DisplayName("2b. POST with malformed JSON -> 400 Bad Request")
    void shouldReturn400WhenJsonIsMalformed() throws Exception {
        String malformedJson = "{ \"type\": \"VACINA\", \"date\": "; // Broken JSON

        mockMvc.perform(post("/api/goatfarms/1/goats/99999/health-events")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("3. POST with non-existent goatId -> 404 Not Found")
    void shouldReturn404WhenGoatNotFound() throws Exception {
        HealthEventCreateRequestDTO request = HealthEventCreateRequestDTO.builder()
                .type(HealthEventType.VACINA)
                .title("Vacina Teste")
                .scheduledDate(LocalDate.now())
                .build();

        mockMvc.perform(post("/api/goatfarms/1/goats/99999/health-events")
                        .header("Authorization", "Bearer " + adminToken) // Admin bypasses farm ownership, hits business logic
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Recurso não encontrado"));
    }

    @Test
    @DisplayName("4. POST without permission -> 403 Forbidden")
    void shouldReturn403WhenUserIsNotOwner() throws Exception {
        HealthEventCreateRequestDTO request = HealthEventCreateRequestDTO.builder()
                .type(HealthEventType.VACINA)
                .title("Vacina Teste")
                .scheduledDate(LocalDate.now())
                .build();

        // User is OPERATOR but not linked to Farm 1 (since we just cleared DB and didn't create farm link)
        mockMvc.perform(post("/api/goatfarms/1/goats/99999/health-events")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Acesso negado"));
    }
    
    @Test
    @DisplayName("4b. POST without authentication -> 401 Unauthorized")
    void shouldReturn401WhenNotAuthenticated() throws Exception {
         HealthEventCreateRequestDTO request = HealthEventCreateRequestDTO.builder()
                .type(HealthEventType.VACINA)
                .title("Vacina Teste")
                .scheduledDate(LocalDate.now())
                .build();

        mockMvc.perform(post("/api/goatfarms/1/goats/99999/health-events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Não autorizado"));
    }

    @Test
    @DisplayName("5. GET restricted endpoint without role -> 403 Forbidden (Filter Level)")
    void shouldReturn403WhenUserHasNoRoleForEndpoint() throws Exception {
        // /api/articles requires ROLE_ADMIN. User has ROLE_OPERATOR.
        mockMvc.perform(post("/api/articles") 
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Acesso negado"));
    }
}
