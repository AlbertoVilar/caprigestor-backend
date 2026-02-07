package com.devmaster.goatfarm.milk.api;

import com.devmaster.goatfarm.authority.persistence.entity.Role;
import com.devmaster.goatfarm.authority.persistence.entity.User;
import com.devmaster.goatfarm.authority.persistence.repository.RoleRepository;
import com.devmaster.goatfarm.authority.persistence.repository.UserRepository;
import com.devmaster.goatfarm.farm.persistence.entity.GoatFarm;
import com.devmaster.goatfarm.farm.persistence.repository.GoatFarmRepository;
import com.devmaster.goatfarm.goat.enums.Gender;
import com.devmaster.goatfarm.goat.enums.GoatStatus;
import com.devmaster.goatfarm.goat.persistence.entity.Goat;
import com.devmaster.goatfarm.goat.persistence.repository.GoatRepository;
import com.devmaster.goatfarm.milk.api.dto.MilkProductionRequestDTO;
import com.devmaster.goatfarm.milk.enums.LactationStatus;
import com.devmaster.goatfarm.milk.enums.MilkProductionStatus;
import com.devmaster.goatfarm.milk.enums.MilkingShift;
import com.devmaster.goatfarm.milk.persistence.entity.Lactation;
import com.devmaster.goatfarm.milk.persistence.entity.MilkProduction;
import com.devmaster.goatfarm.milk.persistence.repository.LactationRepository;
import com.devmaster.goatfarm.milk.persistence.repository.MilkProductionRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MilkProductionCancellationIntegrationTest {

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

    @Autowired
    private GoatRepository goatRepository;

    @Autowired
    private LactationRepository lactationRepository;

    @Autowired
    private MilkProductionRepository milkProductionRepository;

    private User ownerUser;
    private GoatFarm ownerFarm;
    private Goat ownerGoat;
    private Lactation activeLactation;

    @BeforeEach
    void setUp() {
        milkProductionRepository.deleteAll();
        lactationRepository.deleteAll();
        goatRepository.deleteAll();
        goatFarmRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();

        Role roleOwner = roleRepository.save(new Role("ROLE_FARM_OWNER", "Farm owner"));

        ownerUser = new User();
        ownerUser.setName("Owner");
        ownerUser.setEmail("owner@example.com");
        ownerUser.setCpf("00000000001");
        ownerUser.setPassword(passwordEncoder.encode("password"));
        ownerUser.addRole(roleOwner);
        userRepository.save(ownerUser);

        ownerFarm = new GoatFarm();
        ownerFarm.setName("Owner Farm");
        ownerFarm.setUser(ownerUser);
        ownerFarm = goatFarmRepository.save(ownerFarm);

        ownerGoat = new Goat();
        ownerGoat.setRegistrationNumber("GOAT-001");
        ownerGoat.setName("Mimosinha");
        ownerGoat.setGender(Gender.FEMEA);
        ownerGoat.setBirthDate(LocalDate.now().minusYears(2));
        ownerGoat.setFarm(ownerFarm);
        ownerGoat.setStatus(GoatStatus.ATIVO);
        ownerGoat = goatRepository.save(ownerGoat);

        activeLactation = new Lactation();
        activeLactation.setFarmId(ownerFarm.getId());
        activeLactation.setGoatId(ownerGoat.getRegistrationNumber());
        activeLactation.setStartDate(LocalDate.now().minusDays(10));
        activeLactation.setStatus(LactationStatus.ACTIVE);
        activeLactation = lactationRepository.save(activeLactation);
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

    @Test
    void shouldCancelMilkProductionAndHideFromDefaultList() throws Exception {
        String token = loginAndGetToken("owner@example.com", "password");
        MilkProduction production = saveMilkProduction(LocalDate.now().minusDays(1), MilkingShift.MORNING);

        mockMvc.perform(delete("/api/goatfarms/{farmId}/goats/{goatId}/milk-productions/{id}",
                        ownerFarm.getId(), ownerGoat.getRegistrationNumber(), production.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        MilkProduction canceled = milkProductionRepository.findById(production.getId()).orElseThrow();
        assertThat(canceled.getStatus()).isEqualTo(MilkProductionStatus.CANCELED);
        assertThat(canceled.getCanceledAt()).isNotNull();

        mockMvc.perform(get("/api/goatfarms/{farmId}/goats/{goatId}/milk-productions",
                        ownerFarm.getId(), ownerGoat.getRegistrationNumber())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(0));

        mockMvc.perform(get("/api/goatfarms/{farmId}/goats/{goatId}/milk-productions",
                        ownerFarm.getId(), ownerGoat.getRegistrationNumber())
                        .param("includeCanceled", "true")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(production.getId()))
                .andExpect(jsonPath("$.content[0].status").value("CANCELED"));

        mockMvc.perform(get("/api/goatfarms/{farmId}/goats/{goatId}/milk-productions/{id}",
                        ownerFarm.getId(), ownerGoat.getRegistrationNumber(), production.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELED"));
    }

    @Test
    void shouldReturn422WhenNoActiveLactation() throws Exception {
        lactationRepository.deleteAll();
        String token = loginAndGetToken("owner@example.com", "password");

        MilkProductionRequestDTO request = MilkProductionRequestDTO.builder()
                .date(LocalDate.now().minusDays(1))
                .shift(MilkingShift.MORNING)
                .volumeLiters(new BigDecimal("2.5"))
                .notes("Teste")
                .build();

        mockMvc.perform(post("/api/goatfarms/{farmId}/goats/{goatId}/milk-productions",
                        ownerFarm.getId(), ownerGoat.getRegistrationNumber())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void shouldReturn409WhenDuplicateActiveProduction() throws Exception {
        String token = loginAndGetToken("owner@example.com", "password");
        LocalDate date = LocalDate.now().minusDays(1);

        saveMilkProduction(date, MilkingShift.MORNING);

        MilkProductionRequestDTO request = MilkProductionRequestDTO.builder()
                .date(date)
                .shift(MilkingShift.MORNING)
                .volumeLiters(new BigDecimal("2.5"))
                .notes("Duplicado")
                .build();

        mockMvc.perform(post("/api/goatfarms/{farmId}/goats/{goatId}/milk-productions",
                        ownerFarm.getId(), ownerGoat.getRegistrationNumber())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldAllowNewProductionWhenPreviousCanceled() throws Exception {
        String token = loginAndGetToken("owner@example.com", "password");
        LocalDate date = LocalDate.now().minusDays(1);

        MilkProduction production = saveMilkProduction(date, MilkingShift.MORNING);

        mockMvc.perform(delete("/api/goatfarms/{farmId}/goats/{goatId}/milk-productions/{id}",
                        ownerFarm.getId(), ownerGoat.getRegistrationNumber(), production.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        MilkProductionRequestDTO request = MilkProductionRequestDTO.builder()
                .date(date)
                .shift(MilkingShift.MORNING)
                .volumeLiters(new BigDecimal("2.5"))
                .notes("Reprocessado")
                .build();

        mockMvc.perform(post("/api/goatfarms/{farmId}/goats/{goatId}/milk-productions",
                        ownerFarm.getId(), ownerGoat.getRegistrationNumber())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    private MilkProduction saveMilkProduction(LocalDate date, MilkingShift shift) {
        MilkProduction production = new MilkProduction();
        production.setFarmId(ownerFarm.getId());
        production.setGoatId(ownerGoat.getRegistrationNumber());
        production.setLactation(activeLactation);
        production.setDate(date);
        production.setShift(shift);
        production.setVolumeLiters(new BigDecimal("2.5"));
        production.setNotes("Registro");
        production.setStatus(MilkProductionStatus.ACTIVE);
        return milkProductionRepository.save(production);
    }
}
