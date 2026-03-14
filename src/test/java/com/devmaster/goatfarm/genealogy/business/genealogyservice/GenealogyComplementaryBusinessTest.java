package com.devmaster.goatfarm.genealogy.business.genealogyservice;

import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.genealogy.application.ports.out.GenealogyAbccQueryPort;
import com.devmaster.goatfarm.genealogy.business.bo.GenealogyAbccSnapshotVO;
import com.devmaster.goatfarm.genealogy.business.bo.GenealogyNodeSource;
import com.devmaster.goatfarm.goat.application.ports.out.GoatGenealogyQueryPort;
import com.devmaster.goatfarm.goat.persistence.entity.Goat;
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
    private GoatGenealogyQueryPort goatGenealogyQueryPort;
    @Mock
    private GenealogyAbccQueryPort genealogyAbccQueryPort;

    @InjectMocks
    private GenealogyComplementaryBusiness business;

    @Test
    void shouldReturnFoundAndComplementMissingNodesFromAbcc() {
        Goat goat = new Goat();
        goat.setRegistrationNumber("1643218012");
        goat.setName("XEQUE");

        Goat mother = new Goat();
        mother.setRegistrationNumber("2114517012");
        mother.setName("NAIDE");
        goat.setMother(mother);

        when(goatGenealogyQueryPort.findByIdAndFarmIdWithFamilyGraph("1643218012", 1L))
                .thenReturn(Optional.of(goat));

        when(genealogyAbccQueryPort.findGenealogyByRegistrationNumber("1643218012"))
                .thenReturn(Optional.of(
                        GenealogyAbccSnapshotVO.builder()
                                .animalRegistrationNumber("1643218012")
                                .animalName("XEQUE V DO CAPRIL VILAR")
                                .fatherRegistrationNumber("1635717065")
                                .fatherName("C.V.C SIGNOS PETROLEO")
                                .maternalGrandfatherRegistrationNumber("123")
                                .maternalGrandfatherName("AVÃ” MAT")
                                .build()
                ));

        var response = business.findComplementaryGenealogy(1L, "1643218012");

        assertThat(response.getIntegration().getStatus()).isEqualTo("FOUND");
        assertThat(response.getPai().getSource()).isEqualTo(GenealogyNodeSource.ABCC);
        assertThat(response.getMae().getSource()).isEqualTo(GenealogyNodeSource.LOCAL);
        assertThat(response.getAvoMaterno().getSource()).isEqualTo(GenealogyNodeSource.ABCC);
        assertThat(response.getAvoPaterno().getSource()).isEqualTo(GenealogyNodeSource.AUSENTE);
    }

    @Test
    void shouldReturnInsufficientDataWhenLocalRegistrationIsMissing() {
        Goat goat = new Goat();
        goat.setRegistrationNumber("  ");
        goat.setName("SEM REGISTRO");

        when(goatGenealogyQueryPort.findByIdAndFarmIdWithFamilyGraph("g1", 1L))
                .thenReturn(Optional.of(goat));

        var response = business.findComplementaryGenealogy(1L, "g1");

        assertThat(response.getIntegration().getStatus()).isEqualTo("INSUFFICIENT_DATA");
        verify(genealogyAbccQueryPort, never()).findGenealogyByRegistrationNumber(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void shouldReturnNotFoundWhenAbccDoesNotFindRegistration() {
        Goat goat = new Goat();
        goat.setRegistrationNumber("1643218012");
        goat.setName("XEQUE");

        when(goatGenealogyQueryPort.findByIdAndFarmIdWithFamilyGraph("1643218012", 1L))
                .thenReturn(Optional.of(goat));
        when(genealogyAbccQueryPort.findGenealogyByRegistrationNumber("1643218012"))
                .thenReturn(Optional.empty());

        var response = business.findComplementaryGenealogy(1L, "1643218012");

        assertThat(response.getIntegration().getStatus()).isEqualTo("NOT_FOUND");
    }

    @Test
    void shouldReturnUnavailableWhenAbccPortFails() {
        Goat goat = new Goat();
        goat.setRegistrationNumber("1643218012");
        goat.setName("XEQUE");

        when(goatGenealogyQueryPort.findByIdAndFarmIdWithFamilyGraph("1643218012", 1L))
                .thenReturn(Optional.of(goat));
        when(genealogyAbccQueryPort.findGenealogyByRegistrationNumber("1643218012"))
                .thenThrow(new RuntimeException("ABCC down"));

        var response = business.findComplementaryGenealogy(1L, "1643218012");

        assertThat(response.getIntegration().getStatus()).isEqualTo("UNAVAILABLE");
        assertThat(response.getIntegration().getMessage()).contains("ABCC");
    }

    @Test
    void shouldThrowWhenGoatIsNotFound() {
        when(goatGenealogyQueryPort.findByIdAndFarmIdWithFamilyGraph("999", 1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> business.findComplementaryGenealogy(1L, "999"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}

