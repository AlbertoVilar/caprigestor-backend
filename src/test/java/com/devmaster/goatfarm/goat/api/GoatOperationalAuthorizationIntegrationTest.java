package com.devmaster.goatfarm.goat.api;

import com.devmaster.goatfarm.audit.persistence.repository.OperationalAuditEntryRepository;
import com.devmaster.goatfarm.authority.persistence.entity.FarmOperator;
import com.devmaster.goatfarm.authority.persistence.entity.Role;
import com.devmaster.goatfarm.authority.persistence.entity.User;
import com.devmaster.goatfarm.authority.persistence.repository.FarmOperatorRepository;
import com.devmaster.goatfarm.authority.persistence.repository.RoleRepository;
import com.devmaster.goatfarm.authority.persistence.repository.UserRepository;
import com.devmaster.goatfarm.farm.persistence.entity.GoatFarm;
import com.devmaster.goatfarm.farm.persistence.repository.GoatFarmRepository;
import com.devmaster.goatfarm.goat.enums.Gender;
import com.devmaster.goatfarm.goat.enums.GoatBreed;
import com.devmaster.goatfarm.goat.enums.GoatStatus;
import com.devmaster.goatfarm.goat.persistence.entity.GoatEntity;
import com.devmaster.goatfarm.goat.persistence.repository.GoatRegistrationHistoryRepository;
import com.devmaster.goatfarm.goat.persistence.repository.GoatRepository;
import com.devmaster.goatfarm.reproduction.enums.PregnancyStatus;
import com.devmaster.goatfarm.reproduction.persistence.entity.PregnancyEntity;
import com.devmaster.goatfarm.reproduction.persistence.repository.PregnancyRepository;
import com.devmaster.goatfarm.reproduction.persistence.repository.ReproductiveEventRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GoatOperationalAuthorizationIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private GoatFarmRepository goatFarmRepository;
    @Autowired private GoatRepository goatRepository;
    @Autowired private GoatRegistrationHistoryRepository goatRegistrationHistoryRepository;
    @Autowired private OperationalAuditEntryRepository operationalAuditEntryRepository;
    @Autowired private FarmOperatorRepository farmOperatorRepository;
    @Autowired private PregnancyRepository pregnancyRepository;
    @Autowired private ReproductiveEventRepository reproductiveEventRepository;

    private User admin;
    private User owner;
    private User otherOwner;
    private User linkedOperator;
    private User unlinkedOperator;
    private GoatFarm managedFarm;
    private GoatFarm otherFarm;
    private GoatEntity mother;

    @BeforeEach
    void setUp() {
        cleanDatabase();

        Role adminRole = roleRepository.save(new Role("ROLE_ADMIN", "Admin"));
        Role ownerRole = roleRepository.save(new Role("ROLE_FARM_OWNER", "Farm owner"));
        Role operatorRole = roleRepository.save(new Role("ROLE_OPERATOR", "Operator"));

        admin = createUser("admin-goat@example.com", "10101010101", adminRole);
        owner = createUser("owner-goat@example.com", "20202020202", ownerRole);
        linkedOperator = createUser("linked-operator-goat@example.com", "30303030303", operatorRole);
        unlinkedOperator = createUser("unlinked-operator-goat@example.com", "40404040404", operatorRole);
        otherOwner = createUser("other-owner-goat@example.com", "50505050505", ownerRole);

        managedFarm = createFarm("Managed goat farm", "16153", owner);
        otherFarm = createFarm("Other goat farm", "27164", otherOwner);
        mother = createActiveFemaleGoat(managedFarm, "1615300001", "Matriz");

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
    void linkedOperatorCanCreateAnimalOnlyInItsManagedFarm() throws Exception {
        String token = loginAndGetToken(linkedOperator.getEmail());

        mockMvc.perform(post(goatsPath(managedFarm))
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(goatPayload("1615301001", "Cria operacional")))
                .andExpect(status().isCreated());

        mockMvc.perform(post(goatsPath(otherFarm))
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(goatPayload("2716401001", "Cria cruzada")))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminAndFarmOwnerCanCreateAnimalsAndUnlinkedOperatorCannot() throws Exception {
        String adminToken = loginAndGetToken(admin.getEmail());
        String ownerToken = loginAndGetToken(owner.getEmail());
        String unlinkedToken = loginAndGetToken(unlinkedOperator.getEmail());

        mockMvc.perform(post(goatsPath(otherFarm))
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(goatPayload("2716401002", "Cria administrativa")))
                .andExpect(status().isCreated());
        mockMvc.perform(post(goatsPath(managedFarm))
                        .header("Authorization", bearer(ownerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(goatPayload("1615301002", "Cria do proprietário")))
                .andExpect(status().isCreated());
        mockMvc.perform(post(goatsPath(managedFarm))
                        .header("Authorization", bearer(unlinkedToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(goatPayload("1615301003", "Cria negada")))
                .andExpect(status().isForbidden());
    }

    @Test
    void linkedOperatorCannotChangeIdentityOrPatrimonialHistory() throws Exception {
        String token = loginAndGetToken(linkedOperator.getEmail());
        String path = goatsPath(managedFarm) + "/" + mother.getRegistrationNumber();

        mockMvc.perform(put(path)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(goatPayload(mother.getRegistrationNumber(), "Tentativa de edição")))
                .andExpect(status().isForbidden());
        mockMvc.perform(patch(path + "/exit")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"exitType\":\"VENDA\",\"exitDate\":\"" + LocalDate.now() + "\"}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete(path).header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());
    }

    @Test
    void animalGenealogyAndOffspringReadsRemainPublic() throws Exception {
        String path = goatsPath(managedFarm) + "/" + mother.getRegistrationNumber();
        String technicalPath = goatsPath(managedFarm) + "/technical-" + mother.getTechnicalId();

        mockMvc.perform(get(goatsPath(managedFarm))).andExpect(status().isOk());
        mockMvc.perform(get(goatsPath(managedFarm) + "/search").param("name", "Matriz"))
                .andExpect(status().isOk());
        mockMvc.perform(get(goatsPath(managedFarm) + "/summary")).andExpect(status().isOk());
        mockMvc.perform(get(path)).andExpect(status().isOk());
        mockMvc.perform(get(technicalPath)).andExpect(status().isOk());
        mockMvc.perform(get(path + "/genealogies")).andExpect(status().isOk());
        mockMvc.perform(get(path + "/offspring")).andExpect(status().isOk());

        String operatorToken = loginAndGetToken(linkedOperator.getEmail());
        mockMvc.perform(get(technicalPath).header("Authorization", bearer(operatorToken)))
                .andExpect(status().isOk());
    }

    @Test
    void registrationRectificationIsOwnerOnlyAndIdentifierNeutral() throws Exception {
        String technicalPath = goatsPath(managedFarm) + "/technical-" + mother.getTechnicalId();
        String originalRgPath = goatsPath(managedFarm) + "/" + mother.getRegistrationNumber();
        String adminToken = loginAndGetToken(admin.getEmail());
        String ownerToken = loginAndGetToken(owner.getEmail());
        String otherOwnerToken = loginAndGetToken(otherOwner.getEmail());
        String operatorToken = loginAndGetToken(linkedOperator.getEmail());

        mockMvc.perform(patch(technicalPath + "/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rectificationPayload("00099")))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(patch(technicalPath + "/registration")
                        .header("Authorization", bearer(operatorToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rectificationPayload("00099")))
                .andExpect(status().isForbidden());
        mockMvc.perform(patch(originalRgPath + "/registration")
                        .header("Authorization", bearer(otherOwnerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rectificationPayload("00099")))
                .andExpect(status().isForbidden());

        mockMvc.perform(patch(technicalPath + "/registration")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rectificationPayload("00099")))
                .andExpect(status().isOk());

        String correctedRgPath = goatsPath(managedFarm) + "/1615300099";
        mockMvc.perform(get(technicalPath + "/registration-history")
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isOk());
        mockMvc.perform(get(correctedRgPath + "/registration-history")
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk());
        mockMvc.perform(get(correctedRgPath + "/registration-history")
                        .header("Authorization", bearer(operatorToken)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get(correctedRgPath + "/registration-history"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(patch(correctedRgPath + "/registration")
                        .header("Authorization", bearer(ownerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rectificationPayload("00098")))
                .andExpect(status().isOk());
    }

    @Test
    void ownerOnlyMutationsAllowAdminAndOwnerButDenyOtherFarmOwner() throws Exception {
        GoatEntity exitCandidate = createActiveFemaleGoat(managedFarm, "1615300002", "Saída administrativa");
        GoatEntity deleteCandidate = createActiveFemaleGoat(managedFarm, "1615300003", "Exclusão administrativa");
        String adminToken = loginAndGetToken(admin.getEmail());
        String ownerToken = loginAndGetToken(owner.getEmail());
        String otherOwnerToken = loginAndGetToken(otherOwner.getEmail());

        mockMvc.perform(put(goatsPath(managedFarm) + "/technical-" + mother.getTechnicalId())
                        .header("Authorization", bearer(ownerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(goatPayload(mother.getRegistrationNumber(), "Matriz atualizada")))
                .andExpect(status().isOk());
        mockMvc.perform(patch(goatsPath(managedFarm) + "/technical-" + exitCandidate.getTechnicalId() + "/exit")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"exitType\":\"VENDA\",\"exitDate\":\"" + LocalDate.now() + "\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(delete(goatsPath(managedFarm) + "/technical-" + deleteCandidate.getTechnicalId())
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isNoContent());

        mockMvc.perform(put(goatsPath(managedFarm) + "/" + mother.getRegistrationNumber())
                        .header("Authorization", bearer(otherOwnerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(goatPayload(mother.getRegistrationNumber(), "Tentativa cruzada")))
                .andExpect(status().isForbidden());
        mockMvc.perform(put(goatsPath(managedFarm) + "/" + mother.getRegistrationNumber())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(goatPayload(mother.getRegistrationNumber(), "Tentativa anônima")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void linkedOperatorCanRegisterBirthAndUnlinkedOperatorCannot() throws Exception {
        PregnancyEntity pregnancy = createActivePregnancy(mother);
        String linkedToken = loginAndGetToken(linkedOperator.getEmail());
        String unlinkedToken = loginAndGetToken(unlinkedOperator.getEmail());
        String path = birthPath(managedFarm, mother, pregnancy);

        mockMvc.perform(post(path)
                        .header("Authorization", bearer(linkedToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(birthPayload("1615302001", "Cria do parto")))
                .andExpect(status().isCreated());
        assertThat(goatRepository.findByRegistrationNumberAndFarmId("1615302001", managedFarm.getId())).isPresent();
        assertThat(pregnancyRepository.findById(pregnancy.getId())).get()
                .extracting(PregnancyEntity::getStatus)
                .isEqualTo(PregnancyStatus.CLOSED);

        GoatEntity secondMother = createActiveFemaleGoat(managedFarm, "1615300002", "Segunda matriz");
        PregnancyEntity secondPregnancy = createActivePregnancy(secondMother);
        mockMvc.perform(post(birthPath(managedFarm, secondMother, secondPregnancy))
                        .header("Authorization", bearer(unlinkedToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(birthPayload("1615302002", "Parto negado")))
                .andExpect(status().isForbidden());
    }

    @Test
    void failedKidCreationRollsBackBirthClosure() throws Exception {
        GoatEntity rollbackMother = createActiveFemaleGoat(managedFarm, "1615300003", "Matriz para rollback");
        PregnancyEntity pregnancy = createActivePregnancy(rollbackMother);
        createActiveFemaleGoat(managedFarm, "1615302999", "Registro já existente");
        String token = loginAndGetToken(linkedOperator.getEmail());

        mockMvc.perform(post(birthPath(managedFarm, rollbackMother, pregnancy))
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(birthPayload("1615302999", "Cria duplicada")))
                .andExpect(status().isConflict());

        PregnancyEntity persisted = pregnancyRepository.findById(pregnancy.getId()).orElseThrow();
        assertThat(persisted.getStatus()).isEqualTo(PregnancyStatus.ACTIVE);
        assertThat(persisted.getClosedAt()).isNull();
        assertThat(reproductiveEventRepository.findAll()).isEmpty();
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
        return goatFarmRepository.save(farm);
    }

    private GoatEntity createActiveFemaleGoat(GoatFarm farm, String registrationNumber, String name) {
        GoatEntity goat = new GoatEntity();
        goat.setRegistrationNumber(registrationNumber);
        goat.setTod(registrationNumber.substring(0, 5));
        goat.setToe(registrationNumber.substring(5));
        goat.setName(name);
        goat.setGender(Gender.FEMEA);
        goat.setBreed(GoatBreed.SAANEN);
        goat.setBirthDate(LocalDate.now().minusYears(2));
        goat.setStatus(GoatStatus.ATIVO);
        goat.setFarm(farm);
        return goatRepository.save(goat);
    }

    private PregnancyEntity createActivePregnancy(GoatEntity targetMother) {
        return pregnancyRepository.save(PregnancyEntity.builder()
                .farmId(managedFarm.getId())
                .goatId(targetMother.getRegistrationNumber())
                .status(PregnancyStatus.ACTIVE)
                .breedingDate(LocalDate.now().minusDays(150))
                .confirmDate(LocalDate.now().minusDays(120))
                .build());
    }

    private String loginAndGetToken(String email) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"password\"}"))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("accessToken").asText();
    }

    private String goatsPath(GoatFarm farm) {
        return "/api/v1/goatfarms/" + farm.getId() + "/goats";
    }

    private String birthPath(GoatFarm farm, GoatEntity targetMother, PregnancyEntity pregnancy) {
        return goatsPath(farm) + "/" + targetMother.getRegistrationNumber()
                + "/reproduction/pregnancies/" + pregnancy.getId() + "/births";
    }

    private String goatPayload(String registrationNumber, String name) {
        return "{\"registrationNumber\":\"" + registrationNumber + "\","
                + "\"name\":\"" + name + "\","
                + "\"gender\":\"FEMEA\",\"breed\":\"SAANEN\",\"color\":\"Branca\","
                + "\"birthDate\":\"" + LocalDate.now().minusDays(1) + "\",\"status\":\"ATIVO\","
                + "\"tod\":\"" + registrationNumber.substring(0, 5) + "\","
                + "\"toe\":\"" + registrationNumber.substring(5) + "\",\"category\":\"PA\"}";
    }

    private String birthPayload(String registrationNumber, String name) {
        return "{\"birthDate\":\"" + LocalDate.now() + "\",\"kids\":[{"
                + "\"registrationNumber\":\"" + registrationNumber + "\",\"name\":\"" + name + "\","
                + "\"gender\":\"FEMEA\",\"breed\":\"SAANEN\",\"color\":\"Branca\",\"category\":\"PA\"}]}";
    }

    private String rectificationPayload(String toe) {
        return "{\"tod\":\"16153\",\"toe\":\"" + toe + "\","
                + "\"source\":\"OFFICIAL_DOCUMENT\",\"evidenceReference\":\"SEC-A1\","
                + "\"reason\":\"Authorization contract\"}";
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private void cleanDatabase() {
        pregnancyRepository.deleteAll();
        reproductiveEventRepository.deleteAll();
        farmOperatorRepository.deleteAll();
        operationalAuditEntryRepository.deleteAll();
        goatRegistrationHistoryRepository.deleteAll();
        goatRepository.deleteAll();
        goatFarmRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();
    }
}
