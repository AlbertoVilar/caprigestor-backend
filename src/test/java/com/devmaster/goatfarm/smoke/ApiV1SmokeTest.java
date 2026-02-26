package com.devmaster.goatfarm.smoke;

import com.devmaster.goatfarm.authority.persistence.entity.Role;
import com.devmaster.goatfarm.authority.persistence.entity.User;
import com.devmaster.goatfarm.authority.persistence.repository.RoleRepository;
import com.devmaster.goatfarm.authority.persistence.repository.UserRepository;
import com.devmaster.goatfarm.farm.persistence.entity.GoatFarm;
import com.devmaster.goatfarm.farm.persistence.repository.GoatFarmRepository;
import com.fasterxml.jackson.databind.JsonNode;
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
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiV1SmokeTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private GoatFarmRepository goatFarmRepository;

    private String ownerEmail;
    private String ownerPassword;
    private Long ownerFarmId;

    @BeforeEach
    void setUp() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        ownerPassword = "password";

        Role ownerRole = roleRepository.findByAuthority("ROLE_FARM_OWNER")
                .orElseGet(() -> roleRepository.save(new Role("ROLE_FARM_OWNER", "Farm owner")));

        ownerEmail = "smoke.owner." + suffix + "@example.com";
        String cpf = String.format("%011d", ThreadLocalRandom.current().nextLong(0L, 100_000_000_000L));

        User ownerUser = new User();
        ownerUser.setName("Smoke Owner " + suffix);
        ownerUser.setEmail(ownerEmail);
        ownerUser.setCpf(cpf);
        ownerUser.setPassword(passwordEncoder.encode(ownerPassword));
        ownerUser.addRole(ownerRole);
        ownerUser = userRepository.save(ownerUser);

        GoatFarm farm = new GoatFarm();
        farm.setName("Smoke Farm " + suffix);
        farm.setUser(ownerUser);
        farm = goatFarmRepository.save(farm);
        ownerFarmId = farm.getId();
    }

    @Test
    void should_login_and_return_token_or_auth_response() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload(ownerEmail, ownerPassword)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty());
    }

    @Test
    void should_access_one_core_endpoint_under_api_v1() throws Exception {
        String token = loginAndGetToken(ownerEmail, ownerPassword);

        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(ownerEmail));
    }

    @Test
    void should_create_or_list_goats_or_farms_under_api_v1() throws Exception {
        mockMvc.perform(get("/api/v1/goatfarms")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void should_hit_reproduction_alerts_under_api_v1() throws Exception {
        String token = loginAndGetToken(ownerEmail, ownerPassword);

        mockMvc.perform(get("/api/v1/goatfarms/{farmId}/reproduction/alerts/pregnancy-diagnosis", ownerFarmId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPending").exists())
                .andExpect(jsonPath("$.alerts").isArray());
    }

    @Test
    void should_hit_milk_alerts_or_lactation_summary_under_api_v1() throws Exception {
        String token = loginAndGetToken(ownerEmail, ownerPassword);

        mockMvc.perform(get("/api/v1/goatfarms/{farmId}/milk/alerts/dry-off", ownerFarmId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPending").exists())
                .andExpect(jsonPath("$.alerts").isArray());
    }

    @Test
    void should_hit_health_calendar_or_alerts_under_api_v1() throws Exception {
        String token = loginAndGetToken(ownerEmail, ownerPassword);

        mockMvc.perform(get("/api/v1/goatfarms/{farmId}/health-events/alerts", ownerFarmId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dueTodayCount").exists())
                .andExpect(jsonPath("$.windowDays").exists());
    }

    @Test
    void should_reject_invalid_inventory_movement_payload_under_api_v1() throws Exception {
        String token = loginAndGetToken(ownerEmail, ownerPassword);

        mockMvc.perform(post("/api/v1/goatfarms/{farmId}/inventory/movements", ownerFarmId)
                        .header("Authorization", "Bearer " + token)
                        .header("Idempotency-Key", "smoke-key-" + UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnprocessableEntity());
    }

    private String loginAndGetToken(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload(email, password)))
                .andExpect(status().isOk())
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        JsonNode node = objectMapper.readTree(responseJson);
        return node.get("accessToken").asText();
    }

    private String loginPayload(String email, String password) {
        return "{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}";
    }
}
