package com.devmaster.goatfarm.milk.api;

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
import com.devmaster.goatfarm.goat.persistence.entity.Goat;
import com.devmaster.goatfarm.goat.persistence.repository.GoatRepository;
import com.devmaster.goatfarm.milk.enums.LactationStatus;
import com.devmaster.goatfarm.milk.enums.MilkProductionStatus;
import com.devmaster.goatfarm.milk.enums.MilkingShift;
import com.devmaster.goatfarm.milk.persistence.entity.Lactation;
import com.devmaster.goatfarm.milk.persistence.entity.MilkProduction;
import com.devmaster.goatfarm.milk.persistence.repository.LactationRepository;
import com.devmaster.goatfarm.milk.persistence.repository.MilkProductionRepository;
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

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MilkOperationalAuthorizationIntegrationTest {

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
    private LactationRepository lactationRepository;

    @Autowired
    private MilkProductionRepository milkProductionRepository;

    private User admin;
    private User owner;
    private User linkedOperator;
    private User unlinkedOperator;
    private GoatFarm farm;
    private Goat goat;
    private Goat goatWithoutLactation;
    private Lactation activeLactation;
    private MilkProduction production;

    @BeforeEach
    void setUp() {
        cleanDatabase();

        Role adminRole = roleRepository.save(new Role("ROLE_ADMIN", "Admin"));
        Role ownerRole = roleRepository.save(new Role("ROLE_FARM_OWNER", "Farm owner"));
        Role operatorRole = roleRepository.save(new Role("ROLE_OPERATOR", "Operator"));

        admin = createUser("admin-milk@example.com", "11111111111", adminRole);
        owner = createUser("owner-milk@example.com", "22222222222", ownerRole);
        linkedOperator = createUser("linked-operator-milk@example.com", "33333333333", operatorRole);
        unlinkedOperator = createUser("unlinked-operator-milk@example.com", "44444444444", operatorRole);

        farm = new GoatFarm();
        farm.setName("Milk Farm");
        farm.setUser(owner);
        farm = goatFarmRepository.save(farm);

        FarmOperator farmOperator = new FarmOperator();
        farmOperator.setFarm(farm);
        farmOperator.setUser(linkedOperator);
        farmOperatorRepository.save(farmOperator);

        goat = createActiveFemaleGoat("MILK-001", "Lactating Goat");
        goatWithoutLactation = createActiveFemaleGoat("MILK-002", "Fresh Goat");

        activeLactation = new Lactation();
        activeLactation.setFarmId(farm.getId());
        activeLactation.setGoatId(goat.getRegistrationNumber());
        activeLactation.setStartDate(LocalDate.now().minusDays(10));
        activeLactation.setStatus(LactationStatus.ACTIVE);
        activeLactation = lactationRepository.save(activeLactation);

        production = new MilkProduction();
        production.setFarmId(farm.getId());
        production.setGoatId(goat.getRegistrationNumber());
        production.setLactation(activeLactation);
        production.setDate(LocalDate.now().minusDays(2));
        production.setShift(MilkingShift.MORNING);
        production.setVolumeLiters(new BigDecimal("2.50"));
        production.setStatus(MilkProductionStatus.ACTIVE);
        production = milkProductionRepository.save(production);
    }

    @AfterEach
    void tearDown() {
        cleanDatabase();
    }

    @Test
    void linkedOperatorCanPerformOperationalLactationAndMilkProductionRoutines() throws Exception {
        String token = loginAndGetToken(linkedOperator.getEmail());

        mockMvc.perform(get(lactationPath(goat) + "/active").header("Authorization", bearer(token)))
                .andExpect(status().isOk());
        mockMvc.perform(get(lactationPath(goat) + "/active/summary").header("Authorization", bearer(token)))
                .andExpect(status().isOk());
        mockMvc.perform(get(lactationPath(goat) + "/{id}", activeLactation.getId()).header("Authorization", bearer(token)))
                .andExpect(status().isOk());
        mockMvc.perform(get(lactationPath(goat) + "/{id}/summary", activeLactation.getId()).header("Authorization", bearer(token)))
                .andExpect(status().isOk());
        mockMvc.perform(get(lactationPath(goat)).header("Authorization", bearer(token)))
                .andExpect(status().isOk());

        mockMvc.perform(post(lactationPath(goatWithoutLactation))
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"startDate\":\"" + LocalDate.now().minusDays(1) + "\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post(milkProductionPath(goat))
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"date\":\"" + LocalDate.now().minusDays(1)
                                + "\",\"shift\":\"AFTERNOON\",\"volumeLiters\":3.15,\"notes\":\"Operacional\"}"))
                .andExpect(status().isCreated());
        mockMvc.perform(get(milkProductionPath(goat) + "/{id}", production.getId())
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk());
        mockMvc.perform(get(milkProductionPath(goat)).header("Authorization", bearer(token)))
                .andExpect(status().isOk());
        mockMvc.perform(patch(milkProductionPath(goat) + "/{id}", production.getId())
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"volumeLiters\":3.00,\"notes\":\"Correção operacional\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(patch(lactationPath(goat) + "/{id}/dry", activeLactation.getId())
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"endDate\":\"" + LocalDate.now() + "\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(patch(lactationPath(goat) + "/{id}/resume", activeLactation.getId())
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk());
    }

    @Test
    void unlinkedOperatorIsForbiddenFromLactationAndMilkProductionOperations() throws Exception {
        String token = loginAndGetToken(unlinkedOperator.getEmail());

        mockMvc.perform(get(lactationPath(goat) + "/active").header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());
        mockMvc.perform(post(milkProductionPath(goat))
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"date\":\"" + LocalDate.now().minusDays(1)
                                + "\",\"shift\":\"AFTERNOON\",\"volumeLiters\":3.15}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void farmOwnerAndAdminCanAccessOperationalLactationData() throws Exception {
        String ownerToken = loginAndGetToken(owner.getEmail());
        String adminToken = loginAndGetToken(admin.getEmail());

        mockMvc.perform(get(lactationPath(goat) + "/active").header("Authorization", bearer(ownerToken)))
                .andExpect(status().isOk());
        mockMvc.perform(get(lactationPath(goat) + "/active").header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk());
    }

    @Test
    void anonymousUserIsUnauthorizedForLactationOperations() throws Exception {
        mockMvc.perform(get(lactationPath(goat) + "/active"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void linkedOperatorCannotCancelMilkProduction() throws Exception {
        String token = loginAndGetToken(linkedOperator.getEmail());

        mockMvc.perform(delete(milkProductionPath(goat) + "/{id}", production.getId())
                        .header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());
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

    private Goat createActiveFemaleGoat(String registrationNumber, String name) {
        Goat goat = new Goat();
        goat.setRegistrationNumber(registrationNumber);
        goat.setName(name);
        goat.setGender(Gender.FEMEA);
        goat.setBirthDate(LocalDate.now().minusYears(2));
        goat.setStatus(GoatStatus.ATIVO);
        goat.setFarm(farm);
        return goatRepository.save(goat);
    }

    private String loginAndGetToken(String email) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"password\"}"))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("accessToken").asText();
    }

    private String lactationPath(Goat targetGoat) {
        return "/api/v1/goatfarms/" + farm.getId() + "/goats/" + targetGoat.getRegistrationNumber() + "/lactations";
    }

    private String milkProductionPath(Goat targetGoat) {
        return "/api/v1/goatfarms/" + farm.getId() + "/goats/" + targetGoat.getRegistrationNumber() + "/milk-productions";
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private void cleanDatabase() {
        milkProductionRepository.deleteAll();
        lactationRepository.deleteAll();
        farmOperatorRepository.deleteAll();
        goatRepository.deleteAll();
        goatFarmRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();
    }
}
