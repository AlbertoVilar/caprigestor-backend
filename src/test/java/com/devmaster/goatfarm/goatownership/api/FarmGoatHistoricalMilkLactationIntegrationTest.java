package com.devmaster.goatfarm.goatownership.api;

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
import com.devmaster.goatfarm.milk.enums.LactationStatus;
import com.devmaster.goatfarm.milk.enums.MilkProductionStatus;
import com.devmaster.goatfarm.milk.enums.MilkingShift;
import com.devmaster.goatfarm.milk.persistence.entity.LactationEntity;
import com.devmaster.goatfarm.milk.persistence.entity.MilkProductionEntity;
import com.devmaster.goatfarm.milk.persistence.repository.LactationRepository;
import com.devmaster.goatfarm.milk.persistence.repository.MilkProductionRepository;
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

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FarmGoatHistoricalMilkLactationIntegrationTest {

    private static final Instant T0 = Instant.parse("2024-01-01T00:00:00Z");
    private static final Instant T1 = Instant.parse("2024-07-01T00:00:00Z");
    private static final Instant T2 = Instant.parse("2024-12-01T00:00:00Z");

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
    @Autowired private LactationRepository lactationRepository;
    @Autowired private MilkProductionRepository milkProductionRepository;

    private User ownerA;
    private User ownerB;
    private User ownerC;

    private GoatFarm farmA;
    private GoatFarm farmB;
    private GoatFarm farmC;

    private GoatEntity goat1;
    private GoatEntity goatEmpty;
    private LactationEntity lac1;
    private MilkProductionEntity prodA;
    private MilkProductionEntity prodB;
    private MilkProductionEntity prodACanceled;

    @BeforeEach
    void setUp() {
        cleanDatabase();

        Role ownerRole = roleRepository.save(new Role("ROLE_FARM_OWNER", "Farm owner"));

        ownerA = createUser("owner-a@example.com", "22222222222", ownerRole);
        ownerB = createUser("owner-b@example.com", "33333333333", ownerRole);
        ownerC = createUser("owner-c@example.com", "44444444444", ownerRole);

        farmA = createFarm("Capril Farm A", "11111", ownerA);
        farmB = createFarm("Capril Farm B", "22222", ownerB);
        farmC = createFarm("Capril Farm C", "33333", ownerC);

        // goat1: Born at Farm A, transferred to B, deceased at B
        goat1 = new GoatEntity();
        goat1.setRegistrationNumber("1111100001");
        goat1.setName("Serena");
        goat1.setGender(Gender.FEMEA);
        goat1.setBreed(GoatBreed.SAANEN);
        goat1.setColor("Branca");
        goat1.setBirthDate(LocalDate.of(2023, 1, 15));
        goat1.setStatus(GoatStatus.FALECIDO);
        goat1.setCategory(Category.PA);
        goat1.setTod("11111");
        goat1.setToe("00001");
        goat1.setFarm(farmB);
        goat1.setUser(ownerB);
        goat1 = goatRepository.save(goat1);

        CreatorReferenceEntity creator1 = new CreatorReferenceEntity();
        creator1.setGoatId(goat1.getTechnicalId());
        creator1.setCreatorTod("11111");
        creator1.setCreatorFarmId(farmA.getId());
        creator1.setCreatorNameSnapshot("Capril Farm A");
        creator1.setSource(CreatorSource.BIRTH);
        creator1.setRecordedAt(T0);
        creatorReferenceRepository.save(creator1);

        // Ownership Period 1: Farm A
        GoatOwnershipPeriodEntity p1 = new GoatOwnershipPeriodEntity();
        p1.setGoatId(goat1.getTechnicalId());
        p1.setFarmId(farmA.getId());
        p1.setStartedAt(T0);
        p1.setEndedAt(T1);
        p1.setEntryType(OwnershipEntryType.BIRTH);
        p1.setExitType(OwnershipExitType.TRANSFER_OUT);
        p1.setSource("transfer to B");
        ownershipPeriodRepository.save(p1);

        // Ownership Period 2: Farm B
        GoatOwnershipPeriodEntity p2 = new GoatOwnershipPeriodEntity();
        p2.setGoatId(goat1.getTechnicalId());
        p2.setFarmId(farmB.getId());
        p2.setStartedAt(T1);
        p2.setEndedAt(T2);
        p2.setEntryType(OwnershipEntryType.TRANSFER_IN);
        p2.setExitType(OwnershipExitType.DEATH);
        p2.setSource("death at B");
        ownershipPeriodRepository.save(p2);

        // Lactation 1: started at Farm A on 2024-04-01, closed on 2024-11-01
        lac1 = LactationEntity.builder()
                .farmId(farmA.getId())
                .goatId(goat1.getRegistrationNumber())
                .goatTechnicalId(goat1.getTechnicalId())
                .status(LactationStatus.CLOSED)
                .startDate(LocalDate.of(2024, 4, 1))
                .endDate(LocalDate.of(2024, 11, 1))
                .dryAtPregnancyDays(90)
                .restDays(60)
                .build();
        lac1 = lactationRepository.save(lac1);

        // Production A (recorded by Farm A on 2024-05-01)
        prodA = MilkProductionEntity.builder()
                .farmId(farmA.getId())
                .goatId(goat1.getRegistrationNumber())
                .goatTechnicalId(goat1.getTechnicalId())
                .lactation(lac1)
                .date(LocalDate.of(2024, 5, 1))
                .shift(MilkingShift.MORNING)
                .volumeLiters(new BigDecimal("3.50"))
                .status(MilkProductionStatus.ACTIVE)
                .notes("Good morning production")
                .build();
        prodA = milkProductionRepository.save(prodA);

        // Production A CANCELED (recorded by Farm A on 2024-05-02)
        prodACanceled = MilkProductionEntity.builder()
                .farmId(farmA.getId())
                .goatId(goat1.getRegistrationNumber())
                .goatTechnicalId(goat1.getTechnicalId())
                .lactation(lac1)
                .date(LocalDate.of(2024, 5, 2))
                .shift(MilkingShift.AFTERNOON)
                .volumeLiters(new BigDecimal("1.00"))
                .status(MilkProductionStatus.CANCELED)
                .canceledReason("Teste cancelado")
                .canceledAt(LocalDateTime.of(2024, 5, 2, 17, 0))
                .build();
        prodACanceled = milkProductionRepository.save(prodACanceled);

        // Production B (recorded by Farm B on 2024-09-01 after transfer)
        prodB = MilkProductionEntity.builder()
                .farmId(farmB.getId())
                .goatId(goat1.getRegistrationNumber())
                .goatTechnicalId(goat1.getTechnicalId())
                .lactation(lac1)
                .date(LocalDate.of(2024, 9, 1))
                .shift(MilkingShift.MORNING)
                .volumeLiters(new BigDecimal("2.80"))
                .status(MilkProductionStatus.ACTIVE)
                .notes("Production at Farm B")
                .build();
        prodB = milkProductionRepository.save(prodB);

        // goatEmpty: Farm A active goat without any lactation or milk production
        goatEmpty = new GoatEntity();
        goatEmpty.setRegistrationNumber("1111100002");
        goatEmpty.setName("PÃƒÂ©rola");
        goatEmpty.setGender(Gender.FEMEA);
        goatEmpty.setBreed(GoatBreed.SAANEN);
        goatEmpty.setColor("Branca");
        goatEmpty.setBirthDate(LocalDate.of(2024, 1, 1));
        goatEmpty.setStatus(GoatStatus.ATIVO);
        goatEmpty.setCategory(Category.PA);
        goatEmpty.setTod("11111");
        goatEmpty.setToe("00002");
        goatEmpty.setFarm(farmA);
        goatEmpty.setUser(ownerA);
        goatEmpty = goatRepository.save(goatEmpty);

        GoatOwnershipPeriodEntity pEmpty = new GoatOwnershipPeriodEntity();
        pEmpty.setGoatId(goatEmpty.getTechnicalId());
        pEmpty.setFarmId(farmA.getId());
        pEmpty.setStartedAt(T0);
        pEmpty.setEndedAt(null);
        pEmpty.setEntryType(OwnershipEntryType.BIRTH);
        pEmpty.setSource("birth at A");
        ownershipPeriodRepository.save(pEmpty);
    }

    @AfterEach
    void tearDown() {
        cleanDatabase();
    }

    @Test
    @DisplayName("Case 1 & 5: Farm A reads dossier of goat transferred A -> B; sees lactation and only Farm A's productions")
    void farmAReadsHistoricalMilkLactation_seesItsOwnDataOnly() throws Exception {
        String tokenA = loginAndGetToken(ownerA.getEmail());

        mockMvc.perform(get("/api/v1/goatfarms/" + farmA.getId() + "/goat-registry/technical-" + goat1.getTechnicalId() + "/milk-lactation")
                        .header("Authorization", bearer(tokenA))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.goatId").value(goat1.getTechnicalId()))
                .andExpect(jsonPath("$.lactations", hasSize(1)))
                .andExpect(jsonPath("$.lactations[0].id").value(lac1.getId()))
                .andExpect(jsonPath("$.lactations[0].status").value("CLOSED"))
                .andExpect(jsonPath("$.lactations[0].active").value(false))
                .andExpect(jsonPath("$.milkProductions", hasSize(2)))
                .andExpect(jsonPath("$.milkProductions[*].id", containsInAnyOrder(prodA.getId().intValue(), prodACanceled.getId().intValue())))
                .andExpect(jsonPath("$.milkProductions[*].id", not(hasItem(prodB.getId().intValue()))));
    }

    @Test
    @DisplayName("Case 2 & 3: Farm B reads historical dossier; sees inherited lactation and only Farm B's productions")
    void farmBReadsHistoricalMilkLactation_seesInheritedLactationAndItsOwnDataOnly() throws Exception {
        String tokenB = loginAndGetToken(ownerB.getEmail());

        mockMvc.perform(get("/api/v1/goatfarms/" + farmB.getId() + "/goat-registry/technical-" + goat1.getTechnicalId() + "/milk-lactation")
                        .header("Authorization", bearer(tokenB))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.goatId").value(goat1.getTechnicalId()))
                .andExpect(jsonPath("$.lactations", hasSize(1)))
                .andExpect(jsonPath("$.lactations[0].id").value(lac1.getId()))
                .andExpect(jsonPath("$.milkProductions", hasSize(1)))
                .andExpect(jsonPath("$.milkProductions[0].id").value(prodB.getId().intValue()))
                .andExpect(jsonPath("$.milkProductions[*].id", not(hasItem(prodA.getId().intValue()))))
                .andExpect(jsonPath("$.milkProductions[*].id", not(hasItem(prodACanceled.getId().intValue()))));
    }

    @Test
    @DisplayName("Case 4: Unrelated Farm C receives 404 Not Found")
    void unrelatedFarmC_receives404() throws Exception {
        String tokenC = loginAndGetToken(ownerC.getEmail());

        mockMvc.perform(get("/api/v1/goatfarms/" + farmC.getId() + "/goat-registry/technical-" + goat1.getTechnicalId() + "/milk-lactation")
                        .header("Authorization", bearer(tokenC))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Case 6: Closed lactations and canceled milk productions are properly preserved and returned")
    void closedAndCanceledRecords_arePreserved() throws Exception {
        String tokenA = loginAndGetToken(ownerA.getEmail());

        mockMvc.perform(get("/api/v1/goatfarms/" + farmA.getId() + "/goat-registry/technical-" + goat1.getTechnicalId() + "/milk-lactation")
                        .header("Authorization", bearer(tokenA))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lactations[0].status").value("CLOSED"))
                .andExpect(jsonPath("$.lactations[0].active").value(false))
                .andExpect(jsonPath("$.milkProductions[?(@.status == 'CANCELED')].canceledReason").value("Teste cancelado"));
    }

    @Test
    @DisplayName("Case 7: Empty state returns HTTP 200 with empty arrays")
    void emptyState_returns200WithEmptyArrays() throws Exception {
        String tokenA = loginAndGetToken(ownerA.getEmail());

        mockMvc.perform(get("/api/v1/goatfarms/" + farmA.getId() + "/goat-registry/technical-" + goatEmpty.getTechnicalId() + "/milk-lactation")
                        .header("Authorization", bearer(tokenA))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.goatId").value(goatEmpty.getTechnicalId()))
                .andExpect(jsonPath("$.lactations", hasSize(0)))
                .andExpect(jsonPath("$.milkProductions", hasSize(0)));
    }

    @Test
    @DisplayName("Structural identity wins: different goat with matching RG does not leak records")
    void differentGoatWithMatchingRg_doesNotLeakIntoDossier() throws Exception {
        String tokenA = loginAndGetToken(ownerA.getEmail());

        LactationEntity foreignLac = LactationEntity.builder()
                .farmId(farmA.getId())
                .goatId(goat1.getRegistrationNumber())
                .goatTechnicalId(9999L)
                .status(LactationStatus.ACTIVE)
                .startDate(LocalDate.of(2024, 6, 1))
                .build();
        foreignLac = lactationRepository.save(foreignLac);

        MilkProductionEntity foreignProd = MilkProductionEntity.builder()
                .farmId(farmA.getId())
                .goatId(goat1.getRegistrationNumber())
                .goatTechnicalId(9999L)
                .lactation(foreignLac)
                .date(LocalDate.of(2024, 6, 2))
                .shift(MilkingShift.MORNING)
                .volumeLiters(new BigDecimal("9.99"))
                .status(MilkProductionStatus.ACTIVE)
                .build();
        foreignProd = milkProductionRepository.save(foreignProd);

        mockMvc.perform(get("/api/v1/goatfarms/" + farmA.getId() + "/goat-registry/technical-" + goat1.getTechnicalId() + "/milk-lactation")
                        .header("Authorization", bearer(tokenA))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lactations[*].id", not(hasItem(foreignLac.getId().intValue()))))
                .andExpect(jsonPath("$.milkProductions[*].id", not(hasItem(foreignProd.getId().intValue()))));
    }

    @Test
    @DisplayName("Registry RG identifier is rejected with HTTP 400 Bad Request (technical- prefix required)")
    void rgIdentifier_rejectedWith400() throws Exception {
        String tokenA = loginAndGetToken(ownerA.getEmail());

        mockMvc.perform(get("/api/v1/goatfarms/" + farmA.getId() + "/goat-registry/" + goat1.getRegistrationNumber() + "/milk-lactation")
                        .header("Authorization", bearer(tokenA)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Malformed route identifier is rejected with HTTP 400 Bad Request")
    void malformedRouteIdentifier_returns400() throws Exception {
        String tokenA = loginAndGetToken(ownerA.getEmail());

        // Bare numeric
        mockMvc.perform(get("/api/v1/goatfarms/" + farmA.getId() + "/goat-registry/" + goat1.getTechnicalId() + "/milk-lactation")
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
        milkProductionRepository.deleteAll();
        lactationRepository.deleteAll();
        farmOperatorRepository.deleteAll();
        ownershipPeriodRepository.deleteAll();
        creatorReferenceRepository.deleteAll();
        goatRepository.deleteAll();
        goatFarmRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();
    }
}