package com.devmaster.goatfarm.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Keeps the retired legacy persistence seam from reappearing in application
 * code. JPA relationships in other persistence adapters are intentionally
 * outside this rule; only application/business/domain/API/configuration code
 * is forbidden from depending on Goat's persistence implementation types.
 */
class GoatPersistenceBoundaryArchUnitTest {

    private static final JavaClasses IMPORTED_CLASSES = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.devmaster.goatfarm");

    @Test
    void goatPersistenceEntityMustStayInsidePersistenceAdapters() {
        ArchRule rule = noClasses()
                .that().resideInAnyPackage("..application..", "..business..", "..domain..", "..api..", "..config..", "..sharedkernel..")
                .should().dependOnClassesThat().haveFullyQualifiedName("com.devmaster.goatfarm.goat.persistence.entity.GoatEntity");

        rule.check(IMPORTED_CLASSES);
    }

    @Test
    void goatRepositoryAndProjectionMustNotCrossApplicationBoundary() {
        ArchRule rule = noClasses()
                .that().resideInAnyPackage("..application..", "..business..", "..domain..", "..api..", "..config..", "..sharedkernel..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "..goat.persistence.repository..",
                        "..goat.persistence.entity..");

        rule.check(IMPORTED_CLASSES);
    }

    @Test
    void goatApplicationPortsMustNotExposePersistenceTypes() {
        ArchRule rule = noClasses()
                .that().resideInAnyPackage("..goat.application.ports..")
                .should().dependOnClassesThat().resideInAnyPackage("..persistence..");

        rule.check(IMPORTED_CLASSES);
    }
}
