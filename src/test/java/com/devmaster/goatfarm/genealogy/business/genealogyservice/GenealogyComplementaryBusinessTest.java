package com.devmaster.goatfarm.genealogy.business.genealogyservice;

import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.genealogy.application.ports.out.GenealogyAbccQueryPort;
import com.devmaster.goatfarm.genealogy.business.bo.GenealogyAbccSnapshotVO;
import com.devmaster.goatfarm.genealogy.business.bo.GenealogyNodeSource;
import com.devmaster.goatfarm.goat.application.ports.in.GoatGenealogyReadUseCase;
import com.devmaster.goatfarm.goat.application.model.GoatGenealogySnapshot;
import com.devmaster.goatfarm.goat.domain.GoatId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GenealogyComplementaryBusinessTest {

    @Mock
    private GoatGenealogyReadUseCase goatGenealogyQueryPort;
    @Mock
    private GenealogyAbccQueryPort genealogyAbccQueryPort;

    @InjectMocks
    private GenealogyComplementaryBusiness business;

    @Test
    void shouldReturnFoundAndComplementMissingNodesFromAbcc() {
        GoatGenealogySnapshot mother = goat(21L, "2114517012", "NAIDE", null, null);
        GoatGenealogySnapshot goat = goat(11L, "1643218012", "XEQUE", null,
                GoatGenealogySnapshot.ParentReference.local(mother));

        when(goatGenealogyQueryPort.findGenealogyByRegistrationNumberAndFarmId("1643218012", 1L))
                .thenReturn(Optional.of(goat));
        when(genealogyAbccQueryPort.findGenealogyByRegistrationNumber("1643218012"))
                .thenReturn(Optional.of(GenealogyAbccSnapshotVO.builder()
                        .animalRegistrationNumber("1643218012")
                        .animalName("XEQUE V DO CAPRIL VILAR")
                        .fatherRegistrationNumber("1635717065")
                        .fatherName("C.V.C SIGNOS PETROLEO")
                        .maternalGrandfatherRegistrationNumber("123")
                        .maternalGrandfatherName("AVÔ MAT")
                        .build()));

        var response = business.findComplementaryGenealogy(1L, "1643218012");

        assertThat(response.getIntegration().getStatus()).isEqualTo("FOUND");
        assertThat(response.getPai().getSource()).isEqualTo(GenealogyNodeSource.ABCC);
        assertThat(response.getMae().getSource()).isEqualTo(GenealogyNodeSource.LOCAL);
        assertThat(response.getMae().getLocalTechnicalGoatId()).isEqualTo(21L);
        assertThat(response.getAvoMaterno().getSource()).isEqualTo(GenealogyNodeSource.ABCC);
        assertThat(response.getAvoPaterno().getSource()).isEqualTo(GenealogyNodeSource.AUSENTE);
    }

    @Test
    void shouldReturnInsufficientDataWhenLocalRegistrationIsMissing() {
        GoatGenealogySnapshot goat = goat(1L, "  ", "SEM REGISTRO", null, null);
        when(goatGenealogyQueryPort.findGenealogyByRegistrationNumberAndFarmId("g1", 1L))
                .thenReturn(Optional.of(goat));

        var response = business.findComplementaryGenealogy(1L, "g1");

        assertThat(response.getIntegration().getStatus()).isEqualTo("INSUFFICIENT_DATA");
        verify(genealogyAbccQueryPort, never()).findGenealogyByRegistrationNumber(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void shouldReturnNotFoundWhenAbccDoesNotFindRegistration() {
        GoatGenealogySnapshot goat = goat(1L, "1643218012", "XEQUE", null, null);
        when(goatGenealogyQueryPort.findGenealogyByRegistrationNumberAndFarmId("1643218012", 1L))
                .thenReturn(Optional.of(goat));
        when(genealogyAbccQueryPort.findGenealogyByRegistrationNumber("1643218012"))
                .thenReturn(Optional.empty());

        var response = business.findComplementaryGenealogy(1L, "1643218012");

        assertThat(response.getIntegration().getStatus()).isEqualTo("NOT_FOUND");
    }

    @Test
    void shouldExposeAnUnknownExternalFatherAsDeclaredInsteadOfAbccValidated() {
        GoatGenealogySnapshot goat = goat(1L, "KID-001", "Cria",
                GoatGenealogySnapshot.ParentReference.external("1635719026A"), null);
        when(goatGenealogyQueryPort.findGenealogyByRegistrationNumberAndFarmId("KID-001", 1L))
                .thenReturn(Optional.of(goat));
        when(genealogyAbccQueryPort.findGenealogyByRegistrationNumber("KID-001"))
                .thenReturn(Optional.empty());
        when(genealogyAbccQueryPort.findGenealogyByRegistrationNumber("1635719026A"))
                .thenReturn(Optional.empty());

        var response = business.findComplementaryGenealogy(1L, "KID-001");

        assertThat(response.getPai().getRegistrationNumber()).isEqualTo("1635719026A");
        assertThat(response.getPai().getSource()).isEqualTo(GenealogyNodeSource.DECLARADO);
    }

    @Test
    void shouldExposeAnAbccValidatedExternalFatherWithoutCreatingALocalGoat() {
        GoatGenealogySnapshot goat = goat(1L, "KID-001", "Cria",
                GoatGenealogySnapshot.ParentReference.external("1635719026A"), null);
        when(goatGenealogyQueryPort.findGenealogyByRegistrationNumberAndFarmId("KID-001", 1L))
                .thenReturn(Optional.of(goat));
        when(genealogyAbccQueryPort.findGenealogyByRegistrationNumber("KID-001"))
                .thenReturn(Optional.empty());
        when(genealogyAbccQueryPort.findGenealogyByRegistrationNumber("1635719026A"))
                .thenReturn(Optional.of(GenealogyAbccSnapshotVO.builder()
                        .animalRegistrationNumber("1635719026A")
                        .animalName("REPRODUTOR EXTERNO")
                        .build()));

        var response = business.findComplementaryGenealogy(1L, "KID-001");

        assertThat(response.getPai().getName()).isEqualTo("REPRODUTOR EXTERNO");
        assertThat(response.getPai().getRegistrationNumber()).isEqualTo("1635719026A");
        assertThat(response.getPai().getSource()).isEqualTo(GenealogyNodeSource.ABCC);
        assertThat(response.getPai().getLocalGoatId()).isNull();
        assertThat(response.getPai().getLocalTechnicalGoatId()).isNull();
    }

    @Test
    void shouldReturnUnavailableWhenAbccPortFails() {
        GoatGenealogySnapshot goat = goat(1L, "1643218012", "XEQUE", null, null);
        when(goatGenealogyQueryPort.findGenealogyByRegistrationNumberAndFarmId("1643218012", 1L))
                .thenReturn(Optional.of(goat));
        when(genealogyAbccQueryPort.findGenealogyByRegistrationNumber("1643218012"))
                .thenThrow(new RuntimeException("ABCC down"));

        var response = business.findComplementaryGenealogy(1L, "1643218012");

        assertThat(response.getIntegration().getStatus()).isEqualTo("UNAVAILABLE");
        assertThat(response.getIntegration().getMessage()).contains("ABCC");
    }

    @Test
    void shouldThrowWhenGoatIsNotFound() {
        when(goatGenealogyQueryPort.findGenealogyByRegistrationNumberAndFarmId("999", 1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> business.findComplementaryGenealogy(1L, "999"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private GoatGenealogySnapshot goat(
            Long technicalId,
            String registrationNumber,
            String name,
            GoatGenealogySnapshot.ParentReference father,
            GoatGenealogySnapshot.ParentReference mother
    ) {
        return new GoatGenealogySnapshot(new GoatId(technicalId), registrationNumber, name,
                null, null, null, null, null, null, null, null, null, null, father, mother);
    }
}
