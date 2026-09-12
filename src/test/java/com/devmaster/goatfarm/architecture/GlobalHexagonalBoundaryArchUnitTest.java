package com.devmaster.goatfarm.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Global, structural boundaries that are already satisfied by the current
 * codebase. Rules that would fail because of the DEV-A3 persistence baseline
 * are intentionally deferred to DEV-A11-I2/I3.
 */
class GlobalHexagonalBoundaryArchUnitTest {

    private static final JavaClasses IMPORTED_CLASSES = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.devmaster.goatfarm");

    @Test
    void domainMustRemainIndependentFromOuterLayersAndApplicationPorts() {
        noClasses()
                .that().resideInAnyPackage("..domain..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "org.springframework..",
                        "jakarta.persistence..",
                        "org.hibernate..",
                        "org.springframework.security..",
                        "..api..",
                        "..persistence..",
                        "..config..",
                        "..application.ports..")
                .because("domain models must not depend on framework, adapter, or application-port details")
                .check(IMPORTED_CLASSES);
    }

    @Test
    void controllersMustUseApplicationBoundaryInsteadOfPersistenceDetails() {
        noClasses()
                .that().resideInAnyPackage("..api.controller..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "..persistence.repository..",
                        "..persistence.entity..",
                        "..persistence.adapter..")
                .because("controllers must depend on inbound use cases and API mappers")
                .check(IMPORTED_CLASSES);
    }

    @Test
    void securityContextMustRemainInsideTheCurrentPrincipalAdapter() {
        noClasses()
                .that().resideInAnyPackage("..domain..", "..application..", "..business..", "..api..")
                .should().dependOnClassesThat().haveFullyQualifiedName(
                        "org.springframework.security.core.context.SecurityContextHolder")
                .because("the current principal adapter is the only boundary allowed to touch SecurityContextHolder")
                .check(IMPORTED_CLASSES);
    }
}
