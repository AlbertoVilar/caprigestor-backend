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

        Role operatorRole = new Role();
        operatorRole.setAuthority("ROLE_OPERATOR");
        roleRepository.save(operatorRole);

        Role adminRole = new Role();
        adminRole.setAuthority("ROLE_ADMIN");
        roleRepository.save(adminRole);

        User user = new User();
        user.setEmail("user@example.com");
        user.setPassword(passwordEncoder.encode("password"));
        user.setName("Regular User");
        user.setCpf("12345678901");
        user.addRole(operatorRole);
        userRepository.save(user);

        User admin = new User();
        admin.setEmail("admin@example.com");
        admin.setPassword(passwordEncoder.encode("password"));
        admin.setName("Admin User");
        admin.setCpf("00000000000");
        admin.addRole(adminRole);
        userRepository.save(admin);

        userToken = obtainToken("user@example.com", "password");
        adminToken = obtainToken("admin@example.com", "password");
    }

    private String obtainToken(String email, String password) throws Exception {
        String loginPayload = "{\"email\":\"" + email + "\", \"password\":\"" + password + "\"}";
        String response = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload))
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(response).get("accessToken").asText();
    }

    @Test
    @DisplayName("1. POST sem body -> 400 Bad Request")
    void shouldReturn400WhenBodyIsMissing() throws Exception {
        mockMvc.perform(post("/api/v1/goatfarms/1/goats/99999/health-events")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Erro de leitura do corpo da requisição"));
    }

    @Test
    @DisplayName("2. POST com JSON inválido -> 400 Bad Request")
    void shouldReturn400WhenJsonIsInvalid() throws Exception {
        String invalidJson = "{ \"type\": \"INVALID_TYPE\", \"date\": \"2025-01-01\" }";

        mockMvc.perform(post("/api/v1/goatfarms/1/goats/99999/health-events")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("2b. POST com JSON malformado -> 400 Bad Request")
    void shouldReturn400WhenJsonIsMalformed() throws Exception {
        String malformedJson = "{ \"type\": \"VACINA\", \"date\": ";

        mockMvc.perform(post("/api/v1/goatfarms/1/goats/99999/health-events")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("3. POST com goatId inexistente -> 404 Not Found")
    void shouldReturn404WhenGoatNotFound() throws Exception {
        HealthEventCreateRequestDTO request = HealthEventCreateRequestDTO.builder()
                .type(HealthEventType.VACINA)
                .title("Vacina Teste")
                .scheduledDate(LocalDate.now())
                .build();

        mockMvc.perform(post("/api/v1/goatfarms/1/goats/99999/health-events")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Recurso não encontrado"));
    }

    @Test
    @DisplayName("4. POST sem permissão -> 403 Forbidden")
    void shouldReturn403WhenUserIsNotOwner() throws Exception {
        HealthEventCreateRequestDTO request = HealthEventCreateRequestDTO.builder()
                .type(HealthEventType.VACINA)
                .title("Vacina Teste")
                .scheduledDate(LocalDate.now())
                .build();

        mockMvc.perform(post("/api/v1/goatfarms/1/goats/99999/health-events")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Acesso negado"));
    }

    @Test
    @DisplayName("4b. POST sem autenticação -> 401 Unauthorized")
    void shouldReturn401WhenNotAuthenticated() throws Exception {
        HealthEventCreateRequestDTO request = HealthEventCreateRequestDTO.builder()
                .type(HealthEventType.VACINA)
                .title("Vacina Teste")
                .scheduledDate(LocalDate.now())
                .build();

        mockMvc.perform(post("/api/v1/goatfarms/1/goats/99999/health-events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Não autorizado"));
    }

    @Test
    @DisplayName("5. Endpoint restrito sem role -> 403 Forbidden (nível de filtro)")
    void shouldReturn403WhenUserHasNoRoleForEndpoint() throws Exception {
        mockMvc.perform(post("/api/v1/articles")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Acesso negado"));
    }
}