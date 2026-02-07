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
import com.devmaster.goatfarm.reproduction.api.dto.BreedingRequestDTO;
import com.devmaster.goatfarm.reproduction.api.dto.PregnancyCheckRequestDTO;
import com.devmaster.goatfarm.reproduction.api.dto.PregnancyConfirmRequestDTO;
import com.devmaster.goatfarm.reproduction.enums.BreedingType;
import com.devmaster.goatfarm.reproduction.enums.PregnancyCheckResult;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReproductionBreedingLockIntegrationTest {

    private static final String BLOCK_MESSAGE =
            "Não é permitido registrar nova cobertura: existe uma gestação ativa para esta cabra. " +
                    "Encerre/corrija a gestação atual (ex.: falso positivo/aborto) para liberar novas coberturas.";

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
        ownerGoat = goatRepository.save(ownerGoat);
    }

    @Test
    void shouldBlockBreedingWhilePregnancyActiveAndUnlockAfterNegative() throws Exception {
        String token = loginAndGetToken("owner@example.com", "password");

        LocalDate coverageDate = LocalDate.now().minusDays(80);
        createBreeding(token, coverageDate).andExpect(status().isCreated());

        LocalDate confirmDate = coverageDate.plusDays(60);
        PregnancyConfirmRequestDTO confirmRequest = PregnancyConfirmRequestDTO.builder()
                .checkDate(confirmDate)
                .checkResult(PregnancyCheckResult.POSITIVE)
                .notes("Confirmacao positiva")
                .build();

        mockMvc.perform(patch("/api/goatfarms/{farmId}/goats/{goatId}/reproduction/pregnancies/confirm",
                        ownerFarm.getId(), ownerGoat.getRegistrationNumber())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(confirmRequest))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        LocalDate blockedDate = coverageDate.plusDays(70);
        createBreeding(token, blockedDate)
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errors[0].message").value(BLOCK_MESSAGE));

        PregnancyCheckRequestDTO negativeRequest = PregnancyCheckRequestDTO.builder()
                .checkDate(LocalDate.now())
                .checkResult(PregnancyCheckResult.NEGATIVE)
                .notes("Falso positivo")
                .build();

        mockMvc.perform(post("/api/goatfarms/{farmId}/goats/{goatId}/reproduction/pregnancies/checks",
                        ownerFarm.getId(), ownerGoat.getRegistrationNumber())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(negativeRequest))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated());

        createBreeding(token, LocalDate.now()).andExpect(status().isCreated());
    }

    private ResultActions createBreeding(String token, LocalDate eventDate) throws Exception {
        BreedingRequestDTO request = BreedingRequestDTO.builder()
                .eventDate(eventDate)
                .breedingType(BreedingType.NATURAL)
                .notes("Cobertura")
                .build();

        return mockMvc.perform(post("/api/goatfarms/{farmId}/goats/{goatId}/reproduction/breeding",
                        ownerFarm.getId(), ownerGoat.getRegistrationNumber())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + token));
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
