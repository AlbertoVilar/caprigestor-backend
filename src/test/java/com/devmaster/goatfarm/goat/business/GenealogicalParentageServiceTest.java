package com.devmaster.goatfarm.goat.business;

import com.devmaster.goatfarm.application.core.business.validation.GoatGenderValidator;
import com.devmaster.goatfarm.config.exceptions.custom.BusinessRuleException;
import com.devmaster.goatfarm.config.exceptions.custom.ExternalServiceUnavailableException;
import com.devmaster.goatfarm.farm.persistence.entity.GoatFarm;
import com.devmaster.goatfarm.genealogy.application.ports.out.GenealogyAbccQueryPort;
import com.devmaster.goatfarm.genealogy.business.bo.GenealogyAbccSnapshotVO;
import com.devmaster.goatfarm.goat.application.ports.out.GoatPersistencePort;
import com.devmaster.goatfarm.goat.enums.Category;
import com.devmaster.goatfarm.goat.enums.Gender;
import com.devmaster.goatfarm.goat.persistence.entity.Goat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GenealogicalParentageServiceTest {

    @Mock
    private GoatPersistencePort goatPersistencePort;

    @Mock
    private GenealogyAbccQueryPort genealogyAbccQueryPort;

    private GenealogicalParentageService service;

    @BeforeEach
    void setUp() {
        service = new GenealogicalParentageService(
                goatPersistencePort,
                genealogyAbccQueryPort,
                new GoatGenderValidator(goatPersistencePort)
        );
        org.mockito.Mockito.lenient().when(goatPersistencePort.findByRegistrationNumber(anyString())).thenReturn(Optional.empty());
        org.mockito.Mockito.lenient().when(genealogyAbccQueryPort.findGenealogyByRegistrationNumber(anyString())).thenReturn(Optional.empty());
    }

    @Test
    void paAcceptsAnUnknownDeclaredFatherAndPreservesItsCompleteRegistration() {
        GenealogicalParentageService.ResolvedParentage result = service.resolve(
                Category.PA,
                "KID-001",
                " 1635719026a ",
                null
        );

        assertThat(result.father()).isNull();
        assertThat(result.externalFatherRegistrationNumber()).isEqualTo("1635719026A");
        assertThat(result.mother()).isNull();
        assertThat(result.externalMotherRegistrationNumber()).isNull();
    }

    @ParameterizedTest
    @EnumSource(value = Category.class, names = {"PO", "PC"})
    void registeredCategoriesRequireBothParents(Category category) {
        assertThatThrownBy(() -> service.resolve(category, "KID-001", null, null))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("referência genealógica válida");
    }

    @Test
    void acceptsLocalParentsIncludingAFatherFromAnotherFarmWithoutChangingOwnership() {
        Goat father = goat("FATHER-001", Gender.MACHO);
        GoatFarm otherFarm = new GoatFarm();
        otherFarm.setId(99L);
        father.setFarm(otherFarm);
        Goat mother = goat("MOTHER-001", Gender.FEMEA);
        when(goatPersistencePort.findByRegistrationNumber("FATHER-001")).thenReturn(Optional.of(father));
        when(goatPersistencePort.findByRegistrationNumber("MOTHER-001")).thenReturn(Optional.of(mother));

        GenealogicalParentageService.ResolvedParentage result = service.resolve(
                Category.PO, "KID-001", "FATHER-001", "MOTHER-001"
        );

        assertThat(result.father()).isSameAs(father);
        assertThat(result.mother()).isSameAs(mother);
        assertThat(father.getFarm().getId()).isEqualTo(99L);
        assertThat(result.externalFatherRegistrationNumber()).isNull();
    }

    @Test
    void rejectsALocalFemaleDeclaredAsFather() {
        when(goatPersistencePort.findByRegistrationNumber("FEMALE-001"))
                .thenReturn(Optional.of(goat("FEMALE-001", Gender.FEMEA)));

        assertThatThrownBy(() -> service.resolve(Category.PA, "KID-001", "FEMALE-001", null))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("O registro informado para o pai corresponde a um animal do sexo feminino.");
    }

    @Test
    void rejectsAnAbccMaleDeclaredAsMother() {
        when(genealogyAbccQueryPort.findGenealogyByRegistrationNumber("MALE-001"))
                .thenReturn(Optional.of(snapshot("MALE-001", Gender.MACHO)));

        assertThatThrownBy(() -> service.resolve(Category.PA, "KID-001", null, "MALE-001"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("O registro informado para a mãe corresponde a um animal do sexo masculino.");
    }

    @Test
    void acceptsAnAbccFatherAndPreservesTheAlphabeticRegistrationSuffix() {
        Goat mother = goat("MOTHER-001", Gender.FEMEA);
        when(goatPersistencePort.findByRegistrationNumber("MOTHER-001")).thenReturn(Optional.of(mother));
        when(genealogyAbccQueryPort.findGenealogyByRegistrationNumber("1635719026A"))
                .thenReturn(Optional.of(snapshot("1635719026A", Gender.MACHO)));

        GenealogicalParentageService.ResolvedParentage result = service.resolve(
                Category.PO, "KID-001", "1635719026a", "MOTHER-001"
        );

        assertThat(result.father()).isNull();
        assertThat(result.externalFatherRegistrationNumber()).isEqualTo("1635719026A");
        assertThat(result.mother()).isSameAs(mother);
    }

    @Test
    void rejectsUnknownParentsForPoInsteadOfDowngradingTheCategory() {
        assertThatThrownBy(() -> service.resolve(Category.PO, "KID-001", "UNKNOWN-001", "MOTHER-001"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("referência genealógica válida");
    }

    @Test
    void reportsAbccUnavailabilityInsteadOfTreatingItAsNotFoundEvenForPa() {
        when(genealogyAbccQueryPort.findGenealogyByRegistrationNumber("OFFLINE-001"))
                .thenThrow(new IllegalStateException("timeout"));

        assertThatThrownBy(() -> service.resolve(Category.PA, "KID-001", "OFFLINE-001", null))
                .isInstanceOf(ExternalServiceUnavailableException.class)
                .hasMessage("A consulta à ABCC está temporariamente indisponível.");
    }

    private Goat goat(String registrationNumber, Gender gender) {
        Goat goat = new Goat();
        goat.setRegistrationNumber(registrationNumber);
        goat.setGender(gender);
        return goat;
    }

    private GenealogyAbccSnapshotVO snapshot(String registrationNumber, Gender gender) {
        return GenealogyAbccSnapshotVO.builder()
                .animalRegistrationNumber(registrationNumber)
                .animalGender(gender)
                .build();
    }
}
