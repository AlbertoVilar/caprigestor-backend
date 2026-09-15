package com.devmaster.goatfarm.health.api;

import com.devmaster.goatfarm.authority.persistence.entity.FarmOperator;
import com.devmaster.goatfarm.authority.persistence.entity.Role;
import com.devmaster.goatfarm.authority.persistence.entity.User;
import com.devmaster.goatfarm.authority.persistence.repository.FarmOperatorRepository;
import com.devmaster.goatfarm.authority.persistence.repository.RoleRepository;
import com.devmaster.goatfarm.authority.persistence.repository.UserRepository;
import com.devmaster.goatfarm.farm.persistence.entity.GoatFarm;
import com.devmaster.goatfarm.farm.persistence.repository.GoatFarmRepository;
import com.devmaster.goatfarm.goat.enums.Gender;
import com.devmaster.goatfarm.goat.enums.GoatStatus;
import com.devmaster.goatfarm.goat.persistence.entity.GoatEntity;
import com.devmaster.goatfarm.goat.persistence.repository.GoatRepository;
import com.devmaster.goatfarm.goatownership.domain.OwnershipEntryType;
import com.devmaster.goatfarm.goatownership.domain.OwnershipExitType;
import com.devmaster.goatfarm.goatownership.persistence.entity.GoatOwnershipPeriodEntity;
import com.devmaster.goatfarm.goatownership.persistence.repository.GoatOwnershipPeriodRepository;
import com.devmaster.goatfarm.health.api.dto.HealthEventCreateRequestDTO;
import com.devmaster.goatfarm.health.domain.enums.HealthEventType;
import com.devmaster.goatfarm.health.persistence.repository.HealthEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
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
import org.springframework.test.web.servlet.ResultActions;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class HealthOperationalAuthorizationIntegrationTest {

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
    private FarmOperatorRepository farmOperatorRepository;

    @Autowired
    private GoatOwnershipPeriodRepository ownershipPeriodRepository;

    @Autowired
    private HealthEventRepository healthEventRepository;

    private User admin;
    private User owner;
    private User linkedOperator;
    private GoatFarm managedFarm;
    private GoatFarm otherFarm;
    private GoatEntity managedGoat;

    @BeforeEach
    void setUp() {
        cleanDatabase();

        Role adminRole = roleRepository.save(new Role("ROLE_ADMIN", "Admin"));
        Role ownerRole = roleRepository.save(new Role("ROLE_FARM_OWNER", "Farm owner"));
        Role operatorRole = roleRepository.save(new Role("ROLE_OPERATOR", "Operator"));

        admin = createUser("admin-health@example.com", "11111111111", adminRole);
        owner = createUser("owner-health@example.com", "22222222222", ownerRole);
        linkedOperator = createUser("operator-health@example.com", "33333333333", operatorRole);
        User otherOwner = createUser("other-owner-health@example.com", "44444444444", ownerRole);

        managedFarm = createFarm("Managed health farm", owner);
        otherFarm = createFarm("Other health farm", otherOwner);
        managedGoat = createGoat(managedFarm, "HEALTH00001", "Managed health goat");

        FarmOperator farmOperator = new FarmOperator();
        farmOperator.setFarm(managedFarm);
        farmOperator.setUser(linkedOperator);
        farmOperatorRepository.save(farmOperator);
    }

    @AfterEach
    void tearDown() {
        cleanDatabase();
    }

    @Test
    void currentCanonicalOwnerCanCreateHealthEvent() throws Exception {
        String token = loginAndGetToken(owner.getEmail());

        postHealthEvent(token, managedFarm, managedGoat, LocalDate.now().plusDays(1))
                .andExpect(status().isCreated());

        assertThat(healthEventRepository.count()).isEqualTo(1);
    }

    @Test
    void formerOwnerCannotCreateAfterCanonicalTransfer() throws Exception {
        LocalDate transferDate = LocalDate.now().minusDays(2);
        transferOwnershipOnDate(managedGoat, managedFarm, otherFarm, transferDate, true);
        String token = loginAndGetToken(owner.getEmail());

        postHealthEvent(token, managedFarm, managedGoat, transferDate.minusDays(1))
                .andExpect(status().isForbidden());

        assertThat(healthEventRepository.count()).isZero();
    }

    @Test
    void projectionCannotGrantAuthorityWhenCanonicalOwnerIsAnotherFarm() throws Exception {
        LocalDate transferDate = LocalDate.now().minusDays(2);
        transferOwnershipOnDate(managedGoat, managedFarm, otherFarm, transferDate, true);
        String token = loginAndGetToken(owner.getEmail());

        postHealthEvent(token, managedFarm, managedGoat, LocalDate.now())
                .andExpect(status().isForbidden());

        assertThat(healthEventRepository.count()).isZero();
    }

    @Test
    void projectionDriftFailsClosedWhenCanonicalOwnerMatchesRequest() throws Exception {
        LocalDate transferDate = LocalDate.now().minusDays(2);
        transferOwnershipOnDate(managedGoat, managedFarm, otherFarm, transferDate, false);
        String token = loginAndGetToken(admin.getEmail());

        postHealthEvent(token, otherFarm, managedGoat, LocalDate.now())
                .andExpect(status().isForbidden());

        assertThat(healthEventRepository.count()).isZero();
    }

    @Test
    void linkedOperatorCanCreateHealthEventForCurrentFarm() throws Exception {
        String token = loginAndGetToken(linkedOperator.getEmail());

        postHealthEvent(token, managedFarm, managedGoat, LocalDate.now().plusDays(1))
                .andExpect(status().isCreated());
    }

    private ResultActions postHealthEvent(String token, GoatFarm farm, GoatEntity goat, LocalDate scheduledDate)
            throws Exception {
        return mockMvc.perform(post("/api/v1/goatfarms/" + farm.getId()
                        + "/goats/" + goat.getRegistrationNumber() + "/health-events")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(HealthEventCreateRequestDTO.builder()
                        .type(HealthEventType.VACINA)
                        .title("Vacina de teste")
                        .scheduledDate(scheduledDate)
                        .build())));
    }

    private String loginAndGetToken(String email) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"password\"}"))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("accessToken").asText();
    }

    private User createUser(String email, String cpf, Role role) {
        User user = new User();
        user.setName(email);
        user.setEmail(email);
        user.setCpf(cpf);
        user.setPassword(passwordEncoder.encode("password"));
        user.addRole(role);
        return userRepository.save(user);
    }

    private GoatFarm createFarm(String name, User farmOwner) {
        GoatFarm farm = new GoatFarm();
        farm.setName(name);
        farm.setUser(farmOwner);
        return goatFarmRepository.save(farm);
    }

    private GoatEntity createGoat(GoatFarm farm, String registrationNumber, String name) {
        GoatEntity goat = new GoatEntity();
        goat.setRegistrationNumber(registrationNumber);
        goat.setName(name);
        goat.setGender(Gender.FEMEA);
        goat.setBirthDate(LocalDate.now().minusYears(2));
        goat.setStatus(GoatStatus.ATIVO);
        goat.setFarm(farm);
        GoatEntity saved = goatRepository.save(goat);

        GoatOwnershipPeriodEntity ownership = new GoatOwnershipPeriodEntity();
        ownership.setGoatId(saved.getTechnicalId());
        ownership.setFarmId(farm.getId());
        ownership.setStartedAt(Instant.parse("2020-01-01T00:00:00Z"));
        ownership.setEntryType(OwnershipEntryType.MANUAL_IMPORT);
        ownership.setSource("HEALTH_TEST");
        ownershipPeriodRepository.save(ownership);
        return saved;
    }

    private void transferOwnershipOnDate(GoatEntity goat, GoatFarm source, GoatFarm target,
                                         LocalDate transferDate, boolean updateProjection) {
        GoatOwnershipPeriodEntity current = ownershipPeriodRepository
                .findByGoatIdAndEndedAtIsNull(goat.getTechnicalId())
                .orElseThrow();
        ZoneId zone = ZoneId.of("America/Sao_Paulo");
        Instant effectiveAt = transferDate.atStartOfDay(zone).toInstant().plusSeconds(14 * 60 * 60);
        current.setEndedAt(effectiveAt);
        current.setExitType(OwnershipExitType.TRANSFER_OUT);
        ownershipPeriodRepository.saveAndFlush(current);

        GoatOwnershipPeriodEntity targetPeriod = new GoatOwnershipPeriodEntity();
        targetPeriod.setGoatId(goat.getTechnicalId());
        targetPeriod.setFarmId(target.getId());
        targetPeriod.setStartedAt(effectiveAt);
        targetPeriod.setEntryType(OwnershipEntryType.TRANSFER_IN);
        targetPeriod.setSource("HEALTH_TEST_TRANSFER");
        ownershipPeriodRepository.saveAndFlush(targetPeriod);

        if (updateProjection) {
            goat.setFarm(target);
            goatRepository.saveAndFlush(goat);
        }
    }

    private void cleanDatabase() {
        healthEventRepository.deleteAll();
        ownershipPeriodRepository.deleteAll();
        farmOperatorRepository.deleteAll();
        goatRepository.deleteAll();
        goatFarmRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();
    }
}
