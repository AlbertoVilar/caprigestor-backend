package com.devmaster.goatfarm.goat.business;

import com.devmaster.goatfarm.config.exceptions.custom.BusinessRuleException;
import com.devmaster.goatfarm.config.exceptions.custom.ExternalServiceUnavailableException;
import com.devmaster.goatfarm.config.exceptions.custom.InvalidArgumentException;
import com.devmaster.goatfarm.application.core.business.validation.GoatGenderValidator;
import com.devmaster.goatfarm.genealogy.application.ports.out.GenealogyAbccQueryPort;
import com.devmaster.goatfarm.genealogy.business.bo.GenealogyAbccSnapshotVO;
import com.devmaster.goatfarm.goat.application.ports.out.GoatPersistencePort;
import com.devmaster.goatfarm.goat.enums.Category;
import com.devmaster.goatfarm.goat.enums.Gender;
import com.devmaster.goatfarm.goat.persistence.entity.Goat;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Optional;

/**
 * Resolves and validates the two genealogical parent references of a new or updated goat.
 * It deliberately owns no persistence: local parents remain FKs and external parents remain
 * registration references, so genealogical trees can continue to be projected on demand.
 */
@Service
public class GenealogicalParentageService {

    private final GoatPersistencePort goatPersistencePort;
    private final GenealogyAbccQueryPort genealogyAbccQueryPort;
    private final GoatGenderValidator goatGenderValidator;

    public GenealogicalParentageService(
            GoatPersistencePort goatPersistencePort,
            GenealogyAbccQueryPort genealogyAbccQueryPort,
            GoatGenderValidator goatGenderValidator
    ) {
        this.goatPersistencePort = goatPersistencePort;
        this.genealogyAbccQueryPort = genealogyAbccQueryPort;
        this.goatGenderValidator = goatGenderValidator;
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED, readOnly = true)
    public ResolvedParentage resolve(
            Category category,
            String childRegistrationNumber,
            String fatherRegistrationNumber,
            String motherRegistrationNumber
    ) {
        if (category == null) {
            throw new BusinessRuleException("category", "Categoria genealógica é obrigatória.");
        }

        ParentReference father = resolveParent(
                category,
                childRegistrationNumber,
                fatherRegistrationNumber,
                ParentRole.FATHER
        );
        ParentReference mother = resolveParent(
                category,
                childRegistrationNumber,
                motherRegistrationNumber,
                ParentRole.MOTHER
        );

        return new ResolvedParentage(
                father.localGoat(),
                mother.localGoat(),
                father.externalRegistrationNumber(),
                mother.externalRegistrationNumber()
        );
    }

    private ParentReference resolveParent(
            Category category,
            String childRegistrationNumber,
            String registrationNumber,
            ParentRole role
    ) {
        String registration = normalizeRegistration(registrationNumber);
        if (registration == null) {
            requireParentForRegisteredCategories(category, role);
            return ParentReference.empty();
        }

        String childRegistration = normalizeRegistration(childRegistrationNumber);
        if (registration.equals(childRegistration)) {
            throw new InvalidArgumentException(role.fieldName(), "O " + role.label() + " não pode ser o próprio animal.");
        }

        Optional<Goat> localParent = goatPersistencePort.findByRegistrationNumber(registration);
        if (localParent.isPresent()) {
            validateGender(localParent.get().getGender(), role);
            return ParentReference.local(localParent.get());
        }

        Optional<GenealogyAbccSnapshotVO> abccParent;
        try {
            abccParent = genealogyAbccQueryPort.findGenealogyByRegistrationNumber(registration);
        } catch (RuntimeException ex) {
            throw new ExternalServiceUnavailableException(
                    "A consulta à ABCC está temporariamente indisponível.",
                    ex
            );
        }

        if (abccParent.isEmpty()) {
            requireParentForRegisteredCategories(category, role);
            return ParentReference.external(registration);
        }

        GenealogyAbccSnapshotVO snapshot = abccParent.get();
        String returnedRegistration = normalizeRegistration(snapshot.getAnimalRegistrationNumber());
        if (!registration.equals(returnedRegistration) || snapshot.getAnimalGender() == null) {
            if (category == Category.PA) {
                return ParentReference.external(registration);
            }
            throw new ExternalServiceUnavailableException(
                    "A ABCC retornou uma resposta incompleta para o genitor informado.",
                    null
            );
        }

        validateGender(snapshot.getAnimalGender(), role);
        return ParentReference.external(registration);
    }

    private void requireParentForRegisteredCategories(Category category, ParentRole role) {
        if (category == Category.PO || category == Category.PC) {
            throw new BusinessRuleException(
                    role.fieldName(),
                    "Para animais " + category.name() + ", o campo " + role.label()
                            + " deve possuir referência genealógica válida."
            );
        }
    }

    private void validateGender(Gender actualGender, ParentRole role) {
        goatGenderValidator.requireGender(
                actualGender,
                role.expectedGender(),
                role.fieldName(),
                role.article(),
                role.label()
        );
    }

    private String normalizeRegistration(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().replaceAll("\\s+", "").toUpperCase(Locale.ROOT);
        return normalized.isEmpty() ? null : normalized;
    }

    public record ResolvedParentage(
            Goat father,
            Goat mother,
            String externalFatherRegistrationNumber,
            String externalMotherRegistrationNumber
    ) {
    }

    private record ParentReference(Goat localGoat, String externalRegistrationNumber) {
        private static ParentReference empty() {
            return new ParentReference(null, null);
        }

        private static ParentReference local(Goat goat) {
            return new ParentReference(goat, null);
        }

        private static ParentReference external(String registrationNumber) {
            return new ParentReference(null, registrationNumber);
        }
    }

    private enum ParentRole {
        FATHER("fatherRegistrationNumber", "o", "pai", Gender.MACHO),
        MOTHER("motherRegistrationNumber", "a", "mãe", Gender.FEMEA);

        private final String fieldName;
        private final String article;
        private final String label;
        private final Gender expectedGender;

        ParentRole(String fieldName, String article, String label, Gender expectedGender) {
            this.fieldName = fieldName;
            this.article = article;
            this.label = label;
            this.expectedGender = expectedGender;
        }

        String fieldName() { return fieldName; }
        String article() { return article; }
        String label() { return label; }
        Gender expectedGender() { return expectedGender; }
    }
}
