package com.devmaster.goatfarm.goat.integration;

import com.devmaster.goatfarm.authority.application.ports.in.CurrentPrincipalQueryUseCase;
import com.devmaster.goatfarm.authority.application.ports.in.FarmAuthorizationUseCase;
import com.devmaster.goatfarm.authority.business.bo.AuthenticatedPrincipal;
import com.devmaster.goatfarm.genealogy.application.ports.out.GenealogyAbccQueryPort;
import com.devmaster.goatfarm.goat.application.model.GoatCreationOrigin;
import com.devmaster.goatfarm.goat.application.ports.in.GoatAbccImportUseCase;
import com.devmaster.goatfarm.goat.application.ports.in.GoatManagementUseCase;
import com.devmaster.goatfarm.goat.application.ports.out.GoatAbccPublicQueryPort;
import com.devmaster.goatfarm.goat.application.ports.out.GoatExternalParentQueryPort;
import com.devmaster.goatfarm.goat.business.bo.GoatRequestVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccBatchConfirmItemVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccRawPreviewVO;
import com.devmaster.goatfarm.goat.enums.Category;
import com.devmaster.goatfarm.goat.enums.Gender;
import com.devmaster.goatfarm.goat.enums.GoatBreed;
import com.devmaster.goatfarm.goat.enums.GoatStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class GoatAbccBatchPostgresIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect");
    }

    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private GoatAbccImportUseCase abccImportUseCase;
    @Autowired private GoatManagementUseCase goatManagementUseCase;

    @MockBean private FarmAuthorizationUseCase farmAuthorizationUseCase;
    @MockBean private CurrentPrincipalQueryUseCase currentPrincipalQueryUseCase;
    @MockBean private GoatAbccPublicQueryPort abccPublicQueryPort;
    @MockBean private GoatExternalParentQueryPort goatExternalParentQueryPort;
    @MockBean private GenealogyAbccQueryPort genealogyAbccQueryPort;

    @Test
    void batchKeepsPriorImportWhenLaterCreationFails() {
        long userId = createUser("batch-owner@example.com", "10000000001");
        long targetFarmId = createFarm(userId, "ABCC Batch Target", "16153");
        long otherFarmId = createFarm(userId, "ABCC Batch Other", "27164");
        configureAuthorizedPrincipal(userId, targetFarmId, otherFarmId);

        String importedRegistration = "1615399001";
        String duplicateRegistration = "2716499002";
        goatManagementUseCase.createGoat(otherFarmId, goatRequest(duplicateRegistration, "Existing elsewhere", "27164"),
                GoatCreationOrigin.MANUAL);

        when(abccPublicQueryPort.preview("success")).thenReturn(preview("success", importedRegistration, "First succeeds", "16153"));
        when(abccPublicQueryPort.preview("global-duplicate")).thenReturn(
                preview("global-duplicate", duplicateRegistration, "Fails in create", "16153"));

        var response = abccImportUseCase.confirmBatch(targetFarmId, List.of(
                GoatAbccBatchConfirmItemVO.builder().externalId("success").build(),
                GoatAbccBatchConfirmItemVO.builder().externalId("global-duplicate").build()
        ));

        assertThat(response.getTotalImported()).isEqualTo(1);
        assertThat(response.getTotalError()).isEqualTo(1);
        assertThat(response.getResults().stream().map(result -> result.getStatus()).toList())
                .containsExactly("IMPORTED", "ERROR");
        assertThat(countGoats(importedRegistration)).isEqualTo(1);
        assertThat(countGoats(duplicateRegistration)).isEqualTo(1);
    }

    @Test
    void batchCommitsAllValidItems() {
        long userId = createUser("all-valid@example.com", "10000000002");
        long farmId = createFarm(userId, "ABCC Batch All Valid", "16154");
        configureAuthorizedPrincipal(userId, farmId);

        when(abccPublicQueryPort.preview("valid-one")).thenReturn(preview("valid-one", "1615499011", "Valid one", "16154"));
        when(abccPublicQueryPort.preview("valid-two")).thenReturn(preview("valid-two", "1615499012", "Valid two", "16154"));

        var response = abccImportUseCase.confirmBatch(farmId, List.of(
                GoatAbccBatchConfirmItemVO.builder().externalId("valid-one").build(),
                GoatAbccBatchConfirmItemVO.builder().externalId("valid-two").build()
        ));

        assertThat(response.getTotalImported()).isEqualTo(2);
        assertThat(response.getTotalError()).isZero();
        assertThat(countGoats("1615499011")).isEqualTo(1);
        assertThat(countGoats("1615499012")).isEqualTo(1);
    }

    @Test
    void batchCommitsValidItemAndSkipsLaterTodMismatch() {
        long userId = createUser("tod-mismatch@example.com", "10000000003");
        long farmId = createFarm(userId, "ABCC Batch TOD", "16155");
        configureAuthorizedPrincipal(userId, farmId);

        when(abccPublicQueryPort.preview("valid")).thenReturn(preview("valid", "1615599021", "Valid", "16155"));
        when(abccPublicQueryPort.preview("wrong-tod")).thenReturn(preview("wrong-tod", "1615599022", "Wrong TOD", "99999"));

        var response = abccImportUseCase.confirmBatch(farmId, List.of(
                GoatAbccBatchConfirmItemVO.builder().externalId("valid").build(),
                GoatAbccBatchConfirmItemVO.builder().externalId("wrong-tod").build()
        ));

        assertThat(response.getTotalImported()).isEqualTo(1);
        assertThat(response.getTotalSkippedTodMismatch()).isEqualTo(1);
        assertThat(countGoats("1615599021")).isEqualTo(1);
        assertThat(countGoats("1615599022")).isZero();
    }

    @Test
    void batchContinuesWithLaterSuccessAfterCreationFailure() {
        long userId = createUser("later-success@example.com", "10000000004");
        long targetFarmId = createFarm(userId, "ABCC Batch Later Target", "16156");
        long otherFarmId = createFarm(userId, "ABCC Batch Later Other", "27165");
        configureAuthorizedPrincipal(userId, targetFarmId, otherFarmId);

        goatManagementUseCase.createGoat(otherFarmId, goatRequest("2716599031", "Existing elsewhere", "27165"),
                GoatCreationOrigin.MANUAL);
        when(abccPublicQueryPort.preview("global-duplicate")).thenReturn(
                preview("global-duplicate", "2716599031", "Fails in create", "16156"));
        when(abccPublicQueryPort.preview("later-success")).thenReturn(
                preview("later-success", "1615699032", "Later success", "16156"));

        var response = abccImportUseCase.confirmBatch(targetFarmId, List.of(
                GoatAbccBatchConfirmItemVO.builder().externalId("global-duplicate").build(),
                GoatAbccBatchConfirmItemVO.builder().externalId("later-success").build()
        ));

        assertThat(response.getTotalImported()).isEqualTo(1);
        assertThat(response.getTotalError()).isEqualTo(1);
        assertThat(response.getResults().stream().map(result -> result.getStatus()).toList())
                .containsExactly("ERROR", "IMPORTED");
        assertThat(countGoats("1615699032")).isEqualTo(1);
    }

    @Test
    void batchSkipsDuplicateAlreadyInTheTargetFarm() {
        long userId = createUser("local-duplicate@example.com", "10000000005");
        long farmId = createFarm(userId, "ABCC Batch Local Duplicate", "16157");
        configureAuthorizedPrincipal(userId, farmId);

        goatManagementUseCase.createGoat(farmId, goatRequest("1615799041", "Existing locally", "16157"), GoatCreationOrigin.MANUAL);
        when(abccPublicQueryPort.preview("local-duplicate")).thenReturn(
                preview("local-duplicate", "1615799041", "Existing locally", "16157"));

        var response = abccImportUseCase.confirmBatch(farmId, List.of(
                GoatAbccBatchConfirmItemVO.builder().externalId("local-duplicate").build()
        ));

        assertThat(response.getTotalImported()).isZero();
        assertThat(response.getTotalSkippedDuplicate()).isEqualTo(1);
        assertThat(countGoats("1615799041")).isEqualTo(1);
    }

    private void configureAuthorizedPrincipal(long userId, long... farmIds) {
        for (long farmId : farmIds) {
            doNothing().when(farmAuthorizationUseCase).verifyFarmOwnership(farmId);
            doNothing().when(farmAuthorizationUseCase).verifyFarmManagement(farmId);
        }
        when(currentPrincipalQueryUseCase.requireCurrent()).thenReturn(
                new AuthenticatedPrincipal(userId, "batch-owner@example.com", "Batch owner", Set.of("ROLE_FARM_OWNER")));
    }

    private long createUser(String email, String cpf) {
        return jdbcTemplate.queryForObject(
                "insert into users (name, email, password, cpf) values (?, ?, 'password', ?) returning id",
                Long.class, "ABCC Batch User", email, cpf);
    }

    private long createFarm(long userId, String name, String tod) {
        return jdbcTemplate.queryForObject(
                "insert into capril (name, user_id, tod) values (?, ?, ?) returning id",
                Long.class, name, userId, tod);
    }

    private GoatRequestVO goatRequest(String registrationNumber, String name, String tod) {
        return GoatRequestVO.builder()
                .registrationNumber(registrationNumber)
                .tod(tod).toe(registrationNumber.substring(5))
                .name(name).gender(Gender.FEMEA).breed(GoatBreed.SAANEN).color("Branca")
                .birthDate(LocalDate.of(2025, 1, 1)).status(GoatStatus.ATIVO).category(Category.PA)
                .build();
    }

    private GoatAbccRawPreviewVO preview(String externalId, String registrationNumber, String name, String tod) {
        return GoatAbccRawPreviewVO.builder()
                .externalId(externalId).registro(registrationNumber).nome(name)
                .sexo("Fêmea").raca("SAANEN").pelagem("Branca").situacao("RGD")
                .categoria("PA").dataNascimento("01/01/2025").tod(tod)
                .toe(registrationNumber.substring(5)).build();
    }

    private int countGoats(String registrationNumber) {
        return jdbcTemplate.queryForObject(
                "select count(*) from cabras where num_registro = ?", Integer.class, registrationNumber);
    }
}
