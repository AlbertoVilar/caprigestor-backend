package com.devmaster.goatfarm.authority.api.controller;

import com.devmaster.goatfarm.authority.api.dto.LoginRequestDTO;
import com.devmaster.goatfarm.authority.api.dto.LoginResponseDTO;
import com.devmaster.goatfarm.authority.model.entity.Role;
import com.devmaster.goatfarm.authority.model.entity.User;
import com.devmaster.goatfarm.authority.model.repository.RoleRepository;
import com.devmaster.goatfarm.authority.model.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private Role farmOwnerRole;

    @BeforeEach
    void setUp() {
        // Criar role se não existir
        farmOwnerRole = roleRepository.findByAuthority("ROLE_FARM_OWNER")
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setAuthority("ROLE_FARM_OWNER");
                    return roleRepository.save(role);
                });

        // Criar usuário de teste
        testUser = new User();
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setCpf("12345678901");
        testUser.addRole(farmOwnerRole);
        testUser = userRepository.save(testUser);
    }

    @Test
    void shouldLoginSuccessfully() throws Exception {
        // Arrange
        LoginRequestDTO loginRequest = new LoginRequestDTO("test@example.com", "password123");

        // Act & Assert
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.user.email").value("test@example.com"))
                .andExpect(jsonPath("$.user.name").value("Test User"))
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        LoginResponseDTO response = objectMapper.readValue(responseContent, LoginResponseDTO.class);
        
        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(3600L, response.getExpiresIn());
    }

    @Test
    void shouldFailLoginWithInvalidCredentials() throws Exception {
        // Arrange
        LoginRequestDTO loginRequest = new LoginRequestDTO("test@example.com", "wrongpassword");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Email ou senha inválidos"));
    }

    @Test
    void shouldFailLoginWithInvalidEmail() throws Exception {
        // Arrange
        LoginRequestDTO loginRequest = new LoginRequestDTO("nonexistent@example.com", "password123");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Email ou senha inválidos"));
    }

    @Test
    void shouldFailLoginWithEmptyEmail() throws Exception {
        // Arrange
        LoginRequestDTO loginRequest = new LoginRequestDTO("", "password123");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void shouldFailLoginWithEmptyPassword() throws Exception {
        // Arrange
        LoginRequestDTO loginRequest = new LoginRequestDTO("test@example.com", "");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void shouldFailGetCurrentUserWithoutAuthentication() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldGetCurrentUserWithValidToken() throws Exception {
        // Arrange - First login to get token
        LoginRequestDTO loginRequest = new LoginRequestDTO("test@example.com", "password123");
        
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String loginResponse = loginResult.getResponse().getContentAsString();
        LoginResponseDTO loginResponseDTO = objectMapper.readValue(loginResponse, LoginResponseDTO.class);
        String accessToken = loginResponseDTO.getAccessToken();

        // Act & Assert - Use token to get current user
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.name").value("Test User"));
    }
}