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
import com.devmaster.goatfarm.phone.persistence.entity.Phone;
import com.devmaster.goatfarm.phone.persistence.repository.PhoneRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FarmAdministrativeAuthorizationIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private GoatFarmRepository goatFarmRepository;
    @Autowired private AddressRepository addressRepository;
    @Autowired private PhoneRepository phoneRepository;
    @Autowired private FarmOperatorRepository farmOperatorRepository;

    private User admin;
    private User owner;
    private User otherOwner;
    private User linkedOperator;
    private User unlinkedOperator;
    private GoatFarm managedFarm;
    private GoatFarm otherFarm;
    private Address managedAddress;
    private Phone managedPhone;

    @BeforeEach
    void setUp() {
        cleanDatabase();
        Role adminRole = roleRepository.save(new Role("ROLE_ADMIN", "Admin"));
        Role ownerRole = roleRepository.save(new Role("ROLE_FARM_OWNER", "Farm owner"));
        Role operatorRole = roleRepository.save(new Role("ROLE_OPERATOR", "Operator"));

        admin = createUser("admin-farm-security@example.com", "61111111111", adminRole);
        owner = createUser("owner-farm-security@example.com", "62222222222", ownerRole);
        otherOwner = createUser("other-owner-farm-security@example.com", "63333333333", ownerRole);
        linkedOperator = createUser("linked-operator-farm-security@example.com", "64444444444", operatorRole);
        unlinkedOperator = createUser("unlinked-operator-farm-security@example.com", "65555555555", operatorRole);

        managedFarm = createFarm("Managed farm security", "61111", owner, "11111111");
        otherFarm = createFarm("Other farm security", "62222", otherOwner, "22222222");
        managedAddress = managedFarm.getAddress();
        managedPhone = phoneRepository.findAllByGoatFarmId(managedFarm.getId()).getFirst();

        FarmOperator link = new FarmOperator();
        link.setFarm(managedFarm);
        link.setUser(linkedOperator);
        farmOperatorRepository.save(link);
    }

    @AfterEach
    void tearDown() {
        cleanDatabase();
    }

    @Test
    void privateAddressAndPhoneEndpointsAllowOnlyAdminOrOwningFarmOwner() throws Exception {
        String adminToken = loginAndGetToken(admin.getEmail());
        String ownerToken = loginAndGetToken(owner.getEmail());
        String otherOwnerToken = loginAndGetToken(otherOwner.getEmail());
        String linkedOperatorToken = loginAndGetToken(linkedOperator.getEmail());
        String unlinkedOperatorToken = loginAndGetToken(unlinkedOperator.getEmail());
        String addressPath = addressPath(managedFarm, managedAddress);
        String phonePath = phonePath(managedFarm, managedPhone);

        mockMvc.perform(get(addressPath).header("Authorization", bearer(adminToken))).andExpect(status().isOk());
        mockMvc.perform(get(addressPath).header("Authorization", bearer(ownerToken))).andExpect(status().isOk());
        mockMvc.perform(get(addressPath).header("Authorization", bearer(otherOwnerToken))).andExpect(status().isForbidden());
        mockMvc.perform(get(addressPath).header("Authorization", bearer(linkedOperatorToken))).andExpect(status().isForbidden());
        mockMvc.perform(get(addressPath).header("Authorization", bearer(unlinkedOperatorToken))).andExpect(status().isForbidden());
        mockMvc.perform(get(addressPath)).andExpect(status().isUnauthorized());

        mockMvc.perform(get(phonePath).header("Authorization", bearer(adminToken))).andExpect(status().isOk());
        mockMvc.perform(get(phonePath).header("Authorization", bearer(ownerToken))).andExpect(status().isOk());
        mockMvc.perform(get(phonePath).header("Authorization", bearer(otherOwnerToken))).andExpect(status().isForbidden());
        mockMvc.perform(get(phonePath).header("Authorization", bearer(linkedOperatorToken))).andExpect(status().isForbidden());
        mockMvc.perform(get(phonePath).header("Authorization", bearer(unlinkedOperatorToken))).andExpect(status().isForbidden());
        mockMvc.perform(get(phonePath)).andExpect(status().isUnauthorized());

        mockMvc.perform(put(addressPath)
                        .header("Authorization", bearer(ownerToken))
                        .contentType(MediaType.APPLICATION_JSON).content(addressPayload("Rua atualizada")))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/goatfarms/" + managedFarm.getId() + "/addresses")
                        .header("Authorization", bearer(linkedOperatorToken))
                        .contentType(MediaType.APPLICATION_JSON).content(addressPayload("Rua negada")))
                .andExpect(status().isForbidden());
        mockMvc.perform(put(phonePath)
                        .header("Authorization", bearer(ownerToken))
                        .contentType(MediaType.APPLICATION_JSON).content(phonePayload(managedPhone.getId(), "999999998")))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/goatfarms/" + managedFarm.getId() + "/phones")
                        .header("Authorization", bearer(linkedOperatorToken))
                        .contentType(MediaType.APPLICATION_JSON).content(phonePayload("999999997")))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/v1/goatfarms/" + managedFarm.getId() + "/phones")
                        .header("Authorization", bearer(ownerToken))
                        .contentType(MediaType.APPLICATION_JSON).content(phonePayload("999999996")))
                .andExpect(status().isCreated());
        Phone secondPhone = phoneRepository.findAllByGoatFarmId(managedFarm.getId()).stream()
                .filter(phone -> "999999996".equals(phone.getNumber())).findFirst().orElseThrow();
        mockMvc.perform(delete(phonePath(managedFarm, secondPhone))
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/goatfarms/" + otherFarm.getId() + "/addresses/" + managedAddress.getId())
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/v1/goatfarms/" + otherFarm.getId() + "/phones/" + managedPhone.getId())
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isNotFound());
    }

    @Test
    void operatorsAndOtherOwnersCannotAdministerFarmWhileOwnerAndAdminCanDeleteOwnScope() throws Exception {
        String adminToken = loginAndGetToken(admin.getEmail());
        String ownerToken = loginAndGetToken(owner.getEmail());
        String otherOwnerToken = loginAndGetToken(otherOwner.getEmail());
        String linkedOperatorToken = loginAndGetToken(linkedOperator.getEmail());
        String unlinkedOperatorToken = loginAndGetToken(unlinkedOperator.getEmail());
        String updatePayload = updatePayload(managedFarm, owner, managedAddress, managedPhone);

        mockMvc.perform(put("/api/v1/goatfarms/" + managedFarm.getId())
                        .header("Authorization", bearer(linkedOperatorToken))
                        .contentType(MediaType.APPLICATION_JSON).content(updatePayload))
                .andExpect(status().isForbidden());
        mockMvc.perform(put("/api/v1/goatfarms/" + managedFarm.getId())
                        .header("Authorization", bearer(unlinkedOperatorToken))
                        .contentType(MediaType.APPLICATION_JSON).content(updatePayload))
                .andExpect(status().isForbidden());
        mockMvc.perform(put("/api/v1/goatfarms/" + managedFarm.getId())
                        .header("Authorization", bearer(otherOwnerToken))
                        .contentType(MediaType.APPLICATION_JSON).content(updatePayload))
                .andExpect(status().isForbidden());
        mockMvc.perform(put("/api/v1/goatfarms/" + managedFarm.getId())
                        .contentType(MediaType.APPLICATION_JSON).content(updatePayload))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(put("/api/v1/goatfarms/" + managedFarm.getId())
                        .header("Authorization", bearer(ownerToken))
                        .contentType(MediaType.APPLICATION_JSON).content(updatePayload))
                .andExpect(status().isOk());

        GoatFarm ownerFarm = createFarm("Owner deletion farm", "63333", owner, "33333333");
        GoatFarm adminFarm = createFarm("Admin deletion farm", "64444", otherOwner, "44444444");
        mockMvc.perform(delete("/api/v1/goatfarms/" + ownerFarm.getId())
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isNoContent());
        mockMvc.perform(delete("/api/v1/goatfarms/" + adminFarm.getId())
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isNoContent());
        mockMvc.perform(delete("/api/v1/goatfarms/" + managedFarm.getId())
                        .header("Authorization", bearer(linkedOperatorToken)))
                .andExpect(status().isForbidden());
    }

    @Test
    void farmPermissionsDoNotPromoteOperatorsWithoutAnAdministrativeCapability() throws Exception {
        String ownerToken = loginAndGetToken(owner.getEmail());
        String linkedToken = loginAndGetToken(linkedOperator.getEmail());
        String unlinkedToken = loginAndGetToken(unlinkedOperator.getEmail());
        String path = "/api/v1/goatfarms/" + managedFarm.getId() + "/permissions";

        mockMvc.perform(get(path).header("Authorization", bearer(ownerToken)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.canCreateGoat").value(true));
        mockMvc.perform(get(path).header("Authorization", bearer(linkedToken)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.canCreateGoat").value(false));
        mockMvc.perform(get(path).header("Authorization", bearer(unlinkedToken)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.canCreateGoat").value(false));
    }

    @Test
    void publicFullRegistrationCreatesTheOwnerAddressAndPhonesAtomically() throws Exception {
        long farmsBefore = goatFarmRepository.count();
        long addressesBefore = addressRepository.count();
        long phonesBefore = phoneRepository.count();

        mockMvc.perform(post("/api/v1/goatfarms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(fullRegistrationPayload("Public onboarding", "65555", "public-owner@example.com", "66666666666", "988888888")))
                .andExpect(status().isCreated());

        GoatFarm created = goatFarmRepository.findAll().stream()
                .filter(farm -> "Public onboarding".equals(farm.getName())).findFirst().orElseThrow();
        User createdOwner = userRepository.findByEmail("public-owner@example.com").orElseThrow();
        assertThat(created.getUser().getId()).isEqualTo(createdOwner.getId());
        assertThat(createdOwner.getRoles()).extracting(Role::getAuthority).containsExactly("ROLE_FARM_OWNER");
        assertThat(created.getAddress()).isNotNull();
        assertThat(phoneRepository.findAllByGoatFarmId(created.getId())).hasSize(1);

        mockMvc.perform(post("/api/v1/goatfarms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(fullRegistrationPayload("Rollback onboarding", "66666", "rollback-owner@example.com", "67777777777", "11111111")))
                .andExpect(status().isConflict());

        assertThat(goatFarmRepository.count()).isEqualTo(farmsBefore + 1);
        assertThat(addressRepository.count()).isEqualTo(addressesBefore + 1);
        assertThat(phoneRepository.count()).isEqualTo(phonesBefore + 1);
        assertThat(userRepository.findByEmail("rollback-owner@example.com")).isEmpty();
    }

    @Test
    void publicFullRegistrationCannotChooseOwnerRoles() throws Exception {
        String[] prohibitedRoles = {"ROLE_ADMIN", "ROLE_OPERATOR", "ROLE_ANYTHING"};
        for (int index = 0; index < prohibitedRoles.length; index++) {
            String email = "role-attempt-" + index + "@example.com";
            String farmName = "Role attempt " + index;
            String payload = fullRegistrationPayload(farmName, "67" + index + "77", email,
                    "6888888888" + index, "97777777" + index)
                    .replace("\"confirmPassword\":\"password\"", "\"confirmPassword\":\"password\",\"roles\":[\"" + prohibitedRoles[index] + "\"]");

            mockMvc.perform(post("/api/v1/goatfarms")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(payload))
                    .andExpect(status().isUnprocessableEntity());

            assertThat(userRepository.findByEmail(email)).isEmpty();
            assertThat(goatFarmRepository.findAll()).noneMatch(farm -> farmName.equals(farm.getName()));
        }
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

    private GoatFarm createFarm(String name, String tod, User farmOwner, String phoneNumber) {
        Address address = new Address(null, "Rua " + tod, "Centro", "Monteiro", "PB", "58500000", "Brasil");
        GoatFarm farm = new GoatFarm();
        farm.setName(name);
        farm.setTod(tod);
        farm.setUser(farmOwner);
        farm.setAddress(address);
        farm = goatFarmRepository.save(farm);
        phoneRepository.save(new Phone(null, "83", phoneNumber, farm));
        return farm;
    }

    private String loginAndGetToken(String email) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"password\"}"))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("accessToken").asText();
    }

    private String addressPath(GoatFarm farm, Address address) {
        return "/api/v1/goatfarms/" + farm.getId() + "/addresses/" + address.getId();
    }

    private String phonePath(GoatFarm farm, Phone phone) {
        return "/api/v1/goatfarms/" + farm.getId() + "/phones/" + phone.getId();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private String updatePayload(GoatFarm farm, User farmOwner, Address address, Phone phone) {
        return """
                {
                  "farm": {"name":"%s", "tod":"%s", "version":%d},
                  "user": {"name":"%s", "email":"%s", "cpf":"%s"},
                  "address": {"id":%d, "street":"%s", "neighborhood":"Centro", "city":"Monteiro", "state":"PB", "zipCode":"58500000", "country":"Brasil"},
                  "phones": [{"id":%d, "ddd":"83", "number":"%s"}]
                }
                """.formatted(farm.getName(), farm.getTod(), farm.getVersion(), farmOwner.getName(), farmOwner.getEmail(),
                farmOwner.getCpf(), address.getId(), address.getStreet(), phone.getId(), phone.getNumber());
    }

    private String addressPayload(String street) {
        return """
                {"street":"%s", "neighborhood":"Centro", "city":"Monteiro", "state":"PB", "zipCode":"58500000", "country":"Brasil"}
                """.formatted(street);
    }

    private String phonePayload(String number) {
        return "{" + "\"ddd\":\"83\",\"number\":\"" + number + "\"}";
    }

    private String phonePayload(Long id, String number) {
        return "{" + "\"id\":" + id + ",\"ddd\":\"83\",\"number\":\"" + number + "\"}";
    }

    private String fullRegistrationPayload(String name, String tod, String email, String cpf, String phoneNumber) {
        return """
                {
                  "farm": {"name":"%s", "tod":"%s"},
                  "user": {"name":"Public owner", "email":"%s", "cpf":"%s", "password":"password", "confirmPassword":"password"},
                  "address": {"street":"Rua do Cadastro", "neighborhood":"Centro", "city":"Monteiro", "state":"PB", "zipCode":"58500000", "country":"Brasil"},
                  "phones": [{"ddd":"83", "number":"%s"}]
                }
                """.formatted(name, tod, email, cpf, phoneNumber);
    }

    private void cleanDatabase() {
        farmOperatorRepository.deleteAll();
        phoneRepository.deleteAll();
        goatFarmRepository.deleteAll();
        addressRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();
    }
}
