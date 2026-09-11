package com.devmaster.goatfarm.goat.business;

import com.devmaster.goatfarm.application.core.business.validation.GoatGenderValidator;
import com.devmaster.goatfarm.config.exceptions.custom.BusinessRuleException;
import com.devmaster.goatfarm.config.exceptions.custom.ExternalServiceUnavailableException;
import com.devmaster.goatfarm.genealogy.application.ports.out.GenealogyAbccQueryPort;
import com.devmaster.goatfarm.genealogy.business.bo.GenealogyAbccSnapshotVO;
import com.devmaster.goatfarm.goat.application.ports.out.GoatReference;
import com.devmaster.goatfarm.goat.application.ports.out.GoatReferenceQueryPort;
import com.devmaster.goatfarm.goat.application.ports.out.GoatValidationQueryPort;
import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goat.enums.Category;
import com.devmaster.goatfarm.goat.enums.Gender;
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
    private GoatReferenceQueryPort goatReferenceQueryPort;

    @Mock
    private GoatValidationQueryPort goatValidationQueryPort;

    @Mock
    private GenealogyAbccQueryPort genealogyAbccQueryPort;

    private GenealogicalParentageService service;

    @BeforeEach
    void setUp() {
        service = new GenealogicalParentageService(
                goatReferenceQueryPort,
                genealogyAbccQueryPort,
                new GoatGenderValidator(goatValidationQueryPort)
        );
        org.mockito.Mockito.lenient().when(goatReferenceQueryPort.findReferenceByRegistrationNumber(anyString())).thenReturn(Optional.empty());
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

        assertThat(result.father().isLocal()).isFalse();
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
        GoatReference father = goat("FATHER-001", Gender.MACHO, 11L, 99L);
        GoatReference mother = goat("MOTHER-001", Gender.FEMEA, 12L, 10L);
        when(goatReferenceQueryPort.findReferenceByRegistrationNumber("FATHER-001")).thenReturn(Optional.of(father));
        when(goatReferenceQueryPort.findReferenceByRegistrationNumber("MOTHER-001")).thenReturn(Optional.of(mother));

        GenealogicalParentageService.ResolvedParentage result = service.resolve(
                Category.PO, "KID-001", "FATHER-001", "MOTHER-001"
        );

        assertThat(result.father().id()).isEqualTo(new GoatId(11L));
        assertThat(result.father().registrationNumber()).isEqualTo("FATHER-001");
        assertThat(result.mother().id()).isEqualTo(new GoatId(12L));
        assertThat(father.farmId()).isEqualTo(99L);
        assertThat(result.externalFatherRegistrationNumber()).isNull();
    }

    @Test
    void rejectsALocalFemaleDeclaredAsFather() {
        when(goatReferenceQueryPort.findReferenceByRegistrationNumber("FEMALE-001"))
                .thenReturn(Optional.of(goat("FEMALE-001", Gender.FEMEA, 13L, 10L)));

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
        GoatReference mother = goat("MOTHER-001", Gender.FEMEA, 12L, 10L);
        when(goatReferenceQueryPort.findReferenceByRegistrationNumber("MOTHER-001")).thenReturn(Optional.of(mother));
        when(genealogyAbccQueryPort.findGenealogyByRegistrationNumber("1635719026A"))
                .thenReturn(Optional.of(snapshot("1635719026A", Gender.MACHO)));

        GenealogicalParentageService.ResolvedParentage result = service.resolve(
                Category.PO, "KID-001", "1635719026a", "MOTHER-001"
        );

        assertThat(result.father().isLocal()).isFalse();
        assertThat(result.externalFatherRegistrationNumber()).isEqualTo("1635719026A");
        assertThat(result.mother().id()).isEqualTo(new GoatId(12L));
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

    private GoatReference goat(String registrationNumber, Gender gender, Long id, Long farmId) {
        return new GoatReference(new GoatId(id), farmId, registrationNumber, registrationNumber, gender);
    }

    private GenealogyAbccSnapshotVO snapshot(String registrationNumber, Gender gender) {
        return GenealogyAbccSnapshotVO.builder()
                .animalRegistrationNumber(registrationNumber)
                .animalGender(gender)
                .build();
    }
}
