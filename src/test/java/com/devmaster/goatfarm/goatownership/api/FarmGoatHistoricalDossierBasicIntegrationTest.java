package com.devmaster.goatfarm.goatownership.api;

import com.devmaster.goatfarm.authority.persistence.entity.FarmOperator;
import com.devmaster.goatfarm.authority.persistence.entity.Role;
import com.devmaster.goatfarm.authority.persistence.entity.User;
import com.devmaster.goatfarm.authority.persistence.repository.FarmOperatorRepository;
import com.devmaster.goatfarm.authority.persistence.repository.RoleRepository;
import com.devmaster.goatfarm.authority.persistence.repository.UserRepository;
import com.devmaster.goatfarm.farm.persistence.entity.GoatFarm;
import com.devmaster.goatfarm.farm.persistence.repository.GoatFarmRepository;
import com.devmaster.goatfarm.goat.enums.Category;
import com.devmaster.goatfarm.goat.enums.Gender;
import com.devmaster.goatfarm.goat.enums.GoatBreed;
import com.devmaster.goatfarm.goat.enums.GoatStatus;
import com.devmaster.goatfarm.goat.persistence.entity.GoatEntity;
import com.devmaster.goatfarm.goat.persistence.repository.GoatRepository;
import com.devmaster.goatfarm.goatownership.domain.CreatorSource;
import com.devmaster.goatfarm.goatownership.domain.OwnershipEntryType;
import com.devmaster.goatfarm.goatownership.domain.OwnershipExitType;
import com.devmaster.goatfarm.goatownership.persistence.entity.CreatorReferenceEntity;
import com.devmaster.goatfarm.goatownership.persistence.entity.GoatOwnershipPeriodEntity;
import com.devmaster.goatfarm.goatownership.persistence.repository.CreatorReferenceRepository;
import com.devmaster.goatfarm.goatownership.persistence.repository.GoatOwnershipPeriodRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FarmGoatHistoricalDossierBasicIntegrationTest {

    private static final Instant T = Instant.parse("2026-01-01T00:00:00Z");

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private GoatFarmRepository goatFarmRepository;
    @Autowired private GoatRepository goatRepository;
    @Autowired private FarmOperatorRepository farmOperatorRepository;
    @Autowired private CreatorReferenceRepository creatorReferenceRepository;
    @Autowired private GoatOwnershipPeriodRepository ownershipPeriodRepository;

    private User admin;
    private User ownerA;
    private User ownerB;
    private User ownerC;
    private User operatorA;
    private User unlinkedOperator;

    private GoatFarm farmA;
    private GoatFarm farmB;
    private GoatFarm farmC;

    private GoatEntity goat;

    @BeforeEach
    void setUp() {
        cleanDatabase();

        Role adminRole = roleRepository.save(new Role("ROLE_ADMIN", "Admin"));
        Role ownerRole = roleRepository.save(new Role("ROLE_FARM_OWNER", "Farm owner"));
        Role operatorRole = roleRepository.save(new Role("ROLE_OPERATOR", "Operator"));

        admin = createUser("admin-dossier@example.com", "11111111111", adminRole);
        ownerA = createUser("owner-a@example.com", "22222222222", ownerRole);
        ownerB = createUser("owner-b@example.com", "33333333333", ownerRole);
        ownerC = createUser("owner-c@example.com", "44444444444", ownerRole);
        operatorA = createUser("operator-a@example.com", "55555555555", operatorRole);
        unlinkedOperator = createUser("unlinked-operator@example.com", "66666666666", operatorRole);

        farmA = createFarm("Capril Farm A", "11111", ownerA);
        farmB = createFarm("Capril Farm B", "22222", ownerB);
        farmC = createFarm("Capril Farm C", "33333", ownerC);

        FarmOperator linkA = new FarmOperator();
        linkA.setFarm(farmA);
        linkA.setUser(operatorA);
        farmOperatorRepository.save(linkA);

        // Setup goat originally born in Farm A
        goat = new GoatEntity();
        goat.setRegistrationNumber("1111100001");
        goat.setName("Serena");
        goat.setGender(Gender.FEMEA);
        goat.setBreed(GoatBreed.SAANEN);
        goat.setColor("Branca");
        goat.setBirthDate(LocalDate.of(2024, 3, 15));
        goat.setStatus(GoatStatus.FALECIDO);
        goat.setCategory(Category.PA);
        goat.setTod("11111");
        goat.setToe("00001");
        goat.setFarm(farmB); // transitional projected farm points to Farm B
        goat.setUser(ownerB);
        goat = goatRepository.save(goat);

        // CreatorReference: Farm A created this goat
        CreatorReferenceEntity creator = new CreatorReferenceEntity();
        creator.setGoatId(goat.getTechnicalId());
        creator.setCreatorTod("11111");
        creator.setCreatorFarmId(farmA.getId());
        creator.setCreatorNameSnapshot("Capril Farm A");
        creator.setSource(CreatorSource.BIRTH);
        creator.setRecordedAt(T);
        creatorReferenceRepository.save(creator);

        // Period 1: Farm A ownership, ended by TRANSFER_OUT
        GoatOwnershipPeriodEntity p1 = new GoatOwnershipPeriodEntity();
        p1.setGoatId(goat.getTechnicalId());
        p1.setFarmId(farmA.getId());
        p1.setStartedAt(T);
        p1.setEndedAt(T.plusSeconds(3600));
        p1.setEntryType(OwnershipEntryType.BIRTH);
        p1.setExitType(OwnershipExitType.TRANSFER_OUT);
        p1.setSource("transfer to B");
        ownershipPeriodRepository.save(p1);

        // Period 2: Farm B ownership, ended by DEATH at B
        GoatOwnershipPeriodEntity p2 = new GoatOwnershipPeriodEntity();
        p2.setGoatId(goat.getTechnicalId());
        p2.setFarmId(farmB.getId());
        p2.setStartedAt(T.plusSeconds(3600));
        p2.setEndedAt(T.plusSeconds(7200));
        p2.setEntryType(OwnershipEntryType.TRANSFER_IN);
        p2.setExitType(OwnershipExitType.DEATH);
        p2.setSource("death at B");
        ownershipPeriodRepository.save(p2);
    }

    @AfterEach
    void tearDown() {
        cleanDatabase();
    }

    @Test
    @DisplayName("Critical acceptance case: Farm A reads dossier of goat transferred A -> B then deceased at B")
    void criticalCase_farmAReadsTransferredAndDeceasedGoat() throws Exception {
        String tokenA = loginAndGetToken(ownerA.getEmail());

        mockMvc.perform(get("/api/v1/goatfarms/" + farmA.getId() + "/goat-registry/technical-" + goat.getTechnicalId())
                        .header("Authorization", bearer(tokenA))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.goatId").value(goat.getTechnicalId()))
                .andExpect(jsonPath("$.registrationNumber").value("1111100001"))
                .andExpect(jsonPath("$.name").value("Serena"))
                .andExpect(jsonPath("$.globalStatus").value("FALECIDO"))
                .andExpect(jsonPath("$.gender").value("FEMEA"))
                .andExpect(jsonPath("$.breed").value("SAANEN"))
                .andExpect(jsonPath("$.color").value("Branca"))
                .andExpect(jsonPath("$.birthDate").value("2024-03-15"))
                .andExpect(jsonPath("$.tod").value("11111"))
                .andExpect(jsonPath("$.toe").value("00001"))
                .andExpect(jsonPath("$.creatorFarmId").value(farmA.getId()))
                .andExpect(jsonPath("$.creatorNameSnapshot").value("Capril Farm A"))
                .andExpect(jsonPath("$.roles", containsInAnyOrder("CREATOR", "FORMER_OWNER")))
                .andExpect(jsonPath("$.disposition").value("TRANSFERRED"))
                .andExpect(jsonPath("$.currentOwnerFarmId").value(nullValue()));
    }

    @Test
    @DisplayName("Farm B reads same goat and gets FORMER_OWNER / DECEASED with currentOwnerFarmId null")
    void farmBReadsDeceasedGoat() throws Exception {
        String tokenB = loginAndGetToken(ownerB.getEmail());

        mockMvc.perform(get("/api/v1/goatfarms/" + farmB.getId() + "/goat-registry/technical-" + goat.getTechnicalId())
                        .header("Authorization", bearer(tokenB))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles", containsInAnyOrder("FORMER_OWNER")))
                .andExpect(jsonPath("$.disposition").value("DECEASED"))
                .andExpect(jsonPath("$.currentOwnerFarmId").value(nullValue()));
    }

    @Test
    @DisplayName("Unrelated farm C reads same goat and receives 404 Not Found")
    void unrelatedFarmC_receives404() throws Exception {
        String tokenC = loginAndGetToken(ownerC.getEmail());

        mockMvc.perform(get("/api/v1/goatfarms/" + farmC.getId() + "/goat-registry/technical-" + goat.getTechnicalId())
                        .header("Authorization", bearer(tokenC))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Authorization matrix: ADMIN, FARM_OWNER, and linked OPERATOR succeed; unlinked and unrelated denied")
    void authorizationMatrix_canManageFarm() throws Exception {
        String adminToken = loginAndGetToken(admin.getEmail());
        String ownerAToken = loginAndGetToken(ownerA.getEmail());
        String operatorAToken = loginAndGetToken(operatorA.getEmail());
        String unlinkedToken = loginAndGetToken(unlinkedOperator.getEmail());
        String ownerBToken = loginAndGetToken(ownerB.getEmail());

        String path = "/api/v1/goatfarms/" + farmA.getId() + "/goat-registry/technical-" + goat.getTechnicalId();

        // Admin -> 200
        mockMvc.perform(get(path).header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk());

        // Farm Owner A -> 200
        mockMvc.perform(get(path).header("Authorization", bearer(ownerAToken)))
                .andExpect(status().isOk());

        // Linked Operator A -> 200
        mockMvc.perform(get(path).header("Authorization", bearer(operatorAToken)))
                .andExpect(status().isOk());

        // Unlinked operator -> 403
        mockMvc.perform(get(path).header("Authorization", bearer(unlinkedToken)))
                .andExpect(status().isForbidden());

        // Owner B trying to access Farm A registry -> 403
        mockMvc.perform(get(path).header("Authorization", bearer(ownerBToken)))
                .andExpect(status().isForbidden());

        // Unauthenticated -> 401
        mockMvc.perform(get(path))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Invalid route identifiers are rejected with HTTP 400 Bad Request")
    void invalidRouteIdentifiers_rejectedWith400() throws Exception {
        String tokenA = loginAndGetToken(ownerA.getEmail());

        // Bare numeric
        mockMvc.perform(get("/api/v1/goatfarms/" + farmA.getId() + "/goat-registry/" + goat.getTechnicalId())
                        .header("Authorization", bearer(tokenA)))
                .andExpect(status().isBadRequest());

        // RG token
        mockMvc.perform(get("/api/v1/goatfarms/" + farmA.getId() + "/goat-registry/RG-HD-001")
                        .header("Authorization", bearer(tokenA)))
                .andExpect(status().isBadRequest());

        // Incomplete technical prefix
        mockMvc.perform(get("/api/v1/goatfarms/" + farmA.getId() + "/goat-registry/technical-")
                        .header("Authorization", bearer(tokenA)))
                .andExpect(status().isBadRequest());

        // technical-0
        mockMvc.perform(get("/api/v1/goatfarms/" + farmA.getId() + "/goat-registry/technical-0")
                        .header("Authorization", bearer(tokenA)))
                .andExpect(status().isBadRequest());
    }

    private String loginAndGetToken(String email) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"password\"}"))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("accessToken").asText();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private User createUser(String email, String cpf, Role role) {
        User user = new User();
        user.setName("User " + email);
        user.setEmail(email);
        user.setCpf(cpf);
        user.setPassword(passwordEncoder.encode("password"));
        user.addRole(role);
        return userRepository.save(user);
    }

    private GoatFarm createFarm(String name, String tod, User owner) {
        GoatFarm farm = new GoatFarm();
        farm.setName(name);
        farm.setTod(tod);
        farm.setUser(owner);
        return goatFarmRepository.save(farm);
    }

    private void cleanDatabase() {
        farmOperatorRepository.deleteAll();
        ownershipPeriodRepository.deleteAll();
        creatorReferenceRepository.deleteAll();
        goatRepository.deleteAll();
        goatFarmRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();
    }
}
