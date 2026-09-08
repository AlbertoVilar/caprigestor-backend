package com.devmaster.goatfarm.authority.api;

import com.devmaster.goatfarm.authority.persistence.entity.Role;
import com.devmaster.goatfarm.authority.persistence.entity.User;
import com.devmaster.goatfarm.authority.persistence.repository.RoleRepository;
import com.devmaster.goatfarm.authority.persistence.repository.RefreshSessionRepository;
import com.devmaster.goatfarm.authority.persistence.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private RefreshSessionRepository refreshSessionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private Role testRole;
    private User adminUser;
    private User farmOwnerUser;

    @BeforeEach
    void setUp() {
        refreshSessionRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();

        testRole = new Role();
        testRole.setAuthority("ROLE_OPERATOR");
        roleRepository.save(testRole);

        Role adminRole = new Role();
        adminRole.setAuthority("ROLE_ADMIN");
        roleRepository.save(adminRole);

        Role farmOwnerRole = new Role();
        farmOwnerRole.setAuthority("ROLE_FARM_OWNER");
        roleRepository.save(farmOwnerRole);

        testUser = createUser("test@example.com", "12345678901", "Test User", testRole);
        adminUser = createUser("admin@example.com", "12345678902", "Admin User", adminRole);
        farmOwnerUser = createUser("owner@example.com", "12345678903", "Farm Owner", farmOwnerRole);
    }

    @Test
    void shouldLoginSuccessfully() throws Exception {
        String loginPayload = "{\"email\":\"test@example.com\", \"password\":\"password\"}";

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.expiresIn").value(900));
    }

    @Test
    void shouldRotateRefreshTokenAndRevokeFamilyWhenThePreviousTokenIsReplayed() throws Exception {
        String originalRefreshToken = loginAndGetRefreshToken("test@example.com");

        String rotatedResponse = mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + originalRefreshToken + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String replacementRefreshToken = new ObjectMapper().readTree(rotatedResponse).get("refreshToken").asText();

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + originalRefreshToken + "\"}"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + replacementRefreshToken + "\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRevokeRefreshTokenFamilyOnLogout() throws Exception {
        String refreshToken = loginAndGetRefreshToken("test@example.com");

        mockMvc.perform(post("/api/v1/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldFailLoginWithInvalidCredentials() throws Exception {
        String loginPayload = "{\"email\":\"test@example.com\", \"password\":\"wrongpassword\"}";

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload))
                .andExpect(status().isBadRequest()); // ou 401, dependendo da implementação do handler
    }

    @Test
    void shouldGetCurrentUserWithValidToken() throws Exception {
        String loginPayload = "{\"email\":\"test@example.com\", \"password\":\"password\"}";

        String token = loginAndGetToken("test@example.com");

        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void shouldAllowAdminToAccessAdministrativeUserEndpointWithJwt() throws Exception {
        String token = loginAndGetToken("admin@example.com");

        mockMvc.perform(get("/api/v1/users/{id}", testUser.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void shouldForbidOperatorFromAdministrativeUserEndpointWithJwt() throws Exception {
        String token = loginAndGetToken("test@example.com");

        mockMvc.perform(get("/api/v1/users/{id}", adminUser.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldForbidFarmOwnerFromAdministrativeUserEndpointWithJwt() throws Exception {
        String token = loginAndGetToken("owner@example.com");

        mockMvc.perform(get("/api/v1/users/{id}", adminUser.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectUnauthenticatedAdministrativeUserRequest() throws Exception {
        mockMvc.perform(get("/api/v1/users/{id}", farmOwnerUser.getId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRegisterPublicUserWithDefaultRole() throws Exception {
        String registerPayload = """
                {
                  "name": "Public User",
                  "email": "public@example.com",
                  "cpf": "10987654321",
                  "password": "password123",
                  "confirmPassword": "password123"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.roles", contains("ROLE_OPERATOR")));
    }

    @Test
    void shouldNotAllowPrivilegeEscalationOnPublicRegistration() throws Exception {
        String registerPayload = """
                {
                  "name": "Public User",
                  "email": "public-admin-attempt@example.com",
                  "cpf": "10987654322",
                  "password": "password123",
                  "confirmPassword": "password123",
                  "roles": ["ROLE_ADMIN"]
                }
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].fieldName").value("roles"));
    }

    private User createUser(String email, String cpf, String name, Role role) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode("password"));
        user.setName(name);
        user.setCpf(cpf);
        user.addRole(role);
        return userRepository.save(user);
    }

    private String loginAndGetToken(String email) throws Exception {
        String loginPayload = "{\"email\":\"" + email + "\", \"password\":\"password\"}";
        String response = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        return new ObjectMapper().readTree(response).get("accessToken").asText();
    }

    private String loginAndGetRefreshToken(String email) throws Exception {
        String loginPayload = "{\"email\":\"" + email + "\", \"password\":\"password\"}";
        String response = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return new ObjectMapper().readTree(response).get("refreshToken").asText();
    }
}
