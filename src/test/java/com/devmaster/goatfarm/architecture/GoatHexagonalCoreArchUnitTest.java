package com.devmaster.goatfarm.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class GoatHexagonalCoreArchUnitTest {

    private final JavaClasses classes = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.devmaster.goatfarm.goat");

    @Test
    void domainMustRemainFrameworkFree() {
        noClasses().that().resideInAnyPackage("..goat.domain..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "..persistence..", "org.springframework..", "jakarta.persistence..")
                .check(classes);
    }

    @Test
    void cleanPersistencePortMustNotExposeInfrastructure() {
        noClasses().that().haveFullyQualifiedName(
                        "com.devmaster.goatfarm.goat.application.ports.out.GoatPersistencePort")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "..persistence..", "org.springframework..", "jakarta.persistence..")
                .check(classes);
    }

    @Test
    void goatApplicationMustNotDependOnSpringData() {
        noClasses().that().resideInAnyPackage("..goat.application..")
                .should().dependOnClassesThat().resideInAnyPackage("org.springframework.data..")
                .check(classes);
    }
}
