package com.devmaster.goatfarm.milk.api;

import com.devmaster.goatfarm.authority.persistence.entity.Role;
import com.devmaster.goatfarm.authority.persistence.entity.User;
import com.devmaster.goatfarm.authority.persistence.repository.UserRepository;
import com.devmaster.goatfarm.authority.persistence.repository.RoleRepository;
import com.devmaster.goatfarm.farm.persistence.entity.GoatFarm;
import com.devmaster.goatfarm.farm.persistence.repository.GoatFarmRepository;
import com.devmaster.goatfarm.goat.enums.Gender;
import com.devmaster.goatfarm.goat.enums.GoatStatus;
import com.devmaster.goatfarm.goat.model.entity.Goat;
import com.devmaster.goatfarm.goat.model.repository.GoatRepository;
import com.devmaster.goatfarm.milk.enums.LactationStatus;
import com.devmaster.goatfarm.milk.enums.MilkingShift;
import com.devmaster.goatfarm.milk.model.entity.Lactation;
import com.devmaster.goatfarm.milk.model.entity.MilkProduction;
import com.devmaster.goatfarm.milk.model.repository.LactationRepository;
import com.devmaster.goatfarm.milk.model.repository.MilkProductionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LactationSummaryIntegrationTest {

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
    private Lactation lactation;

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
        ownerGoat.setName("Goat 1");
        ownerGoat.setRegistrationNumber("GOAT-001");
        ownerGoat.setFarm(ownerFarm);
        ownerGoat.setGender(Gender.FEMEA);
        ownerGoat.setBirthDate(LocalDate.now().minusYears(2));
        ownerGoat.setStatus(GoatStatus.ATIVO);
        ownerGoat = goatRepository.save(ownerGoat);

        lactation = new Lactation();
        lactation.setFarmId(ownerFarm.getId());
        lactation.setGoatId(ownerGoat.getRegistrationNumber());
        lactation.setStartDate(LocalDate.now().minusDays(5));
        lactation.setStatus(LactationStatus.ACTIVE);
        lactation = lactationRepository.save(lactation);

        MilkProduction production1 = new MilkProduction();
        production1.setFarmId(ownerFarm.getId());
        production1.setGoatId(ownerGoat.getRegistrationNumber());
        production1.setLactation(lactation);
        production1.setDate(LocalDate.now().minusDays(2));
        production1.setVolumeLiters(new BigDecimal("2.0"));
        production1.setShift(MilkingShift.MORNING);
        milkProductionRepository.save(production1);

        MilkProduction production2 = new MilkProduction();
        production2.setFarmId(ownerFarm.getId());
        production2.setGoatId(ownerGoat.getRegistrationNumber());
        production2.setLactation(lactation);
        production2.setDate(LocalDate.now().minusDays(2));
        production2.setVolumeLiters(new BigDecimal("1.0"));
        production2.setShift(MilkingShift.AFTERNOON);
        milkProductionRepository.save(production2);

        MilkProduction production3 = new MilkProduction();
        production3.setFarmId(ownerFarm.getId());
        production3.setGoatId(ownerGoat.getRegistrationNumber());
        production3.setLactation(lactation);
        production3.setDate(LocalDate.now().minusDays(1));
        production3.setVolumeLiters(new BigDecimal("1.0"));
        production3.setShift(MilkingShift.MORNING);
        milkProductionRepository.save(production3);
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
    void summaryShouldReturn200WithMetrics() throws Exception {
        String token = loginAndGetToken("owner@example.com", "password");

        int expectedDaysInLactation = (int) (java.time.temporal.ChronoUnit.DAYS.between(lactation.getStartDate(), LocalDate.now()) + 1);

        mockMvc.perform(get("/api/goatfarms/" + ownerFarm.getId()
                + "/goats/" + ownerGoat.getRegistrationNumber()
                + "/lactations/" + lactation.getId() + "/summary")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lactation.lactationId").value(lactation.getId()))
                .andExpect(jsonPath("$.lactation.goatId").value(ownerGoat.getRegistrationNumber()))
                .andExpect(jsonPath("$.production.totalLiters").value(4.0))
                .andExpect(jsonPath("$.production.daysMeasured").value(2))
                .andExpect(jsonPath("$.production.averagePerDay").value(2.0))
                .andExpect(jsonPath("$.production.peakLiters").value(3.0))
                .andExpect(jsonPath("$.production.daysInLactation").value(expectedDaysInLactation))
                .andExpect(jsonPath("$.pregnancy").value(Matchers.nullValue()));
    }

    @Test
    void summaryShouldReturn200WithoutProductions() throws Exception {
        String token = loginAndGetToken("owner@example.com", "password");

        milkProductionRepository.deleteAll();

        mockMvc.perform(get("/api/goatfarms/" + ownerFarm.getId()
                + "/goats/" + ownerGoat.getRegistrationNumber()
                + "/lactations/" + lactation.getId() + "/summary")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.production.totalLiters").value(0))
                .andExpect(jsonPath("$.production.daysMeasured").value(0))
                .andExpect(jsonPath("$.production.averagePerDay").value(0))
                .andExpect(jsonPath("$.production.peakLiters").value(0))
                .andExpect(jsonPath("$.production.peakDate").value(Matchers.nullValue()));
    }

    @Test
    void summaryShouldReturn422ForMaleGoat() throws Exception {
        String token = loginAndGetToken("owner@example.com", "password");

        Goat maleGoat = new Goat();
        maleGoat.setName("Goat Male");
        maleGoat.setRegistrationNumber("GOAT-002");
        maleGoat.setFarm(ownerFarm);
        maleGoat.setGender(Gender.MACHO);
        maleGoat.setBirthDate(LocalDate.now().minusYears(2));
        maleGoat.setStatus(GoatStatus.ATIVO);
        maleGoat = goatRepository.save(maleGoat);

        Lactation maleLactation = new Lactation();
        maleLactation.setFarmId(ownerFarm.getId());
        maleLactation.setGoatId(maleGoat.getRegistrationNumber());
        maleLactation.setStartDate(LocalDate.now().minusDays(5));
        maleLactation.setStatus(LactationStatus.ACTIVE);
        maleLactation = lactationRepository.save(maleLactation);

        mockMvc.perform(get("/api/goatfarms/" + ownerFarm.getId()
                + "/goats/" + maleGoat.getRegistrationNumber()
                + "/lactations/" + maleLactation.getId() + "/summary")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errors[0].message").value("Apenas fêmeas podem ter lactação."));
    }

    @Test
    void summaryShouldReturn404WhenLactationMissing() throws Exception {
        String token = loginAndGetToken("owner@example.com", "password");

        mockMvc.perform(get("/api/goatfarms/" + ownerFarm.getId()
                + "/goats/" + ownerGoat.getRegistrationNumber()
                + "/lactations/99999/summary")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }
}
