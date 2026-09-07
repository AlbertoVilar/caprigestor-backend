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
import com.devmaster.goatfarm.goat.persistence.entity.Goat;
import com.devmaster.goatfarm.goat.persistence.repository.GoatRepository;
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

    private User admin;
    private User owner;
    private User linkedOperator;
    private User unlinkedOperator;
    private GoatFarm managedFarm;
    private Goat managedGoat;
    private GoatFarm otherFarm;
    private Goat otherGoat;
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

    private long createEvent(String token, GoatFarm farm, Goat goat, String description) throws Exception {
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

    private Goat createGoat(GoatFarm farm, String registrationNumber, String name) {
        Goat goat = new Goat();
        goat.setRegistrationNumber(registrationNumber);
        goat.setName(name);
        goat.setGender(Gender.FEMEA);
        goat.setBirthDate(LocalDate.now().minusYears(2));
        goat.setStatus(GoatStatus.ATIVO);
        goat.setFarm(farm);
        return goatRepository.save(goat);
    }

    private Event createPersistedEvent(Goat goat, String description) {
        Event event = new Event();
        event.setGoat(goat);
        event.setEventType(EventType.VACINACAO);
        event.setDate(LocalDate.now().minusDays(1));
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

    private String eventPath(GoatFarm farm, Goat goat) {
        return "/api/v1/goatfarms/" + farm.getId() + "/goats/" + goat.getRegistrationNumber() + "/events";
    }

    private String eventPayload(Goat goat, String description) {
        return "{\"goatId\":\"" + goat.getRegistrationNumber() + "\","
                + "\"eventType\":\"VACINACAO\","
                + "\"date\":\"" + LocalDate.now().minusDays(1) + "\","
                + "\"description\":\"" + description + "\","
                + "\"location\":\"Farm\","
                + "\"veterinarian\":\"Veterinarian\","
                + "\"outcome\":\"Completed\"}";
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private void cleanDatabase() {
        eventRepository.deleteAll();
        farmOperatorRepository.deleteAll();
        goatRepository.deleteAll();
        goatFarmRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();
    }
}
