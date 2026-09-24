package com.devmaster.goatfarm.farm.api;

import com.devmaster.goatfarm.address.persistence.entity.Address;
import com.devmaster.goatfarm.address.persistence.repository.AddressRepository;
import com.devmaster.goatfarm.authority.persistence.entity.FarmOperator;
import com.devmaster.goatfarm.authority.persistence.entity.Role;
import com.devmaster.goatfarm.authority.persistence.entity.User;
import com.devmaster.goatfarm.authority.persistence.repository.FarmOperatorRepository;
import com.devmaster.goatfarm.authority.persistence.repository.RoleRepository;
import com.devmaster.goatfarm.authority.persistence.repository.UserRepository;
import com.devmaster.goatfarm.farm.persistence.entity.GoatFarm;
import com.devmaster.goatfarm.farm.persistence.repository.GoatFarmRepository;
import com.fasterxml.jackson.databind.JsonNode;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ManagedFarmControllerIntegrationTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private GoatFarmRepository goatFarmRepository;
    @Autowired private FarmOperatorRepository farmOperatorRepository;
    @Autowired private AddressRepository addressRepository;

    private Role adminRole;
    private Role ownerRole;
    private Role operatorRole;
    private Role userRole;
    private User owner;
    private User otherOwner;
    private User linkedOperator;
    private User unlinkedOperator;
    private User dualRole;
    private User admin;
    private GoatFarm ownerFarm;
    private GoatFarm otherFarm;

    @BeforeEach
    void setUp() {
        cleanDatabase();
        adminRole = roleRepository.save(new Role("ROLE_ADMIN", "Admin"));
        ownerRole = roleRepository.save(new Role("ROLE_FARM_OWNER", "Farm owner"));
        operatorRole = roleRepository.save(new Role("ROLE_OPERATOR", "Operator"));
        userRole = roleRepository.save(new Role("ROLE_USER", "User"));

        owner = createUser("managed-owner@example.com", "71111111111", ownerRole);
        otherOwner = createUser("managed-other-owner@example.com", "72222222222", ownerRole);
        linkedOperator = createUser("managed-linked-operator@example.com", "73333333333", operatorRole);
        unlinkedOperator = createUser("managed-unlinked-operator@example.com", "74444444444", operatorRole);
        dualRole = createUser("managed-dual@example.com", "75555555555", ownerRole, operatorRole);
        admin = createUser("managed-admin@example.com", "76666666666", adminRole);
        createUser("managed-user@example.com", "77777777777", userRole);

        ownerFarm = createFarm("Alpha Managed Farm", "71001", owner);
        otherFarm = createFarm("Beta Managed Farm", "71002", dualRole);
        GoatFarm dualFarm = createFarm("Gamma Managed Farm", "71003", otherOwner);
        link(dualFarm, linkedOperator);
        link(otherFarm, dualRole); // owner + operator access to the same farm must deduplicate
        link(otherFarm, dualRole);
        link(dualFarm, dualRole); // distinct operator access proves the owner/operator union
    }

    @AfterEach
    void tearDown() {
        cleanDatabase();
    }

    @Test
    void endpointRequiresAuthenticationAndSupportedRole() throws Exception {
        mockMvc.perform(get("/api/v1/goatfarms/managed")).andExpect(status().isUnauthorized());
        String userToken = loginAndGetToken("managed-user@example.com");
        mockMvc.perform(get("/api/v1/goatfarms/managed").header("Authorization", bearer(userToken)))
                .andExpect(status().isForbidden());
    }

    @Test
    void ownerAndOperatorReceiveOnlyCanonicalManagedUnion() throws Exception {
        JsonNode ownerPage = body(get("/api/v1/goatfarms/managed"), loginAndGetToken(owner.getEmail()));
        assertThat(names(ownerPage)).containsExactly("Alpha Managed Farm");

        JsonNode operatorPage = body(get("/api/v1/goatfarms/managed"), loginAndGetToken(linkedOperator.getEmail()));
        assertThat(names(operatorPage)).containsExactly("Gamma Managed Farm");

        JsonNode dualPage = body(get("/api/v1/goatfarms/managed"), loginAndGetToken(dualRole.getEmail()));
        assertThat(names(dualPage)).containsExactly("Beta Managed Farm", "Gamma Managed Farm");
        assertThat(dualPage.path("totalElements").asLong()).isEqualTo(2L);
    }

    @Test
    void unlinkedOperatorReceivesEmptyManagedPage() throws Exception {
        JsonNode page = body(get("/api/v1/goatfarms/managed"), loginAndGetToken(unlinkedOperator.getEmail()));

        assertThat(page.path("content").size()).isZero();
        assertThat(page.path("totalElements").asLong()).isZero();
    }

    @Test
    void managedFarmSearchFiltersAuthorizedUnionAndPreservesDeduplicatedPagination() throws Exception {
        JsonNode ownerMatch = body(get("/api/v1/goatfarms/managed?query=Alpha"), loginAndGetToken(owner.getEmail()));
        assertThat(names(ownerMatch)).containsExactly("Alpha Managed Farm");

        JsonNode ownerCannotSearchOtherFarm = body(
                get("/api/v1/goatfarms/managed?query=Gamma"), loginAndGetToken(owner.getEmail()));
        assertThat(ownerCannotSearchOtherFarm.path("content").size()).isZero();

        JsonNode dualMatch = body(
                get("/api/v1/goatfarms/managed?query=Managed&size=1&page=0"), loginAndGetToken(dualRole.getEmail()));
        assertThat(names(dualMatch)).containsExactly("Beta Managed Farm");
        assertThat(dualMatch.path("totalElements").asLong()).isEqualTo(2L);

        JsonNode dualSecondPage = body(
                get("/api/v1/goatfarms/managed?query=Managed&size=1&page=1"), loginAndGetToken(dualRole.getEmail()));
        assertThat(names(dualSecondPage)).containsExactly("Gamma Managed Farm");
        assertThat(dualSecondPage.path("totalElements").asLong()).isEqualTo(2L);
    }

    @Test
    void adminSeesAllFarmsWithDeterministicPaginationAndMinimalDTO() throws Exception {
        JsonNode firstPage = body(get("/api/v1/goatfarms/managed?page=0&size=2"), loginAndGetToken(admin.getEmail()));
        assertThat(firstPage.path("totalElements").asLong()).isEqualTo(3L);
        assertThat(firstPage.path("content").size()).isEqualTo(2);
        assertThat(names(firstPage)).containsExactly("Alpha Managed Farm", "Beta Managed Farm");
        JsonNode item = firstPage.path("content").get(0);
        assertThat(item.fieldNames()).toIterable().containsExactlyInAnyOrder("id", "name", "tod", "logoUrl");
    }

    @Test
    void adminSearchAndPaginationRemainDeterministic() throws Exception {
        JsonNode firstPage = body(
                get("/api/v1/goatfarms/managed?query=Managed&size=2&page=0"), loginAndGetToken(admin.getEmail()));
        assertThat(names(firstPage)).containsExactly("Alpha Managed Farm", "Beta Managed Farm");
        assertThat(firstPage.path("totalElements").asLong()).isEqualTo(3L);

        JsonNode secondPage = body(
                get("/api/v1/goatfarms/managed?query=Managed&size=2&page=1"), loginAndGetToken(admin.getEmail()));
        assertThat(names(secondPage)).containsExactly("Gamma Managed Farm");
        assertThat(secondPage.path("totalElements").asLong()).isEqualTo(3L);
    }

    private JsonNode body(org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder request,
                          String token) throws Exception {
        MvcResult result = mockMvc.perform(request.header("Authorization", bearer(token))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private java.util.List<String> names(JsonNode page) {
        java.util.List<String> names = new java.util.ArrayList<>();
        page.path("content").forEach(item -> names.add(item.path("name").asText()));
        return names;
    }

    private User createUser(String email, String cpf, Role... roles) {
        User user = new User();
        user.setName(email);
        user.setEmail(email);
        user.setCpf(cpf);
        user.setPassword(passwordEncoder.encode("password"));
        for (Role role : roles) user.addRole(role);
        return userRepository.save(user);
    }

    private GoatFarm createFarm(String name, String tod, User owner) {
        GoatFarm farm = new GoatFarm();
        farm.setName(name);
        farm.setTod(tod);
        farm.setUser(owner);
        farm.setAddress(new Address(null, "Rua " + tod, "Centro", "Monteiro", "PB", "58500000", "Brasil"));
        return goatFarmRepository.save(farm);
    }

    private void link(GoatFarm farm, User user) {
        FarmOperator link = new FarmOperator();
        link.setFarm(farm);
        link.setUser(user);
        farmOperatorRepository.save(link);
    }

    private String loginAndGetToken(String email) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"password\"}"))
                .andExpect(status().isOk()).andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("accessToken").asText();
    }

    private String bearer(String token) { return "Bearer " + token; }

    private void cleanDatabase() {
        farmOperatorRepository.deleteAll();
        goatFarmRepository.deleteAll();
        addressRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();
    }
}
