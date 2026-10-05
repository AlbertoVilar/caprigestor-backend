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
import com.devmaster.goatfarm.reproduction.enums.*;
import com.devmaster.goatfarm.reproduction.persistence.entity.PregnancyEntity;
import com.devmaster.goatfarm.reproduction.persistence.entity.ReproductiveEventEntity;
import com.devmaster.goatfarm.reproduction.persistence.repository.PregnancyRepository;
import com.devmaster.goatfarm.reproduction.persistence.repository.ReproductiveEventRepository;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FarmGoatHistoricalReproductionIntegrationTest {

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
    @Autowired private PregnancyRepository pregnancyRepository;
    @Autowired private ReproductiveEventRepository reproductiveEventRepository;

    private User admin;
    private User ownerA;
    private User ownerB;
    private User ownerC;

    private GoatFarm farmA;
    private GoatFarm farmB;
    private GoatFarm farmC;

    private GoatEntity goat1;
    private GoatEntity goatEmpty;

    @BeforeEach
    void setUp() {
        cleanDatabase();

        Role adminRole = roleRepository.save(new Role("ROLE_ADMIN", "Administrator"));
        Role ownerRole = roleRepository.save(new Role("ROLE_FARM_OWNER", "Farm owner"));

        admin = createUser("admin@example.com", "11111111111", adminRole);
        ownerA = createUser("owner-a@example.com", "22222222222", ownerRole);
        ownerB = createUser("owner-b@example.com", "33333333333", ownerRole);
        ownerC = createUser("owner-c@example.com", "44444444444", ownerRole);

        farmA = createFarm("Capril Farm A", "11111", ownerA);
        farmB = createFarm("Capril Farm B", "22222", ownerB);
        farmC = createFarm("Capril Farm C", "33333", ownerC);

        // goat1: Born at Farm A, transferred to Farm B, deceased at Farm B
        goat1 = new GoatEntity();
        goat1.setRegistrationNumber("1111100001");
        goat1.setName("Estrela");
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

        // Period 1: Farm A
        GoatOwnershipPeriodEntity p1 = new GoatOwnershipPeriodEntity();
        p1.setGoatId(goat1.getTechnicalId());
        p1.setFarmId(farmA.getId());
        p1.setStartedAt(T0);
        p1.setEndedAt(T1);
        p1.setEntryType(OwnershipEntryType.BIRTH);
        p1.setExitType(OwnershipExitType.TRANSFER_OUT);
        p1.setSource("transfer to B");
        ownershipPeriodRepository.save(p1);

        // Period 2: Farm B
        GoatOwnershipPeriodEntity p2 = new GoatOwnershipPeriodEntity();
        p2.setGoatId(goat1.getTechnicalId());
        p2.setFarmId(farmB.getId());
        p2.setStartedAt(T1);
        p2.setEndedAt(T2);
        p2.setEntryType(OwnershipEntryType.TRANSFER_IN);
        p2.setExitType(OwnershipExitType.DEATH);
        p2.setSource("death at B");
        ownershipPeriodRepository.save(p2);

        // goatEmpty: Farm A active goat without any reproduction data
        goatEmpty = new GoatEntity();
        goatEmpty.setRegistrationNumber("1111100002");
        goatEmpty.setName("Luar");
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
    @DisplayName("Matrix A & B: Farm A owns coverage fact; Farm B check does NOT leak to Farm A")
    void scenarioAandB_farmAOwnFacts_farmBEventDoesNotLeak() throws Exception {
        // Farm A coverage
        reproductiveEventRepository.save(ReproductiveEventEntity.builder()
                .farmId(farmA.getId())
                .goatTechnicalId(goat1.getTechnicalId())
                .goatId(goat1.getRegistrationNumber())
                .eventType(ReproductiveEventType.COVERAGE)
                .eventDate(LocalDate.of(2024, 3, 1))
                .breedingType(BreedingType.NATURAL)
                .breederRef("BOD-01")
                .notes("Cobertura A")
                .build());

        // Farm B diagnosis check
        reproductiveEventRepository.save(ReproductiveEventEntity.builder()
                .farmId(farmB.getId())
                .goatTechnicalId(goat1.getTechnicalId())
                .goatId(goat1.getRegistrationNumber())
                .eventType(ReproductiveEventType.PREGNANCY_CHECK)
                .eventDate(LocalDate.of(2024, 8, 1))
                .checkResult(PregnancyCheckResult.POSITIVE)
                .notes("Check B")
                .build());

        String tokenA = loginAndGetToken(ownerA.getEmail());

        mockMvc.perform(get("/api/v1/goatfarms/" + farmA.getId() + "/goat-registry/technical-" + goat1.getTechnicalId() + "/reproduction")
                        .header("Authorization", bearer(tokenA))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.goatId").value(goat1.getTechnicalId()))
                .andExpect(jsonPath("$.events", hasSize(1)))
                .andExpect(jsonPath("$.events[0].farmId").value(farmA.getId()))
                .andExpect(jsonPath("$.events[0].eventType").value("COVERAGE"))
                .andExpect(jsonPath("$.events[0].breederRef").value("BOD-01"))
                .andExpect(jsonPath("$.processes", hasSize(0)));
    }

    @Test
    @DisplayName("Matrix C: A coverage -> B Pregnancy; A does not see B pregnancy, B sees pregnancy with foreign coverage context")
    void scenarioC_coverageLinkedToForeignPregnancy() throws Exception {
        ReproductiveEventEntity covA = reproductiveEventRepository.save(ReproductiveEventEntity.builder()
                .farmId(farmA.getId())
                .goatTechnicalId(goat1.getTechnicalId())
                .goatId(goat1.getRegistrationNumber())
                .eventType(ReproductiveEventType.COVERAGE)
                .eventDate(LocalDate.of(2024, 3, 1))
                .breedingType(BreedingType.NATURAL)
                .breederRef("BOD-01")
                .build());

        PregnancyEntity pregB = pregnancyRepository.save(PregnancyEntity.builder()
                .farmId(farmB.getId())
                .goatTechnicalId(goat1.getTechnicalId())
                .goatId(goat1.getRegistrationNumber())
                .status(PregnancyStatus.ACTIVE)
                .breedingDate(LocalDate.of(2024, 3, 1))
                .confirmDate(LocalDate.of(2024, 8, 1))
                .expectedDueDate(LocalDate.of(2024, 7, 29))
                .coverageEventId(covA.getId())
                .notes("Pregnancy confirmed at B")
                .build());

        String tokenA = loginAndGetToken(ownerA.getEmail());
        String tokenB = loginAndGetToken(ownerB.getEmail());

        // Response for Farm A: A coverage visible; B pregnancy NOT visible
        mockMvc.perform(get("/api/v1/goatfarms/" + farmA.getId() + "/goat-registry/technical-" + goat1.getTechnicalId() + "/reproduction")
                        .header("Authorization", bearer(tokenA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.events", hasSize(1)))
                .andExpect(jsonPath("$.events[0].id").value(covA.getId()))
                .andExpect(jsonPath("$.processes", hasSize(0)));

        // Response for Farm B: B pregnancy visible; foreign coverage context present; covA NOT in B event stream
        mockMvc.perform(get("/api/v1/goatfarms/" + farmB.getId() + "/goat-registry/technical-" + goat1.getTechnicalId() + "/reproduction")
                        .header("Authorization", bearer(tokenB)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.events", hasSize(0))) // A coverage must NOT appear in B event stream
                .andExpect(jsonPath("$.processes", hasSize(1)))
                .andExpect(jsonPath("$.processes[0].pregnancyId").value(pregB.getId()))
                .andExpect(jsonPath("$.processes[0].processOriginFarmId").value(farmB.getId()))
                .andExpect(jsonPath("$.processes[0].coverageEventId").value(covA.getId()))
                .andExpect(jsonPath("$.processes[0].foreignCoverageContext.coverageEventId").value(covA.getId()))
                .andExpect(jsonPath("$.processes[0].foreignCoverageContext.originFarmId").value(farmA.getId()))
                .andExpect(jsonPath("$.processes[0].foreignCoverageContext.coverageDate").value("2024-03-01"))
                .andExpect(jsonPath("$.processes[0].foreignCoverageContext.breedingType").value("NATURAL"))
                .andExpect(jsonPath("$.processes[0].foreignCoverageContext.breederRef").value("BOD-01"));
    }

    @Test
    @DisplayName("Matrix D: A Pregnancy -> B Closure; A sees process without B closure, B sees process with own closure")
    void scenarioD_aPregnancyClosedByB() throws Exception {
        PregnancyEntity pregA = pregnancyRepository.save(PregnancyEntity.builder()
                .farmId(farmA.getId())
                .goatTechnicalId(goat1.getTechnicalId())
                .goatId(goat1.getRegistrationNumber())
                .status(PregnancyStatus.CLOSED)
                .breedingDate(LocalDate.of(2024, 2, 1))
                .confirmDate(LocalDate.of(2024, 4, 1))
                .expectedDueDate(LocalDate.of(2024, 7, 1))
                .closedAt(LocalDate.of(2024, 7, 5))
                .closeReason(PregnancyCloseReason.BIRTH)
                .notes("Parto gemelar na Fazenda B")
                .build());

        ReproductiveEventEntity closeEventB = reproductiveEventRepository.save(ReproductiveEventEntity.builder()
                .farmId(farmB.getId())
                .goatTechnicalId(goat1.getTechnicalId())
                .goatId(goat1.getRegistrationNumber())
                .eventType(ReproductiveEventType.PREGNANCY_CLOSE)
                .eventDate(LocalDate.of(2024, 7, 5))
                .pregnancyId(pregA.getId())
                .notes("Parto registrado por B")
                .build());

        String tokenA = loginAndGetToken(ownerA.getEmail());
        String tokenB = loginAndGetToken(ownerB.getEmail());

        // Response for Farm A: Process visible, but B closure fields and status NOT leaked
        mockMvc.perform(get("/api/v1/goatfarms/" + farmA.getId() + "/goat-registry/technical-" + goat1.getTechnicalId() + "/reproduction")
                        .header("Authorization", bearer(tokenA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.events", hasSize(0))) // B closure event absent
                .andExpect(jsonPath("$.processes", hasSize(1)))
                .andExpect(jsonPath("$.processes[0].pregnancyId").value(pregA.getId()))
                .andExpect(jsonPath("$.processes[0].processOriginFarmId").value(farmA.getId()))
                .andExpect(jsonPath("$.processes[0].breedingDate").value("2024-02-01"))
                .andExpect(jsonPath("$.processes[0].confirmDate").value("2024-04-01"))
                .andExpect(jsonPath("$.processes[0].closedAt").doesNotExist()) // Redacted
                .andExpect(jsonPath("$.processes[0].closeReason").doesNotExist()) // Redacted
                .andExpect(jsonPath("$.processes[0].status").doesNotExist()); // Redacted (never leaked CLOSED, never fabricated ACTIVE)

        // Response for Farm B: B sees closure event, sees process context with own closure, A's private confirmDate redacted
        mockMvc.perform(get("/api/v1/goatfarms/" + farmB.getId() + "/goat-registry/technical-" + goat1.getTechnicalId() + "/reproduction")
                        .header("Authorization", bearer(tokenB)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.events", hasSize(1)))
                .andExpect(jsonPath("$.events[0].id").value(closeEventB.getId()))
                .andExpect(jsonPath("$.processes", hasSize(1)))
                .andExpect(jsonPath("$.processes[0].pregnancyId").value(pregA.getId()))
                .andExpect(jsonPath("$.processes[0].processOriginFarmId").value(farmA.getId()))
                .andExpect(jsonPath("$.processes[0].breedingDate").value("2024-02-01"))
                .andExpect(jsonPath("$.processes[0].confirmDate").doesNotExist()) // A's private confirm date redacted
                .andExpect(jsonPath("$.processes[0].expectedDueDate").doesNotExist()) // A's private expected due date redacted
                .andExpect(jsonPath("$.processes[0].status").value("CLOSED"))
                .andExpect(jsonPath("$.processes[0].closedAt").value("2024-07-05"))
                .andExpect(jsonPath("$.processes[0].closeReason").value("BIRTH"));
    }

    @Test
    @DisplayName("Matrix E: Missing closure event fails closed (no closedAt, closeReason, or status)")
    void scenarioE_missingClosureEvent_failsClosed() throws Exception {
        PregnancyEntity pregA = pregnancyRepository.save(PregnancyEntity.builder()
                .farmId(farmA.getId())
                .goatTechnicalId(goat1.getTechnicalId())
                .goatId(goat1.getRegistrationNumber())
                .status(PregnancyStatus.CLOSED)
                .breedingDate(LocalDate.of(2024, 2, 1))
                .confirmDate(LocalDate.of(2024, 4, 1))
                .closedAt(LocalDate.of(2024, 7, 5))
                .closeReason(PregnancyCloseReason.BIRTH)
                .build());

        String tokenA = loginAndGetToken(ownerA.getEmail());

        mockMvc.perform(get("/api/v1/goatfarms/" + farmA.getId() + "/goat-registry/technical-" + goat1.getTechnicalId() + "/reproduction")
                        .header("Authorization", bearer(tokenA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.processes", hasSize(1)))
                .andExpect(jsonPath("$.processes[0].closedAt").doesNotExist())
                .andExpect(jsonPath("$.processes[0].closeReason").doesNotExist())
                .andExpect(jsonPath("$.processes[0].status").doesNotExist());
    }

    @Test
    @DisplayName("Matrix F: Ambiguous closure events fail closed (no closedAt, closeReason, or status)")
    void scenarioF_ambiguousClosureEvents_failsClosed() throws Exception {
        PregnancyEntity pregA = pregnancyRepository.save(PregnancyEntity.builder()
                .farmId(farmA.getId())
                .goatTechnicalId(goat1.getTechnicalId())
                .goatId(goat1.getRegistrationNumber())
                .status(PregnancyStatus.CLOSED)
                .breedingDate(LocalDate.of(2024, 2, 1))
                .confirmDate(LocalDate.of(2024, 4, 1))
                .closedAt(LocalDate.of(2024, 7, 5))
                .closeReason(PregnancyCloseReason.BIRTH)
                .build());

        // Two closure events for the same pregnancy
        reproductiveEventRepository.save(ReproductiveEventEntity.builder()
                .farmId(farmA.getId())
                .goatTechnicalId(goat1.getTechnicalId())
                .goatId(goat1.getRegistrationNumber())
                .eventType(ReproductiveEventType.PREGNANCY_CLOSE)
                .eventDate(LocalDate.of(2024, 7, 5))
                .pregnancyId(pregA.getId())
                .build());

        reproductiveEventRepository.save(ReproductiveEventEntity.builder()
                .farmId(farmA.getId())
                .goatTechnicalId(goat1.getTechnicalId())
                .goatId(goat1.getRegistrationNumber())
                .eventType(ReproductiveEventType.PREGNANCY_CLOSE)
                .eventDate(LocalDate.of(2024, 7, 5))
                .pregnancyId(pregA.getId())
                .build());

        String tokenA = loginAndGetToken(ownerA.getEmail());

        mockMvc.perform(get("/api/v1/goatfarms/" + farmA.getId() + "/goat-registry/technical-" + goat1.getTechnicalId() + "/reproduction")
                        .header("Authorization", bearer(tokenA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.processes", hasSize(1)))
                .andExpect(jsonPath("$.processes[0].closedAt").doesNotExist())
                .andExpect(jsonPath("$.processes[0].closeReason").doesNotExist())
                .andExpect(jsonPath("$.processes[0].status").doesNotExist());
    }

    @Test
    @DisplayName("Matrix G: Identity collision - row with different non-null goatTechnicalId and matching RG must NOT appear")
    void scenarioG_identityCollision_differentTechnicalIdWithMatchingRgNotReturned() throws Exception {
        // Event with goatTechnicalId = 9999L but matching goat1's RG
        reproductiveEventRepository.save(ReproductiveEventEntity.builder()
                .farmId(farmA.getId())
                .goatTechnicalId(9999L)
                .goatId(goat1.getRegistrationNumber())
                .eventType(ReproductiveEventType.COVERAGE)
                .eventDate(LocalDate.of(2024, 1, 1))
                .breedingType(BreedingType.NATURAL)
                .build());

        String tokenA = loginAndGetToken(ownerA.getEmail());

        mockMvc.perform(get("/api/v1/goatfarms/" + farmA.getId() + "/goat-registry/technical-" + goat1.getTechnicalId() + "/reproduction")
                        .header("Authorization", bearer(tokenA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.events", hasSize(0)));
    }

    @Test
    @DisplayName("Matrix H: Legacy fallback - historical row with goatTechnicalId NULL and matching legacy RG appears")
    void scenarioH_legacyFallback_nullTechnicalIdWithMatchingRgAppears() throws Exception {
        reproductiveEventRepository.save(ReproductiveEventEntity.builder()
                .farmId(farmA.getId())
                .goatTechnicalId(null)
                .goatId(goat1.getRegistrationNumber())
                .eventType(ReproductiveEventType.COVERAGE)
                .eventDate(LocalDate.of(2024, 1, 1))
                .breedingType(BreedingType.NATURAL)
                .build());

        String tokenA = loginAndGetToken(ownerA.getEmail());

        mockMvc.perform(get("/api/v1/goatfarms/" + farmA.getId() + "/goat-registry/technical-" + goat1.getTechnicalId() + "/reproduction")
                        .header("Authorization", bearer(tokenA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.events", hasSize(1)))
                .andExpect(jsonPath("$.events[0].farmId").value(farmA.getId()))
                .andExpect(jsonPath("$.events[0].eventType").value("COVERAGE"));
    }

    @Test
    @DisplayName("Matrix I: Empty dossier - Registry member with no reproduction data returns 200 with empty lists")
    void scenarioI_emptyDossier_returns200WithEmptyLists() throws Exception {
        String tokenA = loginAndGetToken(ownerA.getEmail());

        mockMvc.perform(get("/api/v1/goatfarms/" + farmA.getId() + "/goat-registry/technical-" + goatEmpty.getTechnicalId() + "/reproduction")
                        .header("Authorization", bearer(tokenA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.goatId").value(goatEmpty.getTechnicalId()))
                .andExpect(jsonPath("$.processes", hasSize(0)))
                .andExpect(jsonPath("$.events", hasSize(0)));
    }

    @Test
    @DisplayName("Section 11: Strict invariant - EVERY event returned has event.farmId == requested farmId")
    void section11_strictEventStreamAssertion() throws Exception {
        reproductiveEventRepository.save(ReproductiveEventEntity.builder()
                .farmId(farmA.getId())
                .goatTechnicalId(goat1.getTechnicalId())
                .goatId(goat1.getRegistrationNumber())
                .eventType(ReproductiveEventType.COVERAGE)
                .eventDate(LocalDate.of(2024, 2, 1))
                .breedingType(BreedingType.NATURAL)
                .build());

        reproductiveEventRepository.save(ReproductiveEventEntity.builder()
                .farmId(farmB.getId())
                .goatTechnicalId(goat1.getTechnicalId())
                .goatId(goat1.getRegistrationNumber())
                .eventType(ReproductiveEventType.PREGNANCY_CHECK)
                .eventDate(LocalDate.of(2024, 3, 1))
                .checkResult(PregnancyCheckResult.NEGATIVE)
                .build());

        reproductiveEventRepository.save(ReproductiveEventEntity.builder()
                .farmId(farmC.getId())
                .goatTechnicalId(goat1.getTechnicalId())
                .goatId(goat1.getRegistrationNumber())
                .eventType(ReproductiveEventType.WEANING)
                .eventDate(LocalDate.of(2024, 4, 1))
                .build());

        String tokenA = loginAndGetToken(ownerA.getEmail());

        mockMvc.perform(get("/api/v1/goatfarms/" + farmA.getId() + "/goat-registry/technical-" + goat1.getTechnicalId() + "/reproduction")
                        .header("Authorization", bearer(tokenA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.events", hasSize(1)))
                .andExpect(jsonPath("$.events[*].farmId", everyItem(equalTo(farmA.getId().intValue()))));
    }

    @Test
    @DisplayName("Section 12: Deterministic ordering in API response")
    void section12_deterministicOrderingInApiResponse() throws Exception {
        reproductiveEventRepository.save(ReproductiveEventEntity.builder()
                .farmId(farmA.getId()).goatTechnicalId(goat1.getTechnicalId()).goatId(goat1.getRegistrationNumber())
                .eventType(ReproductiveEventType.COVERAGE).eventDate(LocalDate.of(2024, 1, 10)).build());
        reproductiveEventRepository.save(ReproductiveEventEntity.builder()
                .farmId(farmA.getId()).goatTechnicalId(goat1.getTechnicalId()).goatId(goat1.getRegistrationNumber())
                .eventType(ReproductiveEventType.COVERAGE).eventDate(LocalDate.of(2024, 3, 15)).build());
        reproductiveEventRepository.save(ReproductiveEventEntity.builder()
                .farmId(farmA.getId()).goatTechnicalId(goat1.getTechnicalId()).goatId(goat1.getRegistrationNumber())
                .eventType(ReproductiveEventType.WEANING).eventDate(LocalDate.of(2024, 3, 15)).build());

        String tokenA = loginAndGetToken(ownerA.getEmail());

        mockMvc.perform(get("/api/v1/goatfarms/" + farmA.getId() + "/goat-registry/technical-" + goat1.getTechnicalId() + "/reproduction")
                        .header("Authorization", bearer(tokenA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.events", hasSize(3)))
                .andExpect(jsonPath("$.events[0].eventDate").value("2024-03-15"))
                .andExpect(jsonPath("$.events[1].eventDate").value("2024-03-15"))
                .andExpect(jsonPath("$.events[2].eventDate").value("2024-01-10"));
    }

    @Test
    @DisplayName("Section 9 Security: ADMIN allowed according to Registry contract")
    void security_adminAllowed() throws Exception {
        String tokenAdmin = loginAndGetToken(admin.getEmail());

        mockMvc.perform(get("/api/v1/goatfarms/" + farmA.getId() + "/goat-registry/technical-" + goat1.getTechnicalId() + "/reproduction")
                        .header("Authorization", bearer(tokenAdmin)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Section 9 Security: Unrelated owner denied with HTTP 403 Forbidden")
    void security_unrelatedOwnerDeniedWith403() throws Exception {
        String tokenC = loginAndGetToken(ownerC.getEmail());

        mockMvc.perform(get("/api/v1/goatfarms/" + farmA.getId() + "/goat-registry/technical-" + goat1.getTechnicalId() + "/reproduction")
                        .header("Authorization", bearer(tokenC)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Section 9 Security: Unauthenticated request rejected with HTTP 401 Unauthorized")
    void security_unauthenticatedReturns401() throws Exception {
        mockMvc.perform(get("/api/v1/goatfarms/" + farmA.getId() + "/goat-registry/technical-" + goat1.getTechnicalId() + "/reproduction"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Section 9 Security: Non-member goat returns HTTP 404 Not Found")
    void security_nonMemberReturns404() throws Exception {
        // goat1 never belonged to Farm C
        String tokenC = loginAndGetToken(ownerC.getEmail());

        mockMvc.perform(get("/api/v1/goatfarms/" + farmC.getId() + "/goat-registry/technical-" + goat1.getTechnicalId() + "/reproduction")
                        .header("Authorization", bearer(tokenC)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Section 9 Security: Invalid technical token formats rejected with HTTP 400 Bad Request")
    void security_invalidTokens_return400() throws Exception {
        String tokenA = loginAndGetToken(ownerA.getEmail());
        String baseUrl = "/api/v1/goatfarms/" + farmA.getId() + "/goat-registry/";

        // 42 (bare number)
        mockMvc.perform(get(baseUrl + "42/reproduction").header("Authorization", bearer(tokenA)))
                .andExpect(status().isBadRequest());

        // RG-123 (registration number)
        mockMvc.perform(get(baseUrl + "RG-123/reproduction").header("Authorization", bearer(tokenA)))
                .andExpect(status().isBadRequest());

        // technical- (empty id)
        mockMvc.perform(get(baseUrl + "technical-/reproduction").header("Authorization", bearer(tokenA)))
                .andExpect(status().isBadRequest());

        // technical-abc (non-numeric id)
        mockMvc.perform(get(baseUrl + "technical-abc/reproduction").header("Authorization", bearer(tokenA)))
                .andExpect(status().isBadRequest());

        // technical-0 (non-positive)
        mockMvc.perform(get(baseUrl + "technical-0/reproduction").header("Authorization", bearer(tokenA)))
                .andExpect(status().isBadRequest());

        // technical--1 (negative)
        mockMvc.perform(get(baseUrl + "technical--1/reproduction").header("Authorization", bearer(tokenA)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Section 6.A: standalone COVERAGE exposes breedingType and breederRef")
    void standaloneFacts_coverageExposesBreedingTypeAndBreederRef() throws Exception {
        reproductiveEventRepository.save(ReproductiveEventEntity.builder()
                .farmId(farmA.getId())
                .goatTechnicalId(goat1.getTechnicalId())
                .goatId(goat1.getRegistrationNumber())
                .eventType(ReproductiveEventType.COVERAGE)
                .eventDate(LocalDate.of(2024, 4, 10))
                .breedingType(BreedingType.NATURAL)
                .breederRef("REPRODUTOR-TOP")
                .notes("Cobertura natural no piquete")
                .build());

        String tokenA = loginAndGetToken(ownerA.getEmail());

        mockMvc.perform(get("/api/v1/goatfarms/" + farmA.getId() + "/goat-registry/technical-" + goat1.getTechnicalId() + "/reproduction")
                        .header("Authorization", bearer(tokenA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.events", hasSize(1)))
                .andExpect(jsonPath("$.events[0].eventType").value("COVERAGE"))
                .andExpect(jsonPath("$.events[0].breedingType").value("NATURAL"))
                .andExpect(jsonPath("$.events[0].breederRef").value("REPRODUTOR-TOP"))
                .andExpect(jsonPath("$.events[0].pregnancyId").doesNotExist());
    }

    @Test
    @DisplayName("Section 6.B: COVERAGE_CORRECTION exposes relatedEventId and correctedEventDate")
    void standaloneFacts_coverageCorrectionExposesRelatedEventAndCorrectedDate() throws Exception {
        ReproductiveEventEntity cov = reproductiveEventRepository.save(ReproductiveEventEntity.builder()
                .farmId(farmA.getId())
                .goatTechnicalId(goat1.getTechnicalId())
                .goatId(goat1.getRegistrationNumber())
                .eventType(ReproductiveEventType.COVERAGE)
                .eventDate(LocalDate.of(2024, 4, 10))
                .breedingType(BreedingType.NATURAL)
                .build());

        reproductiveEventRepository.save(ReproductiveEventEntity.builder()
                .farmId(farmA.getId())
                .goatTechnicalId(goat1.getTechnicalId())
                .goatId(goat1.getRegistrationNumber())
                .eventType(ReproductiveEventType.COVERAGE_CORRECTION)
                .eventDate(LocalDate.of(2024, 4, 12))
                .relatedEventId(cov.getId())
                .correctedEventDate(LocalDate.of(2024, 4, 9))
                .notes("Correção de data de monta")
                .build());

        String tokenA = loginAndGetToken(ownerA.getEmail());

        mockMvc.perform(get("/api/v1/goatfarms/" + farmA.getId() + "/goat-registry/technical-" + goat1.getTechnicalId() + "/reproduction")
                        .header("Authorization", bearer(tokenA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.events", hasSize(2)))
                .andExpect(jsonPath("$.events[0].eventType").value("COVERAGE_CORRECTION"))
                .andExpect(jsonPath("$.events[0].relatedEventId").value(cov.getId()))
                .andExpect(jsonPath("$.events[0].correctedEventDate").value("2024-04-09"));
    }

    @Test
    @DisplayName("Section 6.C & D: Standalone PREGNANCY_CHECK with NEGATIVE and POSITIVE results and null pregnancyId")
    void standaloneFacts_pregnancyCheckWithNullPregnancyId() throws Exception {
        // Negative check
        reproductiveEventRepository.save(ReproductiveEventEntity.builder()
                .farmId(farmA.getId())
                .goatTechnicalId(goat1.getTechnicalId())
                .goatId(goat1.getRegistrationNumber())
                .eventType(ReproductiveEventType.PREGNANCY_CHECK)
                .eventDate(LocalDate.of(2024, 5, 1))
                .checkResult(PregnancyCheckResult.NEGATIVE)
                .notes("DG negativo")
                .build());

        // Positive check without pregnancyId (independent fact)
        reproductiveEventRepository.save(ReproductiveEventEntity.builder()
                .farmId(farmA.getId())
                .goatTechnicalId(goat1.getTechnicalId())
                .goatId(goat1.getRegistrationNumber())
                .eventType(ReproductiveEventType.PREGNANCY_CHECK)
                .eventDate(LocalDate.of(2024, 6, 1))
                .checkResult(PregnancyCheckResult.POSITIVE)
                .notes("DG positivo avulso")
                .build());

        String tokenA = loginAndGetToken(ownerA.getEmail());

        mockMvc.perform(get("/api/v1/goatfarms/" + farmA.getId() + "/goat-registry/technical-" + goat1.getTechnicalId() + "/reproduction")
                        .header("Authorization", bearer(tokenA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.events", hasSize(2)))
                .andExpect(jsonPath("$.events[0].eventDate").value("2024-06-01"))
                .andExpect(jsonPath("$.events[0].checkResult").value("POSITIVE"))
                .andExpect(jsonPath("$.events[0].pregnancyId").doesNotExist())
                .andExpect(jsonPath("$.events[1].eventDate").value("2024-05-01"))
                .andExpect(jsonPath("$.events[1].checkResult").value("NEGATIVE"))
                .andExpect(jsonPath("$.events[1].pregnancyId").doesNotExist())
                .andExpect(jsonPath("$.processes", hasSize(0)));
    }

    @Test
    @DisplayName("Section 6.E: PREGNANCY_CLOSE has pregnancyId but closeReason MUST NOT exist on event JSON")
    void standaloneFacts_pregnancyCloseHasNoCloseReasonOnEventJson() throws Exception {
        reproductiveEventRepository.save(ReproductiveEventEntity.builder()
                .farmId(farmA.getId())
                .goatTechnicalId(goat1.getTechnicalId())
                .goatId(goat1.getRegistrationNumber())
                .eventType(ReproductiveEventType.PREGNANCY_CLOSE)
                .eventDate(LocalDate.of(2024, 6, 20))
                .pregnancyId(999L)
                .notes("Parto registrado")
                .build());

        String tokenA = loginAndGetToken(ownerA.getEmail());

        mockMvc.perform(get("/api/v1/goatfarms/" + farmA.getId() + "/goat-registry/technical-" + goat1.getTechnicalId() + "/reproduction")
                        .header("Authorization", bearer(tokenA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.events", hasSize(1)))
                .andExpect(jsonPath("$.events[0].eventType").value("PREGNANCY_CLOSE"))
                .andExpect(jsonPath("$.events[0].pregnancyId").value(999L))
                .andExpect(jsonPath("$.events[0].closeReason").doesNotExist());
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
        reproductiveEventRepository.deleteAll();
        pregnancyRepository.deleteAll();
        farmOperatorRepository.deleteAll();
        ownershipPeriodRepository.deleteAll();
        creatorReferenceRepository.deleteAll();
        goatRepository.deleteAll();
        goatFarmRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();
    }
}
