package com.devmaster.goatfarm.reproduction.api;

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
import com.devmaster.goatfarm.reproduction.enums.BreedingType;
import com.devmaster.goatfarm.reproduction.enums.PregnancyCheckResult;
import com.devmaster.goatfarm.reproduction.enums.ReproductiveEventType;
import com.devmaster.goatfarm.reproduction.persistence.entity.ReproductiveEvent;
import com.devmaster.goatfarm.reproduction.persistence.repository.PregnancyRepository;
import com.devmaster.goatfarm.reproduction.persistence.repository.ReproductiveEventRepository;
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

import java.time.LocalDate;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReproductionFarmPregnancyDiagnosisAlertsIntegrationTest {

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
    private ReproductiveEventRepository reproductiveEventRepository;

    @Autowired
    private PregnancyRepository pregnancyRepository;

    private User ownerUser;
    private GoatFarm ownerFarm;

    @BeforeEach
    void setUp() {
        pregnancyRepository.deleteAll();
        reproductiveEventRepository.deleteAll();
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
    }

    @Test
    void shouldReturnOnlyEligiblePendingAlerts() throws Exception {
        String token = loginAndGetToken("owner@example.com", "password");
        LocalDate referenceDate = LocalDate.now();

        Goat goatEligible = saveGoat("GOAT-ELIGIBLE");
        Goat goatNotEligible = saveGoat("GOAT-NOT-ELIGIBLE");
        Goat goatResolved = saveGoat("GOAT-RESOLVED");

        saveCoverage(goatEligible.getRegistrationNumber(), referenceDate.minusDays(70));
        saveCoverage(goatNotEligible.getRegistrationNumber(), referenceDate.minusDays(30));
        saveCoverage(goatResolved.getRegistrationNumber(), referenceDate.minusDays(70));
        saveCheck(goatResolved.getRegistrationNumber(), referenceDate.minusDays(2), PregnancyCheckResult.NEGATIVE);

        mockMvc.perform(get("/api/goatfarms/{farmId}/reproduction/alerts/pregnancy-diagnosis", ownerFarm.getId())
                        .param("referenceDate", referenceDate.toString())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPending").value(1))
                .andExpect(jsonPath("$.alerts.length()").value(1))
                .andExpect(jsonPath("$.alerts[0].goatId").value(goatEligible.getRegistrationNumber()))
                .andExpect(jsonPath("$.alerts[0].eligibleDate").value(referenceDate.minusDays(10).toString()))
                .andExpect(jsonPath("$.alerts[0].daysOverdue").value(10))
                .andExpect(jsonPath("$.alerts[0].lastCoverageDate").value(referenceDate.minusDays(70).toString()));
    }

    @Test
    void shouldPaginateAlertsAndReturnTotalPending() throws Exception {
        String token = loginAndGetToken("owner@example.com", "password");
        LocalDate referenceDate = LocalDate.now();

        List<String> goatIds = List.of("GOAT-001", "GOAT-002", "GOAT-003", "GOAT-004", "GOAT-005");
        for (int index = 0; index < goatIds.size(); index++) {
            saveGoat(goatIds.get(index));
            saveCoverage(goatIds.get(index), referenceDate.minusDays(90 - index));
        }

        mockMvc.perform(get("/api/goatfarms/{farmId}/reproduction/alerts/pregnancy-diagnosis", ownerFarm.getId())
                        .param("referenceDate", referenceDate.toString())
                        .param("page", "0")
                        .param("size", "2")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPending").value(5))
                .andExpect(jsonPath("$.alerts.length()").value(2))
                .andExpect(jsonPath("$.alerts[0].goatId").value("GOAT-001"))
                .andExpect(jsonPath("$.alerts[1].goatId").value("GOAT-002"));

        mockMvc.perform(get("/api/goatfarms/{farmId}/reproduction/alerts/pregnancy-diagnosis", ownerFarm.getId())
                        .param("referenceDate", referenceDate.toString())
                        .param("page", "2")
                        .param("size", "2")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPending").value(5))
                .andExpect(jsonPath("$.alerts.length()").value(1))
                .andExpect(jsonPath("$.alerts[0].goatId").value("GOAT-005"));
    }

    private Goat saveGoat(String registrationNumber) {
        Goat goat = new Goat();
        goat.setRegistrationNumber(registrationNumber);
        goat.setName("Goat " + registrationNumber);
        goat.setGender(Gender.FEMEA);
        goat.setBirthDate(LocalDate.now().minusYears(2));
        goat.setFarm(ownerFarm);
        goat.setStatus(GoatStatus.ATIVO);
        return goatRepository.save(goat);
    }

    private void saveCoverage(String goatId, LocalDate coverageDate) {
        ReproductiveEvent coverage = ReproductiveEvent.builder()
                .farmId(ownerFarm.getId())
                .goatId(goatId)
                .eventType(ReproductiveEventType.COVERAGE)
                .eventDate(coverageDate)
                .breedingType(BreedingType.NATURAL)
                .build();
        reproductiveEventRepository.save(coverage);
    }

    private void saveCheck(String goatId, LocalDate checkDate, PregnancyCheckResult result) {
        ReproductiveEvent check = ReproductiveEvent.builder()
                .farmId(ownerFarm.getId())
                .goatId(goatId)
                .eventType(ReproductiveEventType.PREGNANCY_CHECK)
                .eventDate(checkDate)
                .checkResult(result)
                .build();
        reproductiveEventRepository.save(check);
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
