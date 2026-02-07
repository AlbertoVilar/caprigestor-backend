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
import com.devmaster.goatfarm.reproduction.api.dto.CoverageCorrectionRequestDTO;
import com.devmaster.goatfarm.reproduction.api.dto.PregnancyCheckRequestDTO;
import com.devmaster.goatfarm.reproduction.api.dto.PregnancyConfirmRequestDTO;
import com.devmaster.goatfarm.reproduction.enums.BreedingType;
import com.devmaster.goatfarm.reproduction.enums.PregnancyCheckResult;
import com.devmaster.goatfarm.reproduction.enums.PregnancyCloseReason;
import com.devmaster.goatfarm.reproduction.enums.PregnancyStatus;
import com.devmaster.goatfarm.reproduction.enums.ReproductiveEventType;
import com.devmaster.goatfarm.reproduction.persistence.entity.Pregnancy;
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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReproductionRecommendationAndCorrectionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JdbcTemplate jdbcTemplate;

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
    private Goat ownerGoat;

    @BeforeEach
    void setUp() {
        pregnancyRepository.deleteAll();
        reproductiveEventRepository.deleteAll();
        goatRepository.deleteAll();
        goatFarmRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();

        try {
            jdbcTemplate.execute("CREATE UNIQUE INDEX IF NOT EXISTS ux_pregnancy_single_active_per_goat ON pregnancy(farm_id, goat_id, status)");
        } catch (Exception e) {
            System.err.println("FAILED TO CREATE INDEX: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }

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
        goatRepository.save(ownerGoat);
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
    void shouldReturnEligiblePendingRecommendation_whenCoverageEligibleAndNoCheck() throws Exception {
        String token = loginAndGetToken("owner@example.com", "password");
        LocalDate coverageDate = LocalDate.now().minusDays(70);

        ReproductiveEvent coverage = saveCoverageEvent(coverageDate);

        mockMvc.perform(get("/api/goatfarms/{farmId}/goats/{goatId}/reproduction/pregnancies/diagnosis-recommendation",
                        ownerFarm.getId(), ownerGoat.getRegistrationNumber())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ELIGIBLE_PENDING"))
                .andExpect(jsonPath("$.eligibleDate").value(coverageDate.plusDays(60).toString()))
                .andExpect(jsonPath("$.lastCoverage.id").value(coverage.getId()))
                .andExpect(jsonPath("$.warnings").isArray());
    }

    @Test
    void shouldCreateCoverageCorrectionEvent_whenRequestIsValid() throws Exception {
        String token = loginAndGetToken("owner@example.com", "password");
        LocalDate coverageDate = LocalDate.now().minusDays(10);

        ReproductiveEvent coverage = saveCoverageEvent(coverageDate);

        CoverageCorrectionRequestDTO request = CoverageCorrectionRequestDTO.builder()
                .correctedDate(coverageDate.minusDays(2))
                .notes("Ajuste manual")
                .build();

        MvcResult result = mockMvc.perform(post("/api/goatfarms/{farmId}/goats/{goatId}/reproduction/breeding/{coverageEventId}/corrections",
                        ownerFarm.getId(), ownerGoat.getRegistrationNumber(), coverage.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.eventType").value("COVERAGE_CORRECTION"))
                .andExpect(jsonPath("$.relatedEventId").value(coverage.getId()))
                .andExpect(jsonPath("$.correctedEventDate").value(request.getCorrectedDate().toString()))
                .andReturn();

        String body = result.getResponse().getContentAsString();
        assertThat(body).contains("COVERAGE_CORRECTION");
    }

    @Test
    void shouldConfirmPregnancy_whenCheckDateIsAtLeast60Days() throws Exception {
        String token = loginAndGetToken("owner@example.com", "password");
        LocalDate checkDate = LocalDate.now();
        LocalDate coverageDate = checkDate.minusDays(60);

        saveCoverageEvent(coverageDate);

        PregnancyConfirmRequestDTO request = PregnancyConfirmRequestDTO.builder()
                .checkDate(checkDate)
                .checkResult(PregnancyCheckResult.POSITIVE)
                .notes("Ultrassom")
                .build();

        mockMvc.perform(patch("/api/goatfarms/{farmId}/goats/{goatId}/reproduction/pregnancies/confirm",
                        ownerFarm.getId(), ownerGoat.getRegistrationNumber())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.confirmDate").value(checkDate.toString()))
                .andExpect(jsonPath("$.breedingDate").value(coverageDate.toString()));
    }

    @Test
    void shouldReturn422_whenConfirmIsBefore60Days() throws Exception {
        String token = loginAndGetToken("owner@example.com", "password");
        LocalDate checkDate = LocalDate.now();
        LocalDate coverageDate = checkDate.minusDays(59);

        saveCoverageEvent(coverageDate);

        PregnancyConfirmRequestDTO request = PregnancyConfirmRequestDTO.builder()
                .checkDate(checkDate)
                .checkResult(PregnancyCheckResult.POSITIVE)
                .notes("Ultrassom")
                .build();

        mockMvc.perform(patch("/api/goatfarms/{farmId}/goats/{goatId}/reproduction/pregnancies/confirm",
                        ownerFarm.getId(), ownerGoat.getRegistrationNumber())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errors[0].fieldName").value("checkDate"));
    }

    @Test
    void shouldRegisterNegativeCheck_whenCheckDateIsAtLeast60Days() throws Exception {
        String token = loginAndGetToken("owner@example.com", "password");
        LocalDate checkDate = LocalDate.now();
        LocalDate coverageDate = checkDate.minusDays(60);

        saveCoverageEvent(coverageDate);

        PregnancyCheckRequestDTO request = PregnancyCheckRequestDTO.builder()
                .checkDate(checkDate)
                .checkResult(PregnancyCheckResult.NEGATIVE)
                .notes("Sem evidências")
                .build();

        mockMvc.perform(post("/api/goatfarms/{farmId}/goats/{goatId}/reproduction/pregnancies/checks",
                        ownerFarm.getId(), ownerGoat.getRegistrationNumber())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.eventType").value("PREGNANCY_CHECK"))
                .andExpect(jsonPath("$.checkResult").value("NEGATIVE"));
    }

    @Test
    void shouldReturn422_whenNegativeCheckIsBefore60Days() throws Exception {
        String token = loginAndGetToken("owner@example.com", "password");
        LocalDate checkDate = LocalDate.now();
        LocalDate coverageDate = checkDate.minusDays(59);

        saveCoverageEvent(coverageDate);

        PregnancyCheckRequestDTO request = PregnancyCheckRequestDTO.builder()
                .checkDate(checkDate)
                .checkResult(PregnancyCheckResult.NEGATIVE)
                .notes("Sem evidências")
                .build();

        mockMvc.perform(post("/api/goatfarms/{farmId}/goats/{goatId}/reproduction/pregnancies/checks",
                        ownerFarm.getId(), ownerGoat.getRegistrationNumber())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errors[0].fieldName").value("checkDate"));
    }

    @Test
    void shouldOrderEventsByEventDateDescAndIdDesc() throws Exception {
        String token = loginAndGetToken("owner@example.com", "password");
        LocalDate eventDate = LocalDate.now().minusDays(15);

        ReproductiveEvent first = saveCoverageEvent(eventDate);
        ReproductiveEvent second = saveCoverageEvent(eventDate);

        mockMvc.perform(get("/api/goatfarms/{farmId}/goats/{goatId}/reproduction/events",
                        ownerFarm.getId(), ownerGoat.getRegistrationNumber())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(second.getId()))
                .andExpect(jsonPath("$.content[1].id").value(first.getId()));
    }

    @Test
    void shouldOrderPregnanciesByBreedingDateDescAndIdDesc() throws Exception {
        String token = loginAndGetToken("owner@example.com", "password");
        LocalDate breedingDate = LocalDate.now().minusDays(90);

        Pregnancy active = Pregnancy.builder()
                .farmId(ownerFarm.getId())
                .goatId(ownerGoat.getRegistrationNumber())
                .status(PregnancyStatus.ACTIVE)
                .breedingDate(breedingDate)
                .confirmDate(breedingDate.plusDays(60))
                .expectedDueDate(breedingDate.plusDays(150))
                .build();
        active = pregnancyRepository.save(active);

        Pregnancy closed = Pregnancy.builder()
                .farmId(ownerFarm.getId())
                .goatId(ownerGoat.getRegistrationNumber())
                .status(PregnancyStatus.CLOSED)
                .breedingDate(breedingDate)
                .confirmDate(breedingDate.plusDays(60))
                .expectedDueDate(breedingDate.plusDays(150))
                .closedAt(breedingDate.plusDays(120))
                .closeReason(PregnancyCloseReason.BIRTH)
                .build();
        closed = pregnancyRepository.save(closed);

        mockMvc.perform(get("/api/goatfarms/{farmId}/goats/{goatId}/reproduction/pregnancies",
                        ownerFarm.getId(), ownerGoat.getRegistrationNumber())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(closed.getId()))
                .andExpect(jsonPath("$.content[1].id").value(active.getId()));
    }

    private ReproductiveEvent saveCoverageEvent(LocalDate coverageDate) {
        ReproductiveEvent coverage = ReproductiveEvent.builder()
                .farmId(ownerFarm.getId())
                .goatId(ownerGoat.getRegistrationNumber())
                .eventType(ReproductiveEventType.COVERAGE)
                .eventDate(coverageDate)
                .breedingType(BreedingType.NATURAL)
                .build();
        return reproductiveEventRepository.save(coverage);
    }
}
