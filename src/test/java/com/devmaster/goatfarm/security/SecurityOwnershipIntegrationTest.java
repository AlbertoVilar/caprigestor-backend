package com.devmaster.goatfarm.security;

import com.devmaster.goatfarm.events.enuns.EventType;
import com.devmaster.goatfarm.events.model.entity.Event;
import com.devmaster.goatfarm.events.model.repository.EventRepository;
import com.devmaster.goatfarm.farm.model.entity.GoatFarm;
import com.devmaster.goatfarm.farm.model.repository.GoatFarmRepository;
import com.devmaster.goatfarm.goat.enums.Gender;
import com.devmaster.goatfarm.goat.enums.GoatStatus;
import com.devmaster.goatfarm.goat.model.entity.Goat;
import com.devmaster.goatfarm.goat.model.repository.GoatRepository;
import com.devmaster.goatfarm.milk.enums.LactationStatus;
import com.devmaster.goatfarm.milk.model.entity.Lactation;
import com.devmaster.goatfarm.milk.model.repository.LactationRepository;
import com.devmaster.goatfarm.milk.model.entity.MilkProduction;
import com.devmaster.goatfarm.milk.model.repository.MilkProductionRepository;
import com.devmaster.goatfarm.milk.enums.MilkingShift;
import com.devmaster.goatfarm.authority.model.entity.Role;
import com.devmaster.goatfarm.authority.model.entity.User;
import com.devmaster.goatfarm.authority.repository.RoleRepository;
import com.devmaster.goatfarm.authority.model.repository.UserRepository;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class SecurityOwnershipIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private GoatFarmRepository goatFarmRepository;

    @Autowired
    private GoatRepository goatRepository;

    @Autowired
    private LactationRepository lactationRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private MilkProductionRepository milkProductionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User adminUser;
    private User ownerUser;
    private User otherUser;
    private GoatFarm ownerFarm;
    private Goat ownerGoat;
    private Goat anotherGoat;

    @BeforeEach
    void setUp() {
        milkProductionRepository.deleteAll();
        lactationRepository.deleteAll();
        eventRepository.deleteAll();
        goatRepository.deleteAll();
        goatFarmRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();

        Role roleAdmin = roleRepository.save(new Role("ROLE_ADMIN", "Admin"));
        Role roleOwner = roleRepository.save(new Role("ROLE_FARM_OWNER", "Farm owner"));
        Role roleOperator = roleRepository.save(new Role("ROLE_OPERATOR", "Operator"));

        adminUser = new User();
        adminUser.setName("Admin");
        adminUser.setEmail("admin@example.com");
        adminUser.setCpf("00000000000");
        adminUser.setPassword(passwordEncoder.encode("password"));
        adminUser.addRole(roleAdmin);
        userRepository.save(adminUser);

        ownerUser = new User();
        ownerUser.setName("Owner");
        ownerUser.setEmail("owner@example.com");
        ownerUser.setCpf("00000000001");
        ownerUser.setPassword(passwordEncoder.encode("password"));
        ownerUser.addRole(roleOwner);
        userRepository.save(ownerUser);

        otherUser = new User();
        otherUser.setName("Other");
        otherUser.setEmail("other@example.com");
        otherUser.setCpf("00000000002");
        otherUser.setPassword(passwordEncoder.encode("password"));
        otherUser.addRole(roleOwner);
        userRepository.save(otherUser);

        ownerFarm = new GoatFarm();
        ownerFarm.setName("Owner Farm");
        ownerFarm.setUser(ownerUser);
        goatFarmRepository.save(ownerFarm);

        ownerGoat = new Goat();
        ownerGoat.setName("Goat 1");
        ownerGoat.setRegistrationNumber("GOAT-001");
        ownerGoat.setFarm(ownerFarm);
        ownerGoat.setGender(Gender.FEMEA);
        ownerGoat.setBirthDate(LocalDate.now().minusYears(2));
        ownerGoat.setStatus(GoatStatus.ATIVO);
        goatRepository.save(ownerGoat);

        anotherGoat = new Goat();
        anotherGoat.setName("Goat 2");
        anotherGoat.setRegistrationNumber("GOAT-002");
        anotherGoat.setFarm(ownerFarm);
        anotherGoat.setGender(Gender.FEMEA);
        anotherGoat.setBirthDate(LocalDate.now().minusYears(1));
        anotherGoat.setStatus(GoatStatus.ATIVO);
        goatRepository.save(anotherGoat);

        Lactation lactation = new Lactation();
        lactation.setFarmId(ownerFarm.getId());
        lactation.setGoatId(ownerGoat.getRegistrationNumber());
        lactation.setStartDate(LocalDate.now().minusDays(10));
        lactation.setStatus(LactationStatus.ACTIVE);
        lactationRepository.save(lactation);

        Event event = new Event();
        event.setGoat(ownerGoat);
        event.setDate(LocalDate.now().minusDays(5));
        event.setEventType(EventType.COBERTURA);
        eventRepository.save(event);

        MilkProduction production = new MilkProduction();
        production.setFarmId(ownerFarm.getId());
        production.setGoatId(ownerGoat.getRegistrationNumber());
        production.setLactation(lactation);
        production.setDate(LocalDate.now());
        production.setVolumeLiters(new BigDecimal("2.5"));
        production.setShift(MilkingShift.MORNING);
        milkProductionRepository.save(production);
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

    private String buildGoatFarmUpdatePayload() throws Exception {
        java.util.Map<String, Object> farm = new java.util.HashMap<>();
        farm.put("name", "Owner Farm");
        farm.put("tod", "ABCDE");

        java.util.Map<String, Object> user = new java.util.HashMap<>();
        user.put("name", "Owner");
        user.put("email", "owner@example.com");
        user.put("cpf", "00000000001");

        java.util.Map<String, Object> address = new java.util.HashMap<>();
        address.put("street", "Rua A");
        address.put("neighborhood", "Centro");
        address.put("city", "São Paulo");
        address.put("state", "SP");
        address.put("zipCode", "01000-000");
        address.put("country", "Brasil");

        java.util.Map<String, Object> phone = new java.util.HashMap<>();
        phone.put("ddd", "11");
        phone.put("number", "99999999");

        java.util.Map<String, Object> payload = new java.util.HashMap<>();
        payload.put("farm", farm);
        payload.put("user", user);
        payload.put("address", address);
        payload.put("phones", java.util.List.of(phone));

        return objectMapper.writeValueAsString(payload);
    }

    @Test
    void publicEndpoints_shouldBeAccessibleWithoutToken() throws Exception {
        mockMvc.perform(get("/api/goatfarms")).andExpect(status().isOk());
        mockMvc.perform(get("/api/goatfarms/" + ownerFarm.getId() + "/goats")).andExpect(status().isOk());
        mockMvc.perform(get("/api/goatfarms/" + ownerFarm.getId()
                + "/goats/" + ownerGoat.getRegistrationNumber() + "/genealogies"))
                .andExpect(status().isOk());
    }

    @Test
    void privateLactationEndpoints_shouldReturn401WithoutToken() throws Exception {
        mockMvc.perform(get("/api/goatfarms/" + ownerFarm.getId()
                + "/goats/" + ownerGoat.getRegistrationNumber() + "/lactations/active"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void privateReproductionEndpoints_shouldReturn401WithoutToken() throws Exception {
        mockMvc.perform(get("/api/goatfarms/" + ownerFarm.getId()
                + "/goats/" + ownerGoat.getRegistrationNumber() + "/events"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void privateMilkProductionEndpoints_shouldReturn401WithoutToken() throws Exception {
        mockMvc.perform(get("/api/goatfarms/" + ownerFarm.getId()
                + "/goats/" + ownerGoat.getRegistrationNumber() + "/milk-productions"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void privateEndpoints_shouldReturn403ForNonOwnerUser() throws Exception {
        String token = loginAndGetToken("other@example.com", "password");

        mockMvc.perform(get("/api/goatfarms/" + ownerFarm.getId()
                + "/goats/" + ownerGoat.getRegistrationNumber() + "/lactations/active")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/goatfarms/" + ownerFarm.getId()
                + "/goats/" + ownerGoat.getRegistrationNumber() + "/events")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/goatfarms/" + ownerFarm.getId()
                + "/goats/" + ownerGoat.getRegistrationNumber() + "/milk-productions")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void privateEndpoints_shouldReturn200ForOwnerUser() throws Exception {
        String token = loginAndGetToken("owner@example.com", "password");

        mockMvc.perform(get("/api/goatfarms/" + ownerFarm.getId()
                + "/goats/" + ownerGoat.getRegistrationNumber() + "/lactations/active")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // Reproduction events might be empty but 200 OK
        mockMvc.perform(get("/api/goatfarms/" + ownerFarm.getId()
                + "/goats/" + ownerGoat.getRegistrationNumber() + "/events")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/goatfarms/" + ownerFarm.getId()
                + "/goats/" + ownerGoat.getRegistrationNumber() + "/milk-productions")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void privateEndpoints_shouldBypassOwnershipForAdmin() throws Exception {
        String token = loginAndGetToken("admin@example.com", "password");

        mockMvc.perform(get("/api/goatfarms/" + ownerFarm.getId()
                + "/goats/" + ownerGoat.getRegistrationNumber() + "/lactations/active")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/goatfarms/" + ownerFarm.getId()
                + "/goats/" + ownerGoat.getRegistrationNumber() + "/events")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/goatfarms/" + ownerFarm.getId()
                + "/goats/" + ownerGoat.getRegistrationNumber() + "/milk-productions")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void goatFarmUpdate_shouldReturn401WithoutToken() throws Exception {
        String payload = buildGoatFarmUpdatePayload();
        mockMvc.perform(put("/api/goatfarms/" + ownerFarm.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void goatFarmUpdate_shouldReturn403ForNonOwnerUser() throws Exception {
        String token = loginAndGetToken("other@example.com", "password");
        String payload = buildGoatFarmUpdatePayload();

        mockMvc.perform(put("/api/goatfarms/" + ownerFarm.getId())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isForbidden());
    }
}
