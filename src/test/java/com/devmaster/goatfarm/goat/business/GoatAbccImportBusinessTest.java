package com.devmaster.goatfarm.goat.business;

import com.devmaster.goatfarm.application.core.business.common.EntityFinder;
import com.devmaster.goatfarm.authority.persistence.entity.User;
import com.devmaster.goatfarm.config.exceptions.custom.BusinessRuleException;
import com.devmaster.goatfarm.config.security.OwnershipService;
import com.devmaster.goatfarm.farm.application.ports.out.GoatFarmPersistencePort;
import com.devmaster.goatfarm.farm.persistence.entity.GoatFarm;
import com.devmaster.goatfarm.goat.application.ports.in.GoatManagementUseCase;
import com.devmaster.goatfarm.goat.application.ports.out.GoatAbccPublicQueryPort;
import com.devmaster.goatfarm.goat.application.ports.out.GoatPersistencePort;
import com.devmaster.goatfarm.goat.business.bo.GoatRequestVO;
import com.devmaster.goatfarm.goat.business.bo.GoatResponseVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccBatchConfirmItemVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccPreviewRequestVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccRaceOptionVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccRawPreviewVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccRawSearchItemVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccRawSearchResultVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccSearchRequestVO;
import com.devmaster.goatfarm.goat.enums.Gender;
import com.devmaster.goatfarm.goat.enums.GoatBreed;
import com.devmaster.goatfarm.goat.enums.GoatStatus;
import com.devmaster.goatfarm.goat.persistence.entity.Goat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GoatAbccImportBusinessTest {

    @Mock
    private OwnershipService ownershipService;
    @Mock
    private GoatFarmPersistencePort goatFarmPort;
    @Mock
    private GoatAbccPublicQueryPort abccPublicQueryPort;
    @Mock
    private GoatManagementUseCase goatManagementUseCase;
    @Mock
    private GoatPersistencePort goatPersistencePort;
    @Mock
    private EntityFinder entityFinder;

    private GoatAbccImportBusiness business;

    @BeforeEach
    void setUp() {
        business = new GoatAbccImportBusiness(
                ownershipService,
                goatFarmPort,
                abccPublicQueryPort,
                goatManagementUseCase,
                goatPersistencePort,
                entityFinder
        );

        lenient().when(entityFinder.findOrThrow(any(), any())).thenAnswer(invocation -> {
            java.util.function.Supplier<Optional<?>> supplier = invocation.getArgument(0);
            String errorMsg = invocation.getArgument(1);
            return supplier.get().orElseThrow(() -> new com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException(errorMsg));
        });

        lenient().doNothing().when(ownershipService).verifyFarmOwnership(1L);
        lenient().when(ownershipService.getCurrentUser()).thenReturn(buildUser("Alberto Vilar"));
    }

    @Test
    void shouldSearchAndKeepOnlyFarmTodForNonAdmin() {
        when(ownershipService.isCurrentUserAdmin()).thenReturn(false);
        when(goatFarmPort.findById(1L)).thenReturn(Optional.of(buildFarm(1L, "Capril Vilar", "12345")));

        when(abccPublicQueryPort.search(any())).thenReturn(GoatAbccRawSearchResultVO.builder()
                .currentPage(1)
                .totalPages(3)
                .items(List.of(
                        GoatAbccRawSearchItemVO.builder()
                                .externalId("A-001")
                                .nome("DO MESMO TOD")
                                .situacao("RGD")
                                .sexo("Macho")
                                .raca("SAANEN")
                                .tod("12345")
                                .toe("00001")
                                .build(),
                        GoatAbccRawSearchItemVO.builder()
                                .externalId("A-002")
                                .nome("TOD DIFERENTE")
                                .situacao("RGD")
                                .sexo("Macho")
                                .raca("SAANEN")
                                .tod("99999")
                                .toe("00002")
                                .build()
                ))
                .build());

        var response = business.search(1L, GoatAbccSearchRequestVO.builder()
                .raceId(9)
                .affix("CAPRIL VILAR")
                .page(1)
                .build());

        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().getFirst().getExternalId()).isEqualTo("A-001");

        ArgumentCaptor<GoatAbccSearchRequestVO> requestCaptor = ArgumentCaptor.forClass(GoatAbccSearchRequestVO.class);
        verify(abccPublicQueryPort).search(requestCaptor.capture());
        assertThat(requestCaptor.getValue().getTod()).isEqualTo("12345");
    }

    @Test
    void shouldBlockSearchWhenFarmTodIsMissingForNonAdmin() {
        when(ownershipService.isCurrentUserAdmin()).thenReturn(false);
        when(goatFarmPort.findById(1L)).thenReturn(Optional.of(buildFarm(1L, "Capril Vilar", null)));

        assertThatThrownBy(() -> business.search(1L, GoatAbccSearchRequestVO.builder()
                .raceId(9)
                .affix("CAPRIL VILAR")
                .page(1)
                .build()))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("não possui TOD configurado");

        verify(abccPublicQueryPort, never()).search(any());
    }

    @Test
    void shouldAllowSearchAnyTodForAdmin() {
        when(ownershipService.isCurrentUserAdmin()).thenReturn(true);
        when(goatFarmPort.findById(1L)).thenReturn(Optional.of(buildFarm(1L, "Capril Vilar", null)));

        when(abccPublicQueryPort.search(any())).thenReturn(GoatAbccRawSearchResultVO.builder()
                .currentPage(1)
                .totalPages(1)
                .items(List.of(
                        GoatAbccRawSearchItemVO.builder().externalId("A-001").nome("UM").situacao("RGD").sexo("Macho").raca("SAANEN").tod("11111").build(),
                        GoatAbccRawSearchItemVO.builder().externalId("A-002").nome("DOIS").situacao("RGD").sexo("Macho").raca("SAANEN").tod("99999").build()
                ))
                .build());

        var response = business.search(1L, GoatAbccSearchRequestVO.builder()
                .raceId(9)
                .affix("CAPRIL VILAR")
                .page(1)
                .build());

        assertThat(response.getItems()).hasSize(2);
    }

    @Test
    void shouldResolveRaceIdByRaceNameWhenRaceIdIsNotProvided() {
        when(ownershipService.isCurrentUserAdmin()).thenReturn(true);
        when(goatFarmPort.findById(1L)).thenReturn(Optional.of(buildFarm(1L, "Capril Vilar", null)));
        when(abccPublicQueryPort.listRaces()).thenReturn(List.of(
                GoatAbccRaceOptionVO.builder().id(9).name("SAANEN").build(),
                GoatAbccRaceOptionVO.builder().id(2).name("BOER").build()
        ));
        when(abccPublicQueryPort.search(any())).thenReturn(GoatAbccRawSearchResultVO.builder()
                .currentPage(1)
                .totalPages(1)
                .items(List.of())
                .build());

        business.search(1L, GoatAbccSearchRequestVO.builder()
                .raceName("Saanen")
                .affix("CRS")
                .page(1)
                .build());

        verify(abccPublicQueryPort).listRaces();
        verify(abccPublicQueryPort).search(any());
    }

    @Test
    void shouldBlockPreviewWhenTodDoesNotMatchForNonAdmin() {
        when(ownershipService.isCurrentUserAdmin()).thenReturn(false);
        when(goatFarmPort.findById(1L)).thenReturn(Optional.of(buildFarm(1L, "Capril Vilar", "12345")));
        when(abccPublicQueryPort.preview("A-001")).thenReturn(
                buildRawPreview("A-001", "1111111111", "ANIMAL", "99999", "00001")
        );

        assertThatThrownBy(() -> business.preview(1L, GoatAbccPreviewRequestVO.builder().externalId("A-001").build()))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("TOD diferente");
    }

    @Test
    void shouldAllowPreviewWithDifferentTodForAdmin() {
        when(ownershipService.isCurrentUserAdmin()).thenReturn(true);
        when(goatFarmPort.findById(1L)).thenReturn(Optional.of(buildFarm(1L, "Capril Vilar", "12345")));
        when(abccPublicQueryPort.preview("A-001")).thenReturn(
                buildRawPreview("A-001", "1111111111", "ANIMAL", "99999", "00001")
        );

        var response = business.preview(1L, GoatAbccPreviewRequestVO.builder().externalId("A-001").build());

        assertThat(response.getTod()).isEqualTo("99999");
        assertThat(response.getRegistrationNumber()).isEqualTo("1111111111");
    }

    @Test
    void shouldConfirmByReusingGoatCreateFlowWhenTodMatches() {
        when(ownershipService.isCurrentUserAdmin()).thenReturn(false);
        when(goatFarmPort.findById(1L)).thenReturn(Optional.of(buildFarm(1L, "Capril Vilar", "12345")));
        when(abccPublicQueryPort.preview("A-001")).thenReturn(
                buildRawPreview("A-001", "1643218012", "XEQUE V", "12345", "18012")
        );

        GoatRequestVO requestVO = new GoatRequestVO();
        requestVO.setRegistrationNumber("1643218012");
        requestVO.setName("XEQUE V DO CAPRIL VILAR");
        requestVO.setGender(Gender.MACHO);
        requestVO.setBreed(GoatBreed.ALPINA);
        requestVO.setColor("CHAMOISEE");
        requestVO.setBirthDate(LocalDate.of(2018, 6, 27));
        requestVO.setStatus(GoatStatus.ATIVO);
        requestVO.setTod("12345");
        requestVO.setToe("18012");

        GoatResponseVO expected = new GoatResponseVO();
        expected.setRegistrationNumber("1643218012");

        when(goatManagementUseCase.createGoat(1L, requestVO)).thenReturn(expected);

        GoatResponseVO response = business.confirm(1L, "A-001", requestVO);

        assertThat(response).isSameAs(expected);
        verify(goatManagementUseCase).createGoat(1L, requestVO);
    }

    @Test
    void shouldBlockConfirmWhenRequestTodDoesNotMatchFarmTod() {
        when(ownershipService.isCurrentUserAdmin()).thenReturn(false);
        when(goatFarmPort.findById(1L)).thenReturn(Optional.of(buildFarm(1L, "Capril Vilar", "12345")));
        when(abccPublicQueryPort.preview("A-001")).thenReturn(
                buildRawPreview("A-001", "1643218012", "XEQUE V", "12345", "18012")
        );

        GoatRequestVO requestVO = new GoatRequestVO();
        requestVO.setRegistrationNumber("1643218012");
        requestVO.setName("XEQUE V DO CAPRIL VILAR");
        requestVO.setGender(Gender.MACHO);
        requestVO.setBreed(GoatBreed.ALPINA);
        requestVO.setColor("CHAMOISEE");
        requestVO.setBirthDate(LocalDate.of(2018, 6, 27));
        requestVO.setStatus(GoatStatus.ATIVO);
        requestVO.setTod("99999");
        requestVO.setToe("18012");

        assertThatThrownBy(() -> business.confirm(1L, "A-001", requestVO))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("TOD informado");

        verify(goatManagementUseCase, never()).createGoat(eq(1L), any(GoatRequestVO.class));
    }

    @Test
    void shouldAllowConfirmWithDifferentTodForAdmin() {
        when(ownershipService.isCurrentUserAdmin()).thenReturn(true);
        when(goatFarmPort.findById(1L)).thenReturn(Optional.of(buildFarm(1L, "Capril Vilar", "12345")));
        when(abccPublicQueryPort.preview("A-001")).thenReturn(
                buildRawPreview("A-001", "1643218012", "XEQUE V", "99999", "18012")
        );

        GoatRequestVO requestVO = new GoatRequestVO();
        requestVO.setRegistrationNumber("1643218012");
        requestVO.setName("XEQUE V DO CAPRIL VILAR");
        requestVO.setGender(Gender.MACHO);
        requestVO.setBreed(GoatBreed.ALPINA);
        requestVO.setColor("CHAMOISEE");
        requestVO.setBirthDate(LocalDate.of(2018, 6, 27));
        requestVO.setStatus(GoatStatus.ATIVO);
        requestVO.setTod("99999");
        requestVO.setToe("18012");

        GoatResponseVO expected = new GoatResponseVO();
        expected.setRegistrationNumber("1643218012");
        when(goatManagementUseCase.createGoat(1L, requestVO)).thenReturn(expected);

        GoatResponseVO response = business.confirm(1L, "A-001", requestVO);

        assertThat(response).isSameAs(expected);
    }

    @Test
    void shouldConfirmBatchWithDuplicateAndTodMismatchWithoutFailingWholeBatch() {
        when(ownershipService.isCurrentUserAdmin()).thenReturn(false);
        when(goatFarmPort.findById(1L)).thenReturn(Optional.of(buildFarm(1L, "Capril Vilar", "12345")));

        when(abccPublicQueryPort.preview("A-001")).thenReturn(
                buildRawPreview("A-001", "1111111111", "IMPORTAVEL", "12345", "11111")
        );
        when(abccPublicQueryPort.preview("A-002")).thenReturn(
                buildRawPreview("A-002", "2222222222", "TOD DIFERENTE", "99999", "22222")
        );
        when(abccPublicQueryPort.preview("A-003")).thenReturn(
                buildRawPreview("A-003", "3333333333", "DUPLICADA", "12345", "33333")
        );
        when(abccPublicQueryPort.preview("A-004")).thenReturn(
                buildRawPreview("A-004", null, "INVALIDA", "12345", "44444")
        );

        when(goatPersistencePort.findByIdAndFarmId("1111111111", 1L)).thenReturn(Optional.empty());
        when(goatPersistencePort.findByIdAndFarmId("3333333333", 1L)).thenReturn(Optional.of(new Goat()));

        GoatResponseVO created = new GoatResponseVO();
        created.setRegistrationNumber("1111111111");
        created.setName("IMPORTAVEL");
        when(goatManagementUseCase.createGoat(eq(1L), any(GoatRequestVO.class))).thenReturn(created);

        var response = business.confirmBatch(1L, List.of(
                GoatAbccBatchConfirmItemVO.builder().externalId("A-001").build(),
                GoatAbccBatchConfirmItemVO.builder().externalId("A-002").build(),
                GoatAbccBatchConfirmItemVO.builder().externalId("A-003").build(),
                GoatAbccBatchConfirmItemVO.builder().externalId("A-004").build()
        ));

        assertThat(response.getTotalSelected()).isEqualTo(4);
        assertThat(response.getTotalImported()).isEqualTo(1);
        assertThat(response.getTotalSkippedDuplicate()).isEqualTo(1);
        assertThat(response.getTotalSkippedTodMismatch()).isEqualTo(1);
        assertThat(response.getTotalError()).isEqualTo(1);
        assertThat(response.getResults().stream().map(r -> r.getStatus()).toList())
                .containsExactly("IMPORTED", "SKIPPED_TOD_MISMATCH", "SKIPPED_DUPLICATE", "ERROR");
    }

    @Test
    void shouldBlockBatchWhenFarmTodIsMissingForNonAdmin() {
        when(ownershipService.isCurrentUserAdmin()).thenReturn(false);
        when(goatFarmPort.findById(1L)).thenReturn(Optional.of(buildFarm(1L, "Capril Vilar", null)));

        assertThatThrownBy(() -> business.confirmBatch(1L, List.of(
                GoatAbccBatchConfirmItemVO.builder().externalId("A-001").build()
        )))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("não possui TOD configurado");
    }

    @Test
    void shouldReturnBusinessRuleWhenConfirmPayloadIsMissing() {
        assertThatThrownBy(() -> business.confirm(1L, "4044", null))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Dados do animal");
    }

    private GoatFarm buildFarm(Long farmId, String name, String tod) {
        GoatFarm farm = new GoatFarm();
        farm.setId(farmId);
        farm.setName(name);
        farm.setTod(tod);
        return farm;
    }

    private User buildUser(String name) {
        User user = new User();
        user.setName(name);
        return user;
    }

    private GoatAbccRawPreviewVO buildRawPreview(String externalId, String registro, String nome, String tod, String toe) {
        return GoatAbccRawPreviewVO.builder()
                .externalId(externalId)
                .registro(registro)
                .nome(nome)
                .sexo("Macho")
                .raca("SAANEN")
                .pelagem("BRANCA")
                .situacao("RGD")
                .categoria("PO")
                .dataNascimento("10/01/2020")
                .tod(tod)
                .toe(toe)
                .build();
    }
}
