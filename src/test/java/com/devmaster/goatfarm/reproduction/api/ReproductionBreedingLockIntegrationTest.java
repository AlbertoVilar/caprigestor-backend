package com.devmaster.goatfarm.reproduction.api;

import com.devmaster.goatfarm.authority.persistence.entity.Role;
import com.devmaster.goatfarm.authority.persistence.entity.User;
import com.devmaster.goatfarm.authority.persistence.repository.FarmOperatorRepository;
import com.devmaster.goatfarm.authority.persistence.repository.RoleRepository;
import com.devmaster.goatfarm.authority.persistence.repository.UserRepository;
import com.devmaster.goatfarm.events.persistence.repository.EventRepository;
import com.devmaster.goatfarm.farm.persistence.entity.GoatFarm;
import com.devmaster.goatfarm.farm.persistence.repository.GoatFarmRepository;
import com.devmaster.goatfarm.goat.enums.Gender;
import com.devmaster.goatfarm.goat.enums.GoatStatus;
import com.devmaster.goatfarm.goat.persistence.entity.Goat;
import com.devmaster.goatfarm.goat.persistence.repository.GoatRepository;
import com.devmaster.goatfarm.reproduction.api.dto.BreedingRequestDTO;
import com.devmaster.goatfarm.reproduction.api.dto.PregnancyConfirmRequestDTO;
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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReproductionBreedingLockIntegrationTest {

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

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private FarmOperatorRepository farmOperatorRepository;

    private User ownerUser;
    private GoatFarm ownerFarm;
    private Goat ownerGoat;

    @BeforeEach
    void setUp() {
        pregnancyRepository.deleteAll();
        reproductiveEventRepository.deleteAll();
        eventRepository.deleteAll();
        farmOperatorRepository.deleteAll();
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
        ownerGoat = goatRepository.save(ownerGoat);
    }

    @Test
    void shouldBlockBreedingWhenCoverageAlreadyRegisteredForSameDay() throws Exception {
        String token = loginAndGetToken("owner@example.com", "password");

        LocalDate coverageDate = LocalDate.now().minusDays(10);
        createBreeding(token, coverageDate).andExpect(status().isCreated());
        createBreeding(token, coverageDate)
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errors[0].message").value(containsString("cobertura registrada para esta cabra hoje")));
    }

    @Test
    void shouldBlockBreedingWhenActivePregnancyExists() throws Exception {
        String token = loginAndGetToken("owner@example.com", "password");

        LocalDate firstCoverageDate = LocalDate.now().minusDays(90);
        createBreeding(token, firstCoverageDate).andExpect(status().isCreated());

        PregnancyConfirmRequestDTO confirmRequest = PregnancyConfirmRequestDTO.builder()
                .checkDate(firstCoverageDate.plusDays(60))
                .checkResult(PregnancyCheckResult.POSITIVE)
                .notes("Confirmacao positiva")
                .build();

        mockMvc.perform(patch("/api/v1/goatfarms/{farmId}/goats/{goatId}/reproduction/pregnancies/confirm",
                        ownerFarm.getId(), ownerGoat.getRegistrationNumber())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(confirmRequest))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        LocalDate secondCoverageDate = firstCoverageDate.plusDays(10);
        createBreeding(token, secondCoverageDate)
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errors[0].message").value(containsString("gestacao ativa")));

        List<ReproductiveEvent> events = reproductiveEventRepository.findAll();
        long coverageCount = events.stream()
                .filter(event -> event.getEventType() == ReproductiveEventType.COVERAGE)
                .count();

        if (coverageCount != 1) {
            throw new AssertionError("Nao deve existir segunda cobertura com gestacao ativa.");
        }
    }

    private ResultActions createBreeding(String token, LocalDate eventDate) throws Exception {
        BreedingRequestDTO request = BreedingRequestDTO.builder()
                .eventDate(eventDate)
                .breedingType(BreedingType.NATURAL)
                .notes("Cobertura")
                .build();

        return mockMvc.perform(post("/api/v1/goatfarms/{farmId}/goats/{goatId}/reproduction/breeding",
                        ownerFarm.getId(), ownerGoat.getRegistrationNumber())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + token));
    }

    private String loginAndGetToken(String email, String password) throws Exception {
        String loginPayload = "{\"email\":\"" + email + "\", \"password\":\"" + password + "\"}";
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        return objectMapper.readTree(response).get("accessToken").asText();
    }
}

