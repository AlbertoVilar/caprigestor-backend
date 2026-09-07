package com.devmaster.goatfarm.goat.api;

import com.devmaster.goatfarm.authority.persistence.entity.FarmOperator;
import com.devmaster.goatfarm.authority.persistence.entity.Role;
import com.devmaster.goatfarm.authority.persistence.entity.User;
import com.devmaster.goatfarm.authority.persistence.repository.FarmOperatorRepository;
import com.devmaster.goatfarm.authority.persistence.repository.RoleRepository;
import com.devmaster.goatfarm.authority.persistence.repository.UserRepository;
import com.devmaster.goatfarm.farm.persistence.entity.GoatFarm;
import com.devmaster.goatfarm.farm.persistence.repository.GoatFarmRepository;
import com.devmaster.goatfarm.genealogy.application.ports.out.GenealogyAbccQueryPort;
import com.devmaster.goatfarm.goat.application.ports.out.GoatAbccPublicQueryPort;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccRaceOptionVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccRawPreviewVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccRawSearchItemVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccRawSearchResultVO;
import com.devmaster.goatfarm.goat.persistence.repository.GoatRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GoatAbccAuthorizationIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private GoatFarmRepository goatFarmRepository;
    @Autowired private GoatRepository goatRepository;
    @Autowired private FarmOperatorRepository farmOperatorRepository;

    @MockBean private GoatAbccPublicQueryPort abccPublicQueryPort;
    @MockBean private GenealogyAbccQueryPort genealogyAbccQueryPort;

    private User admin;
    private User owner;
    private User linkedOperator;
    private User unlinkedOperator;
    private User otherOwner;
    private GoatFarm managedFarm;
    private GoatFarm otherFarm;

    @BeforeEach
    void setUp() {
        cleanDatabase();

        Role adminRole = roleRepository.save(new Role("ROLE_ADMIN", "Admin"));
        Role ownerRole = roleRepository.save(new Role("ROLE_FARM_OWNER", "Farm owner"));
        Role operatorRole = roleRepository.save(new Role("ROLE_OPERATOR", "Operator"));
        admin = createUser("admin-abcc@example.com", "10101010101", adminRole);
        owner = createUser("owner-abcc@example.com", "20202020202", ownerRole);
        linkedOperator = createUser("linked-operator-abcc@example.com", "30303030303", operatorRole);
        unlinkedOperator = createUser("unlinked-operator-abcc@example.com", "40404040404", operatorRole);
        otherOwner = createUser("other-owner-abcc@example.com", "50505050505", ownerRole);

        managedFarm = createFarm("Managed ABCC farm", "16153", owner);
        otherFarm = createFarm("Other ABCC farm", "27164", otherOwner);
        FarmOperator link = new FarmOperator();
        link.setFarm(managedFarm);
        link.setUser(linkedOperator);
        farmOperatorRepository.save(link);

        when(abccPublicQueryPort.listRaces()).thenReturn(List.of(
                GoatAbccRaceOptionVO.builder().id(9).name("SAANEN").build()));
        when(abccPublicQueryPort.search(any())).thenReturn(GoatAbccRawSearchResultVO.builder()
                .currentPage(1).totalPages(1)
                .items(List.of(GoatAbccRawSearchItemVO.builder()
                        .externalId("search-1").nome("Animal ABCC").situacao("RGD")
                        .sexo("Macho").raca("SAANEN").tod("99999").build()))
                .build());
        when(abccPublicQueryPort.preview(anyString())).thenAnswer(invocation -> previewFor(invocation.getArgument(0)));
    }

    @AfterEach
    void tearDown() {
        cleanDatabase();
    }

    @Test
    void racesSearchAndPreviewArePublicAndDoNotPersistLocalData() throws Exception {
        long goatsBefore = goatRepository.count();
        List<String> tokens = Arrays.asList(
                null,
                loginAndGetToken(admin.getEmail()),
                loginAndGetToken(owner.getEmail()),
                loginAndGetToken(linkedOperator.getEmail()),
                loginAndGetToken(unlinkedOperator.getEmail())
        );

        for (String token : tokens) {
            mockMvc.perform(withAuthorization(get(abccPath(managedFarm) + "/races"), token))
                    .andExpect(status().isOk());
            mockMvc.perform(withAuthorization(post(abccPath(managedFarm) + "/search")
                            .contentType(MediaType.APPLICATION_JSON).content(searchPayload()), token))
                    .andExpect(status().isOk());
            mockMvc.perform(withAuthorization(post(abccPath(managedFarm) + "/preview")
                            .contentType(MediaType.APPLICATION_JSON).content("{\"externalId\":\"preview-public\"}"), token))
                    .andExpect(status().isOk());
        }

        assertThat(goatRepository.count()).isEqualTo(goatsBefore);
    }

    @Test
    void confirmIsLimitedToAdminAndOwningFarmOwner() throws Exception {
        String adminToken = loginAndGetToken(admin.getEmail());
        String ownerToken = loginAndGetToken(owner.getEmail());
        String linkedOperatorToken = loginAndGetToken(linkedOperator.getEmail());
        String unlinkedOperatorToken = loginAndGetToken(unlinkedOperator.getEmail());
        String otherOwnerToken = loginAndGetToken(otherOwner.getEmail());

        mockMvc.perform(withAuthorization(post(abccPath(managedFarm) + "/confirm")
                        .contentType(MediaType.APPLICATION_JSON).content(confirmPayload("confirm-admin", "1615303001")), adminToken))
                .andExpect(status().isCreated());
        mockMvc.perform(withAuthorization(post(abccPath(managedFarm) + "/confirm")
                        .contentType(MediaType.APPLICATION_JSON).content(confirmPayload("confirm-owner", "1615303002")), ownerToken))
                .andExpect(status().isCreated());
        mockMvc.perform(withAuthorization(post(abccPath(managedFarm) + "/confirm")
                        .contentType(MediaType.APPLICATION_JSON).content(confirmPayload("confirm-linked", "1615303003")), linkedOperatorToken))
                .andExpect(status().isForbidden());
        mockMvc.perform(withAuthorization(post(abccPath(managedFarm) + "/confirm")
                        .contentType(MediaType.APPLICATION_JSON).content(confirmPayload("confirm-unlinked", "1615303004")), unlinkedOperatorToken))
                .andExpect(status().isForbidden());
        mockMvc.perform(withAuthorization(post(abccPath(managedFarm) + "/confirm")
                        .contentType(MediaType.APPLICATION_JSON).content(confirmPayload("confirm-cross", "1615303005")), otherOwnerToken))
                .andExpect(status().isForbidden());
        mockMvc.perform(post(abccPath(managedFarm) + "/confirm")
                        .contentType(MediaType.APPLICATION_JSON).content(confirmPayload("confirm-anonymous", "1615303006")))
                .andExpect(status().isUnauthorized());

        assertThat(goatRepository.findByIdAndFarmId("1615303001", managedFarm.getId())).isPresent();
        assertThat(goatRepository.findByIdAndFarmId("1615303002", managedFarm.getId())).isPresent();
        assertThat(goatRepository.findByIdAndFarmId("1615303003", managedFarm.getId())).isEmpty();
        assertThat(goatRepository.findByIdAndFarmId("1615303004", managedFarm.getId())).isEmpty();
    }

    @Test
    void confirmBatchHasTheSameAuthorizationBoundary() throws Exception {
        String adminToken = loginAndGetToken(admin.getEmail());
        String ownerToken = loginAndGetToken(owner.getEmail());
        String linkedOperatorToken = loginAndGetToken(linkedOperator.getEmail());
        String unlinkedOperatorToken = loginAndGetToken(unlinkedOperator.getEmail());
        String otherOwnerToken = loginAndGetToken(otherOwner.getEmail());

        mockMvc.perform(withAuthorization(post(abccPath(managedFarm) + "/confirm-batch")
                        .contentType(MediaType.APPLICATION_JSON).content(batchPayload("batch-admin")), adminToken))
                .andExpect(status().isOk());
        mockMvc.perform(withAuthorization(post(abccPath(managedFarm) + "/confirm-batch")
                        .contentType(MediaType.APPLICATION_JSON).content(batchPayload("batch-owner")), ownerToken))
                .andExpect(status().isOk());
        mockMvc.perform(withAuthorization(post(abccPath(managedFarm) + "/confirm-batch")
                        .contentType(MediaType.APPLICATION_JSON).content(batchPayload("batch-linked")), linkedOperatorToken))
                .andExpect(status().isForbidden());
        mockMvc.perform(withAuthorization(post(abccPath(managedFarm) + "/confirm-batch")
                        .contentType(MediaType.APPLICATION_JSON).content(batchPayload("batch-unlinked")), unlinkedOperatorToken))
                .andExpect(status().isForbidden());
        mockMvc.perform(withAuthorization(post(abccPath(managedFarm) + "/confirm-batch")
                        .contentType(MediaType.APPLICATION_JSON).content(batchPayload("batch-cross")), otherOwnerToken))
                .andExpect(status().isForbidden());
        mockMvc.perform(post(abccPath(managedFarm) + "/confirm-batch")
                        .contentType(MediaType.APPLICATION_JSON).content(batchPayload("batch-anonymous")))
                .andExpect(status().isUnauthorized());
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

    private GoatAbccRawPreviewVO previewFor(String externalId) {
        String registrationNumber = switch (externalId) {
            case "confirm-admin" -> "1615303001";
            case "confirm-owner" -> "1615303002";
            case "batch-admin" -> "1615304001";
            case "batch-owner" -> "1615304002";
            default -> "1615304999";
        };
        return GoatAbccRawPreviewVO.builder()
                .externalId(externalId).registro(registrationNumber).nome("Animal " + externalId)
                .sexo("Fêmea").raca("SAANEN").pelagem("Branca").situacao("RGD")
                .categoria("PA").dataNascimento("10/01/2020").tod("16153")
                .toe(registrationNumber.substring(5)).build();
    }

    private String loginAndGetToken(String email) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"password\"}"))
                .andExpect(status().isOk()).andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("accessToken").asText();
    }

    private String abccPath(GoatFarm farm) {
        return "/api/v1/goatfarms/" + farm.getId() + "/goats/imports/abcc";
    }

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder withAuthorization(
            org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder request, String token) {
        return token == null ? request : request.header("Authorization", "Bearer " + token);
    }

    private String searchPayload() {
        return "{\"raceId\":9,\"affix\":\"CAPRIL\",\"page\":1}";
    }

    private String confirmPayload(String externalId, String registrationNumber) {
        return "{\"externalId\":\"" + externalId + "\",\"goat\":{"
                + "\"registrationNumber\":\"" + registrationNumber + "\",\"name\":\"Animal " + externalId + "\","
                + "\"gender\":\"FEMEA\",\"breed\":\"SAANEN\",\"color\":\"Branca\","
                + "\"birthDate\":\"2020-01-10\",\"status\":\"ATIVO\",\"tod\":\"16153\","
                + "\"toe\":\"" + registrationNumber.substring(5) + "\",\"category\":\"PA\"}}";
    }

    private String batchPayload(String externalId) {
        return "{\"items\":[{\"externalId\":\"" + externalId + "\"}]}";
    }

    private void cleanDatabase() {
        farmOperatorRepository.deleteAll();
        goatRepository.deleteAll();
        goatFarmRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();
    }
}
