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
import com.devmaster.goatfarm.milk.enums.LactationStatus;
import com.devmaster.goatfarm.milk.persistence.entity.Lactation;
import com.devmaster.goatfarm.milk.persistence.repository.LactationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MilkFarmDryOffAlertsIntegrationTest {

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
    private JdbcTemplate jdbcTemplate;

    private User ownerUser;
    private GoatFarm ownerFarm;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM reproductive_event");
        jdbcTemplate.update("DELETE FROM pregnancy");
        jdbcTemplate.update("DELETE FROM milk_production");
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
    }

    @Test
    void shouldReturnOnlyDryOffEligibleAlertsAsOfReferenceDate() throws Exception {
        String token = loginAndGetToken("owner@example.com", "password");
        LocalDate referenceDate = LocalDate.of(2026, 2, 1);

        createGoatAndActiveLactation("GOAT-ALERT", 90);
        createGoatAndActiveLactation("GOAT-NOT-YET", 90);
        createGoatAndActiveLactation("GOAT-CLOSED", 90);

        insertPregnancy(ownerFarm.getId(), "GOAT-ALERT", "ACTIVE",
                LocalDate.of(2025, 10, 20), LocalDate.of(2025, 12, 20), null);
        insertPregnancy(ownerFarm.getId(), "GOAT-NOT-YET", "ACTIVE",
                LocalDate.of(2026, 1, 10), null, null);
        insertPregnancy(ownerFarm.getId(), "GOAT-CLOSED", "ACTIVE",
                LocalDate.of(2025, 10, 1), LocalDate.of(2025, 12, 1), LocalDate.of(2026, 1, 20));

        mockMvc.perform(get("/api/goatfarms/{farmId}/milk/alerts/dry-off", ownerFarm.getId())
                        .param("referenceDate", referenceDate.toString())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPending").value(1))
                .andExpect(jsonPath("$.alerts.length()").value(1))
                .andExpect(jsonPath("$.alerts[0].goatId").value("GOAT-ALERT"))
                .andExpect(jsonPath("$.alerts[0].dryOffRecommendation").value(true))
                .andExpect(jsonPath("$.alerts[0].dryAtPregnancyDays").value(90))
                .andExpect(jsonPath("$.alerts[0].gestationDays").value(104))
                .andExpect(jsonPath("$.alerts[0].daysOverdue").value(14))
                .andExpect(jsonPath("$.alerts[0].dryOffDate").value("2026-01-18"))
                .andExpect(jsonPath("$.alerts[0].startDatePregnancy").value("2025-10-20"));
    }

    @Test
    void shouldPaginateDryOffAlertsAndReturnTotalPending() throws Exception {
        String token = loginAndGetToken("owner@example.com", "password");
        LocalDate referenceDate = LocalDate.of(2026, 2, 1);

        for (int index = 1; index <= 5; index++) {
            String goatId = String.format("GOAT-%03d", index);
            createGoatAndActiveLactation(goatId, 90);
            insertPregnancy(ownerFarm.getId(), goatId, "ACTIVE",
                    LocalDate.of(2025, 10, 20), null, null);
        }

        mockMvc.perform(get("/api/goatfarms/{farmId}/milk/alerts/dry-off", ownerFarm.getId())
                        .param("referenceDate", referenceDate.toString())
                        .param("page", "0")
                        .param("size", "2")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPending").value(5))
                .andExpect(jsonPath("$.alerts.length()").value(2))
                .andExpect(jsonPath("$.alerts[0].goatId").value("GOAT-001"))
                .andExpect(jsonPath("$.alerts[1].goatId").value("GOAT-002"));

        mockMvc.perform(get("/api/goatfarms/{farmId}/milk/alerts/dry-off", ownerFarm.getId())
                        .param("referenceDate", referenceDate.toString())
                        .param("page", "2")
                        .param("size", "2")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPending").value(5))
                .andExpect(jsonPath("$.alerts.length()").value(1))
                .andExpect(jsonPath("$.alerts[0].goatId").value("GOAT-005"));
    }

    @Test
    void shouldIgnoreFuturePregnancySnapshotWhenSelectingAsOfReferenceDate() throws Exception {
        String token = loginAndGetToken("owner@example.com", "password");
        LocalDate referenceDate = LocalDate.of(2026, 2, 1);

        createGoatAndActiveLactation("GOAT-HISTORY", 90);

        insertPregnancy(ownerFarm.getId(), "GOAT-HISTORY", "ACTIVE",
                LocalDate.of(2025, 10, 20), LocalDate.of(2025, 12, 20), null);
        insertPregnancy(ownerFarm.getId(), "GOAT-HISTORY", "CLOSED",
                LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 20), LocalDate.of(2026, 3, 25));

        mockMvc.perform(get("/api/goatfarms/{farmId}/milk/alerts/dry-off", ownerFarm.getId())
                        .param("referenceDate", referenceDate.toString())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPending").value(1))
                .andExpect(jsonPath("$.alerts.length()").value(1))
                .andExpect(jsonPath("$.alerts[0].goatId").value("GOAT-HISTORY"))
                .andExpect(jsonPath("$.alerts[0].breedingDate").value("2025-10-20"));
    }

    @Test
    void shouldTreatPregnancyAsActiveOnlyBeforeClosedAt_inAsOfQueries() throws Exception {
        String token = loginAndGetToken("owner@example.com", "password");
        createGoatAndActiveLactation("GOAT-AS-OF", 90);

        insertPregnancy(ownerFarm.getId(), "GOAT-AS-OF", "ACTIVE",
                LocalDate.of(2025, 10, 20), LocalDate.of(2025, 12, 20), LocalDate.of(2026, 2, 10));

        mockMvc.perform(get("/api/goatfarms/{farmId}/milk/alerts/dry-off", ownerFarm.getId())
                        .param("referenceDate", "2026-02-01")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPending").value(1))
                .andExpect(jsonPath("$.alerts.length()").value(1))
                .andExpect(jsonPath("$.alerts[0].goatId").value("GOAT-AS-OF"));

        mockMvc.perform(get("/api/goatfarms/{farmId}/milk/alerts/dry-off", ownerFarm.getId())
                        .param("referenceDate", "2026-02-20")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPending").value(0))
                .andExpect(jsonPath("$.alerts.length()").value(0));
    }

    private Goat createGoatAndActiveLactation(String goatId, Integer dryAtPregnancyDays) {
        Goat goat = new Goat();
        goat.setName("Goat " + goatId);
        goat.setRegistrationNumber(goatId);
        goat.setFarm(ownerFarm);
        goat.setGender(Gender.FEMEA);
        goat.setBirthDate(LocalDate.of(2023, 1, 1));
        goat.setStatus(GoatStatus.ATIVO);
        goat = goatRepository.save(goat);

        Lactation lactation = new Lactation();
        lactation.setFarmId(ownerFarm.getId());
        lactation.setGoatId(goatId);
        lactation.setStatus(LactationStatus.ACTIVE);
        lactation.setStartDate(LocalDate.of(2025, 1, 1));
        lactation.setDryAtPregnancyDays(dryAtPregnancyDays);
        lactationRepository.save(lactation);

        return goat;
    }

    private void insertPregnancy(Long farmId,
                                 String goatId,
                                 String status,
                                 LocalDate breedingDate,
                                 LocalDate confirmDate,
                                 LocalDate closedAt) {
        LocalDateTime now = LocalDateTime.now();
        jdbcTemplate.update("""
                INSERT INTO pregnancy (
                    farm_id, goat_id, status, breeding_date, confirm_date, expected_due_date, closed_at,
                    close_reason, notes, created_at, updated_at
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                farmId,
                goatId,
                status,
                breedingDate,
                confirmDate,
                breedingDate != null ? breedingDate.plusDays(150) : null,
                closedAt,
                closedAt != null ? "FALSE_POSITIVE" : null,
                null,
                Timestamp.valueOf(now),
                Timestamp.valueOf(now));
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
