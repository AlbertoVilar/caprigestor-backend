package com.devmaster.goatfarm.commercial.api;

import com.devmaster.goatfarm.address.persistence.entity.Address;
import com.devmaster.goatfarm.authority.persistence.entity.FarmOperator;
import com.devmaster.goatfarm.authority.persistence.entity.Role;
import com.devmaster.goatfarm.authority.persistence.entity.User;
import com.devmaster.goatfarm.authority.persistence.repository.FarmOperatorRepository;
import com.devmaster.goatfarm.authority.persistence.repository.RoleRepository;
import com.devmaster.goatfarm.authority.persistence.repository.UserRepository;
import com.devmaster.goatfarm.farm.persistence.entity.GoatFarm;
import com.devmaster.goatfarm.farm.persistence.repository.GoatFarmRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CommercialAuthorizationIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private GoatFarmRepository goatFarmRepository;
    @Autowired private FarmOperatorRepository farmOperatorRepository;

    private User admin;
    private User owner;
    private User otherOwner;
    private User linkedOperator;
    private User unlinkedOperator;
    private GoatFarm managedFarm;
    private GoatFarm otherFarm;

    @BeforeEach
    void setUp() {
        Role adminRole = role("ROLE_ADMIN");
        Role ownerRole = role("ROLE_FARM_OWNER");
        Role operatorRole = role("ROLE_OPERATOR");

        admin = createUser("commercial-admin@example.com", "71111111111", adminRole);
        owner = createUser("commercial-owner@example.com", "72222222222", ownerRole);
        otherOwner = createUser("commercial-other-owner@example.com", "73333333333", ownerRole);
        linkedOperator = createUser("commercial-linked@example.com", "74444444444", operatorRole);
        unlinkedOperator = createUser("commercial-unlinked@example.com", "75555555555", operatorRole);

        managedFarm = createFarm("Commercial managed farm", "71111", owner);
        otherFarm = createFarm("Commercial other farm", "72222", otherOwner);

        FarmOperator link = new FarmOperator();
        link.setFarm(managedFarm);
        link.setUser(linkedOperator);
        farmOperatorRepository.save(link);
    }

    @Test
    void linkedOperatorCanUseOperationalReadsAndCustomerRegistration() throws Exception {
        String token = loginAndGetToken(linkedOperator.getEmail());
        String base = commercialBase(managedFarm);

        mockMvc.perform(get(base + "/customers").header("Authorization", bearer(token)))
                .andExpect(status().isOk());
        mockMvc.perform(get(base + "/animal-sales").header("Authorization", bearer(token)))
                .andExpect(status().isOk());
        mockMvc.perform(get(base + "/milk-sales").header("Authorization", bearer(token)))
                .andExpect(status().isOk());
        mockMvc.perform(get(base + "/receivables").header("Authorization", bearer(token)))
                .andExpect(status().isOk());
        mockMvc.perform(get(base + "/summary").header("Authorization", bearer(token)))
                .andExpect(status().isOk());
        mockMvc.perform(get(base + "/operational-expenses").header("Authorization", bearer(token)))
                .andExpect(status().isOk());
        mockMvc.perform(get(base + "/monthly-summary?year=2026&month=9").header("Authorization", bearer(token)))
                .andExpect(status().isOk());
        mockMvc.perform(post(base + "/customers")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Cliente operacional\",\"email\":\"cliente-operacional@example.com\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void linkedOperatorCannotPerformFinancialOrPatrimonialMutations() throws Exception {
        String token = loginAndGetToken(linkedOperator.getEmail());
        String base = commercialBase(managedFarm);

        mockMvc.perform(post(base + "/animal-sales").header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON).content(animalSalePayload()))
                .andExpect(status().isForbidden());
        mockMvc.perform(patch(base + "/animal-sales/1/payment").header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON).content(paymentPayload()))
                .andExpect(status().isForbidden());
        mockMvc.perform(post(base + "/milk-sales").header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON).content(milkSalePayload()))
                .andExpect(status().isForbidden());
        mockMvc.perform(patch(base + "/milk-sales/1/payment").header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON).content(paymentPayload()))
                .andExpect(status().isForbidden());
        mockMvc.perform(post(base + "/operational-expenses").header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON).content(expensePayload("Operador")))
                .andExpect(status().isForbidden());
    }

    @Test
    void ownerAndAdminCanCreateDefinitiveOperationalExpenses() throws Exception {
        String ownerToken = loginAndGetToken(owner.getEmail());
        String adminToken = loginAndGetToken(admin.getEmail());
        String base = commercialBase(managedFarm);

        mockMvc.perform(post(base + "/operational-expenses").header("Authorization", bearer(ownerToken))
                        .contentType(MediaType.APPLICATION_JSON).content(expensePayload("Proprietário")))
                .andExpect(status().isCreated());
        mockMvc.perform(post(base + "/operational-expenses").header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON).content(expensePayload("Administrador")))
                .andExpect(status().isCreated());
    }

    @Test
    void crossFarmUnlinkedAndAnonymousAccessAreRejected() throws Exception {
        String otherOwnerToken = loginAndGetToken(otherOwner.getEmail());
        String linkedToken = loginAndGetToken(linkedOperator.getEmail());
        String unlinkedToken = loginAndGetToken(unlinkedOperator.getEmail());
        String managedBase = commercialBase(managedFarm);
        String otherBase = commercialBase(otherFarm);

        mockMvc.perform(post(managedBase + "/operational-expenses").header("Authorization", bearer(otherOwnerToken))
                        .contentType(MediaType.APPLICATION_JSON).content(expensePayload("Outra fazenda")))
                .andExpect(status().isForbidden());
        mockMvc.perform(post(managedBase + "/operational-expenses").header("Authorization", bearer(unlinkedToken))
                        .contentType(MediaType.APPLICATION_JSON).content(expensePayload("Sem vínculo")))
                .andExpect(status().isForbidden());
        mockMvc.perform(post(managedBase + "/operational-expenses")
                        .contentType(MediaType.APPLICATION_JSON).content(expensePayload("Anônimo")))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post(otherBase + "/customers").header("Authorization", bearer(linkedToken))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Acesso cruzado\"}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get(otherBase + "/receivables").header("Authorization", bearer(linkedToken)))
                .andExpect(status().isForbidden());
    }

    private Role role(String authority) {
        return roleRepository.findByAuthority(authority)
                .orElseGet(() -> roleRepository.save(new Role(authority, authority)));
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

    private GoatFarm createFarm(String name, String tod, User farmOwner) {
        GoatFarm farm = new GoatFarm();
        farm.setName(name);
        farm.setTod(tod);
        farm.setUser(farmOwner);
        farm.setAddress(new Address(null, "Rua " + tod, "Centro", "Monteiro", "PB", "58500000", "Brasil"));
        return goatFarmRepository.save(farm);
    }

    private String loginAndGetToken(String email) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"password\"}"))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("accessToken").asText();
    }

    private String commercialBase(GoatFarm farm) {
        return "/api/v1/goatfarms/" + farm.getId() + "/commercial";
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private String animalSalePayload() {
        return "{\"goatId\":\"7111100001\",\"customerId\":1,\"saleDate\":\"2026-09-01\",\"amount\":1000.00,\"dueDate\":\"2026-09-10\"}";
    }

    private String milkSalePayload() {
        return "{\"customerId\":1,\"saleDate\":\"2026-09-01\",\"quantityLiters\":10.00,\"unitPrice\":5.00,\"dueDate\":\"2026-09-10\"}";
    }

    private String paymentPayload() {
        return "{\"paymentDate\":\"2026-09-02\"}";
    }

    private String expensePayload(String description) {
        return "{\"category\":\"OTHER\",\"description\":\"" + description
                + "\",\"amount\":100.00,\"expenseDate\":\"2026-09-01\"}";
    }
}
