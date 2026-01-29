package com.devmaster.goatfarm.farm.api;

import com.devmaster.goatfarm.address.model.repository.AddressRepository;
import com.devmaster.goatfarm.authority.model.entity.Role;
import com.devmaster.goatfarm.authority.model.entity.User;
import com.devmaster.goatfarm.authority.model.repository.UserRepository;
import com.devmaster.goatfarm.authority.repository.RoleRepository;
import com.devmaster.goatfarm.farm.persistence.repository.GoatFarmRepository;
import com.devmaster.goatfarm.phone.model.repository.PhoneRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GoatFarmLogoIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private GoatFarmRepository goatFarmRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private PhoneRepository phoneRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        phoneRepository.deleteAll();
        goatFarmRepository.deleteAll();
        addressRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();

        roleRepository.save(new Role("ROLE_FARM_OWNER", "Farm owner"));
        Role adminRole = roleRepository.save(new Role("ROLE_ADMIN", "Admin"));

        User admin = new User();
        admin.setName("Admin");
        admin.setEmail("admin.logo@example.com");
        admin.setCpf("90000000000");
        admin.setPassword(passwordEncoder.encode("password"));
        admin.addRole(adminRole);
        userRepository.save(admin);
    }

    @Test
    void createFarmWithLogoUrlShouldAppearInDetailAndList() throws Exception {
        String token = loginAndGetToken("admin.logo@example.com", "password");

        String payload = """
                {
                  "farm": {
                    "name": "Capril Logo",
                    "tod": "LOGO1",
                    "logoUrl": "https://cdn.example.com/logo.png"
                  },
                  "user": {
                    "name": "Owner",
                    "email": "owner.logo@example.com",
                    "cpf": "12345678901",
                    "password": "password",
                    "confirmPassword": "password"
                  },
                  "address": {
                    "street": "Rua A",
                    "neighborhood": "Centro",
                    "city": "Cidade",
                    "state": "SP",
                    "zipCode": "12345-678",
                    "country": "Brasil"
                  },
                  "phones": [
                    { "ddd": "11", "number": "999999999" }
                  ]
                }
                """;

        MvcResult createResult = mockMvc.perform(post("/api/goatfarms")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.logoUrl").value("https://cdn.example.com/logo.png"))
                .andReturn();

        JsonNode created = objectMapper.readTree(createResult.getResponse().getContentAsString());
        long farmId = created.get("id").asLong();

        mockMvc.perform(get("/api/goatfarms/" + farmId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.logoUrl").value("https://cdn.example.com/logo.png"));

        mockMvc.perform(get("/api/goatfarms")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].logoUrl").value("https://cdn.example.com/logo.png"));
    }

    private String loginAndGetToken(String email, String password) throws Exception {
        String loginPayload = "{\"email\":\"" + email + "\", \"password\":\"" + password + "\"}";
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        return objectMapper.readTree(response).get("accessToken").asText();
    }
}
