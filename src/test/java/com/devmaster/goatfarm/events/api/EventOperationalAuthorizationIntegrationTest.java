package com.devmaster.goatfarm.events.api;

import com.devmaster.goatfarm.authority.persistence.entity.FarmOperator;
import com.devmaster.goatfarm.authority.persistence.entity.Role;
import com.devmaster.goatfarm.authority.persistence.entity.User;
import com.devmaster.goatfarm.authority.persistence.repository.FarmOperatorRepository;
import com.devmaster.goatfarm.authority.persistence.repository.RoleRepository;
import com.devmaster.goatfarm.authority.persistence.repository.UserRepository;
import com.devmaster.goatfarm.events.enums.EventType;
import com.devmaster.goatfarm.events.persistence.entity.Event;
import com.devmaster.goatfarm.events.persistence.repository.EventRepository;
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

import java.time.LocalDate;
import java.time.Instant;
import java.time.ZoneId;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EventOperationalAuthorizationIntegrationTest {

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
    private EventRepository eventRepository;

    @Autowired
    private GoatOwnershipPeriodRepository ownershipPeriodRepository;

    private static final ZoneId DOMAIN_ZONE = ZoneId.of("America/Sao_Paulo");

    private LocalDate domainToday() {
        return LocalDate.now(DOMAIN_ZONE);
    }

    private User admin;
    private User owner;
    private User linkedOperator;
    private User unlinkedOperator;
    private GoatFarm managedFarm;
    private GoatEntity managedGoat;
    private GoatFarm otherFarm;
    private GoatEntity otherGoat;
    private Event otherFarmEvent;

    @BeforeEach
    void setUp() {
        cleanDatabase();

        Role adminRole = roleRepository.save(new Role("ROLE_ADMIN", "Admin"));
        Role ownerRole = roleRepository.save(new Role("ROLE_FARM_OWNER", "Farm owner"));
        Role operatorRole = roleRepository.save(new Role("ROLE_OPERATOR", "Operator"));

        admin = createUser("admin-events@example.com", "10101010101", adminRole);
        owner = createUser("owner-events@example.com", "20202020202", ownerRole);
        linkedOperator = createUser("linked-operator-events@example.com", "30303030303", operatorRole);
        unlinkedOperator = createUser("unlinked-operator-events@example.com", "40404040404", operatorRole);
        User otherOwner = createUser("other-owner-events@example.com", "50505050505", ownerRole);

        managedFarm = createFarm("Managed events farm", owner);
        otherFarm = createFarm("Other events farm", otherOwner);
        managedGoat = createGoat(managedFarm, "EVENT00001", "Managed goat");
        otherGoat = createGoat(otherFarm, "EVENT00002", "Other goat");

        FarmOperator farmOperator = new FarmOperator();
        farmOperator.setFarm(managedFarm);
        farmOperator.setUser(linkedOperator);
        farmOperatorRepository.save(farmOperator);

        otherFarmEvent = createPersistedEvent(otherGoat, "Event on the other farm");
    }

    @AfterEach
    void tearDown() {
        cleanDatabase();
    }

    @Test
    void adminAndFarmOwnerCanCreateReadUpdateAndDeleteEvents() throws Exception {
        assertAdministrativeEventLifecycle(loginAndGetToken(admin.getEmail()), "Admin event");
        assertAdministrativeEventLifecycle(loginAndGetToken(owner.getEmail()), "Owner event");
    }

    @Test
    void linkedOperatorCanCreateAndReadOperationalEventsForItsFarm() throws Exception {
        String token = loginAndGetToken(linkedOperator.getEmail());
        long eventId = createEvent(token, managedFarm, managedGoat, "Operational event");

        mockMvc.perform(get(eventPath(managedFarm, managedGoat) + "/{eventId}", eventId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk());
        mockMvc.perform(get(eventPath(managedFarm, managedGoat)).header("Authorization", bearer(token)))
                .andExpect(status().isOk());
        mockMvc.perform(get(eventPath(managedFarm, managedGoat) + "/filter")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk());
    }

    @Test
    void unlinkedOperatorIsForbiddenFromOperationalEventEndpoints() throws Exception {
        String token = loginAndGetToken(unlinkedOperator.getEmail());

        mockMvc.perform(post(eventPath(managedFarm, managedGoat))
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventPayload(managedGoat, "Denied event")))
                .andExpect(status().isForbidden());
        mockMvc.perform(get(eventPath(managedFarm, managedGoat) + "/{eventId}", otherFarmEvent.getId())
                        .header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get(eventPath(managedFarm, managedGoat)).header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get(eventPath(managedFarm, managedGoat) + "/filter")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());
    }

    @Test
    void linkedOperatorCannotAccessEventsOfAnotherFarmEvenWithManagedFarmInPath() throws Exception {
        String token = loginAndGetToken(linkedOperator.getEmail());
        String forgedPath = eventPath(managedFarm, otherGoat);

        mockMvc.perform(post(forgedPath)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventPayload(otherGoat, "Cross farm event")))
                .andExpect(status().isForbidden());
        mockMvc.perform(get(forgedPath + "/{eventId}", otherFarmEvent.getId())
                        .header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get(forgedPath).header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get(forgedPath + "/filter").header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());
    }

    @Test
    void anonymousUserIsUnauthorizedForOperationalEventEndpoints() throws Exception {
        mockMvc.perform(post(eventPath(managedFarm, managedGoat))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventPayload(managedGoat, "Anonymous event")))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get(eventPath(managedFarm, managedGoat) + "/{eventId}", otherFarmEvent.getId()))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get(eventPath(managedFarm, managedGoat)))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get(eventPath(managedFarm, managedGoat) + "/filter"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void linkedOperatorCannotUpdateOrDeleteEventHistory() throws Exception {
        String operatorToken = loginAndGetToken(linkedOperator.getEmail());
        String ownerToken = loginAndGetToken(owner.getEmail());
        long eventId = createEvent(ownerToken, managedFarm, managedGoat, "Protected history");

        mockMvc.perform(put(eventPath(managedFarm, managedGoat) + "/{eventId}", eventId)
                        .header("Authorization", bearer(operatorToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventPayload(managedGoat, "Unauthorized update")))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete(eventPath(managedFarm, managedGoat) + "/{eventId}", eventId)
                        .header("Authorization", bearer(operatorToken)))
                .andExpect(status().isForbidden());
    }

    @Test
    void futureEventDateIsRejectedBeforePersistence() throws Exception {
        String token = loginAndGetToken(owner.getEmail());

        mockMvc.perform(post(eventPath(managedFarm, managedGoat))
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventPayload(managedGoat, "Future event", domainToday().plusDays(1))))
                .andExpect(status().isBadRequest());

        org.assertj.core.api.Assertions.assertThat(eventRepository.count()).isEqualTo(1);
    }

    @Test
    void formerOwnerCannotCreateEventAfterCanonicalTransfer() throws Exception {
        LocalDate transferDate = domainToday().minusDays(2);
        transferOwnershipOnDate(managedGoat, managedFarm, otherFarm, transferDate, true);
        String token = loginAndGetToken(owner.getEmail());

        mockMvc.perform(post(eventPath(managedFarm, managedGoat))
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventPayload(managedGoat, "Former owner event", transferDate.minusDays(1))))
                .andExpect(status().isForbidden());
    }

    @Test
    void currentOwnerCannotCreateEventInPreviousOwnersPeriod() throws Exception {
        LocalDate transferDate = domainToday().minusDays(2);
        transferOwnershipOnDate(managedGoat, managedFarm, otherFarm, transferDate, true);
        String token = loginAndGetToken(admin.getEmail());

        mockMvc.perform(post(eventPath(otherFarm, managedGoat))
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventPayload(managedGoat, "Previous owner event", transferDate.minusDays(1))))
                .andExpect(status().isForbidden());
    }

    @Test
    void currentOwnerCanCreateEventForAnUnambiguousCurrentDate() throws Exception {
        transferOwnership(managedGoat, managedFarm, otherFarm, true);
        String token = loginAndGetToken(admin.getEmail());

        mockMvc.perform(post(eventPath(otherFarm, managedGoat))
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventPayload(managedGoat, "Current owner event", domainToday())))
                .andExpect(status().isCreated());
    }

    @Test
    void transferDayIsRejectedForDateOnlyEvent() throws Exception {
        transferOwnershipOnDate(managedGoat, managedFarm, otherFarm, domainToday().minusDays(2), false);
        String token = loginAndGetToken(admin.getEmail());

        mockMvc.perform(post(eventPath(otherFarm, managedGoat))
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventPayload(managedGoat, "Transfer day event", domainToday().minusDays(2))))
                .andExpect(status().isForbidden());
    }

    @Test
    void projectionDriftFailsClosedEvenWhenCanonicalOwnerMatchesRequest() throws Exception {
        transferOwnershipOnDate(managedGoat, managedFarm, otherFarm, domainToday().minusDays(2), false);
        String token = loginAndGetToken(admin.getEmail());

        mockMvc.perform(post(eventPath(otherFarm, managedGoat))
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventPayload(managedGoat, "Drifted projection event", domainToday())))
                .andExpect(status().isForbidden());
    }

    private void assertAdministrativeEventLifecycle(String token, String description) throws Exception {
        long eventId = createEvent(token, managedFarm, managedGoat, description);

        mockMvc.perform(get(eventPath(managedFarm, managedGoat) + "/{eventId}", eventId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk());
        mockMvc.perform(get(eventPath(managedFarm, managedGoat)).header("Authorization", bearer(token)))
                .andExpect(status().isOk());
        mockMvc.perform(get(eventPath(managedFarm, managedGoat) + "/filter")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk());
        mockMvc.perform(put(eventPath(managedFarm, managedGoat) + "/{eventId}", eventId)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventPayload(managedGoat, description + " updated")))
                .andExpect(status().isOk());
        mockMvc.perform(delete(eventPath(managedFarm, managedGoat) + "/{eventId}", eventId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isNoContent());
    }

    private long createEvent(String token, GoatFarm farm, GoatEntity goat, String description) throws Exception {
        MvcResult result = mockMvc.perform(post(eventPath(farm, goat))
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventPayload(goat, description)))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
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
        goat.setBirthDate(domainToday().minusYears(2));
        goat.setStatus(GoatStatus.ATIVO);
        goat.setFarm(farm);
        GoatEntity saved = goatRepository.save(goat);
        GoatOwnershipPeriodEntity ownership = new GoatOwnershipPeriodEntity();
        ownership.setGoatId(saved.getTechnicalId());
        ownership.setFarmId(farm.getId());
        ownership.setStartedAt(Instant.parse("2020-01-01T00:00:00Z"));
        ownership.setEntryType(OwnershipEntryType.MANUAL_IMPORT);
        ownership.setSource("EVENT_TEST");
        ownershipPeriodRepository.save(ownership);
        return saved;
    }

    private Event createPersistedEvent(GoatEntity goat, String description) {
        Event event = new Event();
        event.setGoat(goat);
        event.setEventType(EventType.VACINACAO);
        event.setDate(domainToday().minusDays(1));
        event.setDescription(description);
        event.setLocation("Farm");
        event.setVeterinarian("Veterinarian");
        event.setOutcome("Completed");
        return eventRepository.save(event);
    }

    private String loginAndGetToken(String email) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"password\"}"))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("accessToken").asText();
    }

    private String eventPath(GoatFarm farm, GoatEntity goat) {
        return "/api/v1/goatfarms/" + farm.getId() + "/goats/" + goat.getRegistrationNumber() + "/events";
    }

    private String eventPayload(GoatEntity goat, String description) {
        return eventPayload(goat, description, domainToday().minusDays(1));
    }

    private String eventPayload(GoatEntity goat, String description, LocalDate date) {
        return String.format(
                "{\"goatId\":\"%s\",\"eventType\":\"VACINACAO\",\"date\":\"%s\","
                        + "\"description\":\"%s\",\"location\":\"Farm\","
                        + "\"veterinarian\":\"Veterinarian\",\"outcome\":\"Completed\"}",
                goat.getRegistrationNumber(), date, description);
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private void transferOwnership(GoatEntity goat, GoatFarm source, GoatFarm target, boolean updateProjection) {
        transferOwnershipOnDate(goat, source, target, domainToday().minusDays(2), updateProjection);
    }

    private void transferOwnershipOnDate(GoatEntity goat, GoatFarm source, GoatFarm target,
                                         LocalDate transferDate, boolean updateProjection) {
        GoatOwnershipPeriodEntity current = ownershipPeriodRepository
                .findByGoatIdAndEndedAtIsNull(goat.getTechnicalId())
                .orElseThrow();
        Instant effectiveAt = transferDate.atStartOfDay(DOMAIN_ZONE).toInstant().plusSeconds(14 * 60 * 60);
        current.setEndedAt(effectiveAt);
        current.setExitType(OwnershipExitType.TRANSFER_OUT);
        ownershipPeriodRepository.saveAndFlush(current);

        GoatOwnershipPeriodEntity targetPeriod = new GoatOwnershipPeriodEntity();
        targetPeriod.setGoatId(goat.getTechnicalId());
        targetPeriod.setFarmId(target.getId());
        targetPeriod.setStartedAt(effectiveAt);
        targetPeriod.setEntryType(OwnershipEntryType.TRANSFER_IN);
        targetPeriod.setSource("EVENT_TEST_TRANSFER");
        ownershipPeriodRepository.saveAndFlush(targetPeriod);

        if (updateProjection) {
            goat.setFarm(target);
            goatRepository.saveAndFlush(goat);
        }
    }

    private void cleanDatabase() {
        eventRepository.deleteAll();
        ownershipPeriodRepository.deleteAll();
        farmOperatorRepository.deleteAll();
        goatRepository.deleteAll();
        goatFarmRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();
    }
}
