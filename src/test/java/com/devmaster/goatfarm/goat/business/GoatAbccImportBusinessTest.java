package com.devmaster.goatfarm.goat.business;

import com.devmaster.goatfarm.application.core.business.common.EntityFinder;
import com.devmaster.goatfarm.authority.application.ports.in.CurrentPrincipalQueryUseCase;
import com.devmaster.goatfarm.authority.application.ports.in.FarmAuthorizationUseCase;
import com.devmaster.goatfarm.authority.business.bo.AuthenticatedPrincipal;
import com.devmaster.goatfarm.config.exceptions.DuplicateEntityException;
import com.devmaster.goatfarm.config.exceptions.custom.BusinessRuleException;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.farm.application.model.FarmRecord;
import com.devmaster.goatfarm.farm.application.ports.out.GoatFarmPersistencePort;
import com.devmaster.goatfarm.goat.application.model.GoatCreationOrigin;
import com.devmaster.goatfarm.goat.application.ports.in.GoatAbccQueryUseCase;
import com.devmaster.goatfarm.goat.application.ports.in.GoatManagementUseCase;
import com.devmaster.goatfarm.goat.application.ports.out.GoatReferenceQueryPort;
import com.devmaster.goatfarm.goat.application.ports.out.GoatReference;
import com.devmaster.goatfarm.goat.business.abcc.AbccImportEligibilityPolicy;
import com.devmaster.goatfarm.goat.business.abcc.AbccAnimalTranslator;
import com.devmaster.goatfarm.goat.business.bo.GoatRequestVO;
import com.devmaster.goatfarm.goat.business.bo.GoatResponseVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccBatchConfirmItemVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccPreviewRequestVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccPreviewResponseVO;
import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goat.enums.Gender;
import com.devmaster.goatfarm.goat.enums.GoatBreed;
import com.devmaster.goatfarm.goat.enums.GoatStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GoatAbccImportBusinessTest {

    @Mock private FarmAuthorizationUseCase ownershipService;
    @Mock private CurrentPrincipalQueryUseCase currentPrincipalQuery;
    @Mock private GoatFarmPersistencePort goatFarmPort;
    @Mock private GoatAbccQueryUseCase goatAbccQueryUseCase;
    @Mock private GoatManagementUseCase goatManagementUseCase;
    @Mock private GoatReferenceQueryPort goatReferenceQueryPort;
    @Mock private EntityFinder entityFinder;

    private GoatAbccImportBusiness business;

    @BeforeEach
    void setUp() {
        business = new GoatAbccImportBusiness(ownershipService, goatFarmPort, goatAbccQueryUseCase,
                goatManagementUseCase, goatReferenceQueryPort, entityFinder, currentPrincipalQuery,
                new AbccAnimalTranslator(), new AbccImportEligibilityPolicy());
        lenient().when(entityFinder.findOrThrow(any(), any())).thenAnswer(invocation -> {
            Supplier<Optional<?>> supplier = invocation.getArgument(0);
            String message = invocation.getArgument(1);
            return supplier.get().orElseThrow(() -> new ResourceNotFoundException(message));
        });
        lenient().doNothing().when(ownershipService).verifyFarmOwnership(1L);
        lenient().when(currentPrincipalQuery.requireCurrent()).thenReturn(principal("ROLE_FARM_OWNER"));
    }

    @Test
    void confirmUsesQueryUseCasePreviewAndDelegatesAnimalCreation() {
        when(goatFarmPort.findById(1L)).thenReturn(Optional.of(farm("12345")));
        when(goatAbccQueryUseCase.preview(eq(1L), any())).thenReturn(preview("A-1", "1643218012", "12345"));
        GoatRequestVO request = request("1643218012", "12345");
        GoatResponseVO expected = response("1643218012", "ANIMAL");
        when(goatManagementUseCase.createGoat(1L, request, GoatCreationOrigin.ABCC_IMPORT)).thenReturn(expected);

        assertThat(business.confirm(1L, "A-1", request)).isSameAs(expected);

        verify(ownershipService).verifyFarmOwnership(1L);
        verify(goatAbccQueryUseCase).preview(eq(1L), any(GoatAbccPreviewRequestVO.class));
        verify(goatManagementUseCase).createGoat(1L, request, GoatCreationOrigin.ABCC_IMPORT);
    }

    @Test
    void confirmAttachesAbccCreatorEvidenceAndDoesNotReplaceItWithImporter() {
        when(goatFarmPort.findById(1L)).thenReturn(Optional.of(farm("12345")));
        GoatAbccPreviewResponseVO preview = preview("A-1", "1643218012", "12345");
        preview.setCreatorName("Capril Bocaina");
        when(goatAbccQueryUseCase.preview(eq(1L), any())).thenReturn(preview);
        when(goatManagementUseCase.createGoat(eq(1L), any(), eq(GoatCreationOrigin.ABCC_IMPORT)))
                .thenReturn(response("1643218012", "ANIMAL"));
        GoatRequestVO request = request("1643218012", "12345");

        business.confirm(1L, "A-1", request);

        assertThat(request.getCreatorProvenance().getCreatorNameSnapshot()).isEqualTo("Capril Bocaina");
        assertThat(request.getCreatorProvenance().getCreatorTod()).isEqualTo("12345");
        assertThat(request.getCreatorProvenance().getEvidenceReference()).isEqualTo("ABCC:A-1");
    }

    @Test
    void confirmRejectsRequestTodMismatchBeforeCreatingGoat() {
        when(goatFarmPort.findById(1L)).thenReturn(Optional.of(farm("12345")));
        when(goatAbccQueryUseCase.preview(eq(1L), any())).thenReturn(preview("A-1", "1643218012", "12345"));

        assertThatThrownBy(() -> business.confirm(1L, "A-1", request("1643218012", "99999")))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("TOD informado");
        verify(goatManagementUseCase, never()).createGoat(anyLong(), any(), any());
    }

    @Test
    void adminKeepsTodBypass() {
        when(currentPrincipalQuery.requireCurrent()).thenReturn(principal("ROLE_ADMIN"));
        when(goatFarmPort.findById(1L)).thenReturn(Optional.of(farm("12345")));
        when(goatAbccQueryUseCase.preview(eq(1L), any())).thenReturn(preview("A-1", "1643218012", "99999"));
        GoatRequestVO request = request("1643218012", "99999");
        GoatResponseVO expected = response("1643218012", "ANIMAL");
        when(goatManagementUseCase.createGoat(1L, request, GoatCreationOrigin.ABCC_IMPORT)).thenReturn(expected);

        assertThat(business.confirm(1L, "A-1", request)).isSameAs(expected);
    }

    @Test
    void batchKeepsBestEffortStatusesAndDuplicatePrecheck() {
        when(goatFarmPort.findById(1L)).thenReturn(Optional.of(farm("12345")));
        when(goatAbccQueryUseCase.preview(eq(1L), any())).thenAnswer(invocation -> {
            GoatAbccPreviewRequestVO req = invocation.getArgument(1);
            return switch (req.getExternalId()) {
                case "ok" -> preview("ok", "1111111111", "12345");
                case "wrong-tod" -> preview("wrong-tod", "2222222222", "99999");
                case "duplicate" -> preview("duplicate", "3333333333", "12345");
                default -> preview("invalid", null, "12345");
            };
        });
        when(goatReferenceQueryPort.findReferenceByRegistrationNumberAndFarmId("1111111111", 1L)).thenReturn(Optional.empty());
        when(goatReferenceQueryPort.findReferenceByRegistrationNumberAndFarmId("2222222222", 1L)).thenReturn(Optional.empty());
        when(goatReferenceQueryPort.findReferenceByRegistrationNumberAndFarmId("3333333333", 1L))
                .thenReturn(Optional.of(new GoatReference(new GoatId(33L), 1L, "3333333333", "DUPLICATE")));
        when(goatManagementUseCase.createGoat(eq(1L), any(), eq(GoatCreationOrigin.ABCC_IMPORT)))
                .thenReturn(response("1111111111", "OK"));

        var result = business.confirmBatch(1L, List.of(item("ok"), item("wrong-tod"), item("duplicate"), item("invalid")));

        assertThat(result.getTotalSelected()).isEqualTo(4);
        assertThat(result.getTotalImported()).isEqualTo(1);
        assertThat(result.getTotalSkippedDuplicate()).isEqualTo(1);
        assertThat(result.getTotalSkippedTodMismatch()).isEqualTo(1);
        assertThat(result.getTotalError()).isEqualTo(1);
        assertThat(result.getResults().stream().map(r -> r.getStatus()).toList())
                .containsExactly("IMPORTED", "SKIPPED_TOD_MISMATCH", "SKIPPED_DUPLICATE", "ERROR");
    }

    @Test
    void successfulBatchItemRequestsOnePreviewAndUsesItsSnapshotForRequestAndProvenance() {
        when(goatFarmPort.findById(1L)).thenReturn(Optional.of(farm("12345")));
        GoatAbccPreviewResponseVO snapshot = preview("A-1", "1111111111", "12345");
        snapshot.setName("SNAPSHOT A");
        snapshot.setCreatorName("CREATOR A");
        when(goatAbccQueryUseCase.preview(eq(1L), any())).thenReturn(snapshot);
        when(goatReferenceQueryPort.findReferenceByRegistrationNumberAndFarmId("1111111111", 1L)).thenReturn(Optional.empty());
        when(goatManagementUseCase.createGoat(eq(1L), any(), eq(GoatCreationOrigin.ABCC_IMPORT)))
                .thenReturn(response("1111111111", "SNAPSHOT A"));

        business.confirmBatch(1L, List.of(item("A-1")));

        ArgumentCaptor<GoatRequestVO> captor = ArgumentCaptor.forClass(GoatRequestVO.class);
        verify(goatManagementUseCase).createGoat(eq(1L), captor.capture(), eq(GoatCreationOrigin.ABCC_IMPORT));
        assertThat(captor.getValue().getName()).isEqualTo("SNAPSHOT A");
        assertThat(captor.getValue().getCreatorProvenance().getCreatorNameSnapshot()).isEqualTo("CREATOR A");
        verify(goatAbccQueryUseCase).preview(eq(1L), any(GoatAbccPreviewRequestVO.class));
    }

    @Test
    void batchStopsBeforeQueryWhenFarmTodMissingForNonAdmin() {
        when(goatFarmPort.findById(1L)).thenReturn(Optional.of(farm(null)));
        assertThatThrownBy(() -> business.confirmBatch(1L, List.of(item("A-1"))))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("não possui TOD configurado");
        verify(goatAbccQueryUseCase, never()).preview(anyLong(), any());
    }

    @Test
    void rejectsMissingConfirmPayload() {
        assertThatThrownBy(() -> business.confirm(1L, "A-1", null))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Dados do animal");
    }

    @Test
    void missingFarmTodAndInvalidExternalIdRemainBusinessRules() {
        when(goatFarmPort.findById(1L)).thenReturn(Optional.of(farm(null)));
        assertThatThrownBy(() -> business.confirm(1L, "A-1", request("1643218012", "12345")))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("não possui TOD configurado");
        verify(goatAbccQueryUseCase, never()).preview(anyLong(), any());
    }

    private AuthenticatedPrincipal principal(String role) {
        return new AuthenticatedPrincipal(1L, "alberto@example.com", "Alberto Vilar", Set.of(role));
    }

    private FarmRecord farm(String tod) {
        return new FarmRecord(1L, "Capril Vilar", tod, null, null, null, List.of(), null, null, null);
    }

    private GoatAbccPreviewResponseVO preview(String externalId, String registration, String tod) {
        return GoatAbccPreviewResponseVO.builder().externalId(externalId).registrationNumber(registration)
                .name("ANIMAL").gender(Gender.MACHO).breed(GoatBreed.ALPINA).color("CHAMOISEE")
                .birthDate(LocalDate.of(2018, 6, 27)).status(GoatStatus.ATIVO).tod(tod)
                .toe(registration == null ? null : registration.substring(5)).build();
    }

    private GoatRequestVO request(String registration, String tod) {
        GoatRequestVO request = new GoatRequestVO();
        request.setRegistrationNumber(registration);
        request.setName("ANIMAL");
        request.setGender(Gender.MACHO);
        request.setBreed(GoatBreed.ALPINA);
        request.setColor("CHAMOISEE");
        request.setBirthDate(LocalDate.of(2018, 6, 27));
        request.setStatus(GoatStatus.ATIVO);
        request.setTod(tod);
        request.setToe(registration.substring(5));
        return request;
    }

    private GoatAbccBatchConfirmItemVO item(String externalId) {
        return GoatAbccBatchConfirmItemVO.builder().externalId(externalId).build();
    }

    private GoatResponseVO response(String registration, String name) {
        GoatResponseVO response = new GoatResponseVO();
        response.setRegistrationNumber(registration);
        response.setName(name);
        return response;
    }
}
