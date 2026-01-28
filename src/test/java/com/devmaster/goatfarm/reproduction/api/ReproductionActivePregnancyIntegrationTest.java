package com.devmaster.goatfarm.reproduction.api;

import com.devmaster.goatfarm.authority.model.entity.Role;
import com.devmaster.goatfarm.authority.model.entity.User;
import com.devmaster.goatfarm.authority.model.repository.UserRepository;
import com.devmaster.goatfarm.authority.repository.RoleRepository;
import com.devmaster.goatfarm.farm.model.entity.GoatFarm;
import com.devmaster.goatfarm.farm.model.repository.GoatFarmRepository;
import com.devmaster.goatfarm.goat.enums.Gender;
import com.devmaster.goatfarm.goat.enums.GoatStatus;
import com.devmaster.goatfarm.goat.model.entity.Goat;
import com.devmaster.goatfarm.goat.model.repository.GoatRepository;
import com.devmaster.goatfarm.infrastructure.adapters.out.persistence.PregnancyPersistenceAdapter;
import com.devmaster.goatfarm.reproduction.api.dto.PregnancyConfirmRequestDTO;
import com.devmaster.goatfarm.reproduction.enums.BreedingType;
import com.devmaster.goatfarm.reproduction.enums.PregnancyCheckResult;
import com.devmaster.goatfarm.reproduction.enums.PregnancyStatus;
import com.devmaster.goatfarm.reproduction.enums.ReproductiveEventType;
import com.devmaster.goatfarm.reproduction.model.entity.Pregnancy;
import com.devmaster.goatfarm.reproduction.model.entity.ReproductiveEvent;
import com.devmaster.goatfarm.reproduction.model.repository.PregnancyRepository;
import com.devmaster.goatfarm.reproduction.model.repository.ReproductiveEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReproductionActivePregnancyIntegrationTest {

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

    @SpyBean
    private PregnancyPersistenceAdapter pregnancyPersistenceAdapter;

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

        // Manually create the index to simulate the production constraint.
        // For H2 compatibility in this test, we use a standard unique index on (farm_id, goat_id, status).
        // This is sufficient to catch the duplicate ACTIVE pregnancy scenario in this test.
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
    void shouldReturn409_WhenConfirmingPregnancyForGoatWithActivePregnancy_UsingDbConstraint() throws Exception {
        String token = loginAndGetToken("owner@example.com", "password");

        // 1. Create initial coverage (breeding)
        ReproductiveEvent coverage = ReproductiveEvent.builder()
                .farmId(ownerFarm.getId())
                .goatId(ownerGoat.getRegistrationNumber())
                .eventType(ReproductiveEventType.COVERAGE)
                .eventDate(LocalDate.now().minusDays(30))
                .breedingType(BreedingType.NATURAL)
                .build();
        reproductiveEventRepository.save(coverage);

        // 2. Create an EXISTING Active Pregnancy
        Pregnancy activePregnancy = Pregnancy.builder()
                .farmId(ownerFarm.getId())
                .goatId(ownerGoat.getRegistrationNumber())
                .status(PregnancyStatus.ACTIVE)
                .breedingDate(coverage.getEventDate())
                .confirmDate(LocalDate.now().minusDays(10))
                .build();
        pregnancyRepository.save(activePregnancy);

        // 3. Prepare request to confirm another pregnancy
        PregnancyConfirmRequestDTO request = new PregnancyConfirmRequestDTO();
        request.setCheckDate(LocalDate.now());
        request.setCheckResult(PregnancyCheckResult.POSITIVE);
        request.setNotes("Tentativa de duplicidade");

        // 4. Force Service check to PASS by mocking the return of findActive... to empty
        // This forces the flow to proceed to 'save' and hit the DB Constraint
        doReturn(Optional.empty())
                .when(pregnancyPersistenceAdapter).findActiveByFarmIdAndGoatId(eq(ownerFarm.getId()), eq(ownerGoat.getRegistrationNumber()));

        // 5. Perform Request
        mockMvc.perform(patch("/api/goatfarms/{farmId}/goats/{goatId}/reproduction/pregnancies/confirm",
                        ownerFarm.getId(), ownerGoat.getRegistrationNumber())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict()) // 409
                .andExpect(jsonPath("$.errors[0].message").value("Já existe uma gestação ativa para esta cabra"));
    }
}
