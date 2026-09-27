package com.devmaster.goatfarm.goat.business;

import com.devmaster.goatfarm.application.core.business.common.EntityFinder;
import com.devmaster.goatfarm.config.exceptions.custom.BusinessRuleException;
import com.devmaster.goatfarm.config.exceptions.custom.ExternalServiceUnavailableException;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.farm.application.model.FarmRecord;
import com.devmaster.goatfarm.farm.application.ports.out.GoatFarmPersistencePort;
import com.devmaster.goatfarm.goat.application.ports.in.GoatAbccQueryUseCase;
import com.devmaster.goatfarm.goat.application.ports.out.GoatAbccPublicQueryPort;
import com.devmaster.goatfarm.goat.business.abcc.AbccAnimalTranslator;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccPreviewRequestVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccRaceOptionVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccRawPreviewVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccRawSearchItemVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccRawSearchResultVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccRegistrationLookupRequestVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccSearchRequestVO;
import com.devmaster.goatfarm.goat.enums.GoatBreed;
import com.devmaster.goatfarm.goat.enums.GoatStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GoatAbccQueryBusinessTest {

    @Mock private GoatAbccPublicQueryPort abccPublicQueryPort;
    @Mock private GoatFarmPersistencePort goatFarmPort;
    @Mock private EntityFinder entityFinder;

    private GoatAbccQueryUseCase business;

    @BeforeEach
    void setUp() {
        business = new GoatAbccQueryBusiness(abccPublicQueryPort, goatFarmPort, entityFinder,
                new AbccAnimalTranslator());
        lenient().when(entityFinder.findOrThrow(any(), any())).thenAnswer(invocation -> {
            Supplier<Optional<?>> supplier = invocation.getArgument(0);
            String message = invocation.getArgument(1);
            return supplier.get().orElseThrow(() -> new ResourceNotFoundException(message));
        });
    }

    @Test
    void listRacesNormalizesNamesAndBreed() {
        when(abccPublicQueryPort.listRaces()).thenReturn(List.of(
                GoatAbccRaceOptionVO.builder().id(9).name(" Saanen ").build()));

        var races = business.listRaces(1L);

        assertThat(races).singleElement().satisfies(race -> {
            assertThat(race.getName()).isEqualTo("Saanen");
            assertThat(race.getNormalizedBreed()).isEqualTo(GoatBreed.SAANEN);
        });
    }

    @Test
    void searchIsPublicDoesNotFilterByFarmTodAndPreservesRequestedTod() {
        when(abccPublicQueryPort.search(any())).thenReturn(GoatAbccRawSearchResultVO.builder()
                .currentPage(1).totalPages(1).items(List.of(
                        searchItem("A-1", "12345", "00001", "SAANEN"),
                        searchItem("A-2", "99999", "00002", "SAANEN"))).build());

        var response = business.search(1L, GoatAbccSearchRequestVO.builder()
                .raceId(9).affix("CAPRIL VILAR").tod("12345").page(1).build());

        assertThat(response.getItems()).hasSize(2);
        ArgumentCaptor<com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccSearchRequestVO> captor =
                ArgumentCaptor.forClass(com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccSearchRequestVO.class);
        verify(abccPublicQueryPort).search(captor.capture());
        assertThat(captor.getValue().getTod()).isEqualTo("12345");
        verify(goatFarmPort, never()).findById(any());
    }

    @Test
    void searchResolvesRaceNameWithoutLocalFarmAndNormalizesResponse() {
        when(abccPublicQueryPort.listRaces()).thenReturn(List.of(
                GoatAbccRaceOptionVO.builder().id(9).name("SAANEN").build()));
        when(abccPublicQueryPort.search(any())).thenReturn(GoatAbccRawSearchResultVO.builder()
                .currentPage(1).totalPages(1).items(List.of(searchItem("A-1", "12345", "00001", "SAANEN")))
                .build());

        var response = business.search(1L, GoatAbccSearchRequestVO.builder()
                .raceName("Saanen").affix("CRS").page(1).build());

        assertThat(response.getItems()).singleElement().satisfies(item ->
                assertThat(item.getNormalizedBreed()).isEqualTo(GoatBreed.SAANEN));
        verify(abccPublicQueryPort).listRaces();
        verify(goatFarmPort, never()).findById(any());
    }

    @Test
    void searchRejectsMissingAffixBeforeCallingAbcc() {
        assertThatThrownBy(() -> business.search(1L,
                GoatAbccSearchRequestVO.builder().raceId(9).build()))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Afixo é obrigatório");
        verify(abccPublicQueryPort, never()).search(any());
    }

    @Test
    void searchTranslatesUnexpectedPublicAbccFailure() {
        when(abccPublicQueryPort.search(any())).thenThrow(new RuntimeException("malformed response"));

        assertThatThrownBy(() -> business.search(1L,
                GoatAbccSearchRequestVO.builder().raceId(9).affix("CRS").build()))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Não foi possível consultar a ABCC pública");
    }

    @Test
    void previewLoadsFarmAndDoesNotApplyTodFilterOrRequireCurrentUser() {
        when(goatFarmPort.findById(1L)).thenReturn(Optional.of(farm()));
        when(abccPublicQueryPort.preview("A-1")).thenReturn(rawPreview("A-1", "9999900001", "99999", "RGD"));

        var response = business.preview(1L, GoatAbccPreviewRequestVO.builder().externalId("A-1").build());

        assertThat(response.getTod()).isEqualTo("99999");
        assertThat(response.getFarmName()).isEqualTo("Capril Vilar");
        assertThat(response.getUserName()).isNull();
    }

    @Test
    void previewMapsSemRgdAsActive() {
        when(goatFarmPort.findById(1L)).thenReturn(Optional.of(farm()));
        when(abccPublicQueryPort.preview("A-1")).thenReturn(rawPreview("A-1", "1234500001", "12345", "Sem RGD"));

        var response = business.preview(1L, GoatAbccPreviewRequestVO.builder().externalId("A-1").build());

        assertThat(response.getStatus()).isEqualTo(GoatStatus.ATIVO);
        assertThat(response.getNormalizationWarnings()).isEmpty();
    }

    @Test
    void previewTranslatesUnexpectedAbccFailure() {
        when(goatFarmPort.findById(1L)).thenReturn(Optional.of(farm()));
        when(abccPublicQueryPort.preview("A-1")).thenThrow(new RuntimeException("timeout"));

        assertThatThrownBy(() -> business.preview(1L, GoatAbccPreviewRequestVO.builder().externalId("A-1").build()))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Não foi possível obter o preview");
    }

    @Test
    void lookupReturnsFoundPreviewWithoutPersisting() {
        givenRaceAndCandidate("SAANEN");
        when(goatFarmPort.findById(1L)).thenReturn(Optional.of(farm()));
        when(abccPublicQueryPort.preview("A-1")).thenReturn(rawPreview("A-1", "1234567890", "12345", "RGD"));

        var result = business.lookupByRegistration(1L, lookupRequest());

        assertThat(result.getStatus()).isEqualTo("FOUND");
        assertThat(result.getPreview().getRegistrationNumber()).isEqualTo("1234567890");
    }

    @Test
    void lookupReturnsNotFoundWhenRegistrationIsAbsentOrRaceDoesNotMatch() {
        when(abccPublicQueryPort.listRaces()).thenReturn(List.of(race("SAANEN")));
        when(abccPublicQueryPort.searchByRegistration(9, "1234567890")).thenReturn(
                GoatAbccRawSearchResultVO.builder().items(List.of(searchItem("A-1", "12345", "67890", "BOER"))).build());

        var result = business.lookupByRegistration(1L, lookupRequest());

        assertThat(result.getStatus()).isEqualTo("NOT_FOUND");
        verify(abccPublicQueryPort, never()).preview(any());
    }

    @Test
    void lookupReturnsAmbiguousInsteadOfSelectingFirstCandidate() {
        when(abccPublicQueryPort.listRaces()).thenReturn(List.of(race("SAANEN")));
        when(abccPublicQueryPort.searchByRegistration(9, "1234567890")).thenReturn(
                GoatAbccRawSearchResultVO.builder().items(List.of(
                        searchItem("A-1", "12345", "67890", "SAANEN"),
                        searchItem("A-2", "12345", "67890", "SAANEN"))).build());

        var result = business.lookupByRegistration(1L, lookupRequest());

        assertThat(result.getStatus()).isEqualTo("AMBIGUOUS");
        assertThat(result.getCandidates()).hasSize(2);
        verify(abccPublicQueryPort, never()).preview(any());
    }

    @Test
    void lookupRejectsPreviewWithDifferentRegistrationOrBreed() {
        givenRaceAndCandidate("SAANEN");
        when(goatFarmPort.findById(1L)).thenReturn(Optional.of(farm()));
        when(abccPublicQueryPort.preview("A-1")).thenReturn(rawPreview("A-1", "9999999999", "12345", "BOER"));

        assertThatThrownBy(() -> business.lookupByRegistration(1L, lookupRequest()))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("registro divergente");
    }

    @Test
    void lookupRejectsPreviewWithDifferentBreed() {
        givenRaceAndCandidate("SAANEN");
        when(goatFarmPort.findById(1L)).thenReturn(Optional.of(farm()));
        when(abccPublicQueryPort.preview("A-1"))
                .thenReturn(rawPreview("A-1", "1234567890", "12345", "BOER", "BOER"));

        assertThatThrownBy(() -> business.lookupByRegistration(1L, lookupRequest()))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("raça divergente");
    }

    @Test
    void lookupRejectsCandidateWithoutExternalId() {
        when(abccPublicQueryPort.listRaces()).thenReturn(List.of(race("SAANEN")));
        when(abccPublicQueryPort.searchByRegistration(9, "1234567890")).thenReturn(
                GoatAbccRawSearchResultVO.builder().items(List.of(
                        GoatAbccRawSearchItemVO.builder().tod("12345").toe("67890").raca("SAANEN").build()))
                        .build());

        assertThatThrownBy(() -> business.lookupByRegistration(1L, lookupRequest()))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("sem identificador externo");
    }

    @Test
    void lookupPropagatesExternalUnavailableException() {
        when(abccPublicQueryPort.listRaces()).thenReturn(List.of(race("SAANEN")));
        var failure = new ExternalServiceUnavailableException("ABCC indisponível", new RuntimeException("timeout"));
        when(abccPublicQueryPort.searchByRegistration(9, "1234567890")).thenThrow(failure);

        assertThatThrownBy(() -> business.lookupByRegistration(1L, lookupRequest())).isSameAs(failure);
    }

    @Test
    void lookupTranslatesUnexpectedAbccFailure() {
        when(abccPublicQueryPort.listRaces()).thenReturn(List.of(race("SAANEN")));
        when(abccPublicQueryPort.searchByRegistration(9, "1234567890"))
                .thenThrow(new RuntimeException("malformed response"));

        assertThatThrownBy(() -> business.lookupByRegistration(1L, lookupRequest()))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Não foi possível consultar a ABCC pública");
    }

    private void givenRaceAndCandidate(String selectedRace) {
        when(abccPublicQueryPort.listRaces()).thenReturn(List.of(race(selectedRace)));
        when(abccPublicQueryPort.searchByRegistration(9, "1234567890")).thenReturn(
                GoatAbccRawSearchResultVO.builder().items(List.of(
                        GoatAbccRawSearchItemVO.builder().externalId("A-1").tod("12345").toe("67890")
                                .raca("SAANEN").build())).build());
    }

    private GoatAbccRegistrationLookupRequestVO lookupRequest() {
        return GoatAbccRegistrationLookupRequestVO.builder().raceId(9).registrationNumber("12345 67890").build();
    }

    private GoatAbccRaceOptionVO race(String name) {
        return GoatAbccRaceOptionVO.builder().id(9).name(name).build();
    }

    private GoatAbccRawSearchItemVO searchItem(String id, String tod, String toe, String breed) {
        return GoatAbccRawSearchItemVO.builder().externalId(id).nome("ANIMAL").situacao("RGD")
                .sexo("Macho").raca(breed).tod(tod).toe(toe).build();
    }

    private GoatAbccRawPreviewVO rawPreview(String id, String registration, String tod, String status) {
        return rawPreview(id, registration, tod, status, "SAANEN");
    }

    private GoatAbccRawPreviewVO rawPreview(String id, String registration, String tod, String status, String breed) {
        return GoatAbccRawPreviewVO.builder().externalId(id).registro(registration).nome("ANIMAL")
                .sexo("Macho").raca(breed).pelagem("BRANCA")
                .situacao(status).categoria("PO").dataNascimento("10/01/2020")
                .tod(tod).toe(registration.substring(5)).build();
    }

    private FarmRecord farm() {
        return new FarmRecord(1L, "Capril Vilar", "12345", null, null, null, List.of(), null, null, null);
    }
}
