package com.devmaster.goatfarm.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class LactationDomainBoundaryArchUnitTest {

    private static final JavaClasses CLASSES = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.devmaster.goatfarm");

    @Test
    void lactationDomainMustRemainFrameworkFree() {
        noClasses().that().resideInAnyPackage("..milk.domain..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "..persistence..", "..api..", "..controller..",
                        "org.springframework..", "jakarta.persistence..", "org.hibernate..")
                .check(CLASSES);
    }

    @Test
    void lactationBusinessMustNotDependOnPersistenceRepresentation() {
        noClasses().that().haveFullyQualifiedName(
                        "com.devmaster.goatfarm.milk.business.lactationservice.LactationBusiness")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "..milk.persistence.entity..", "..milk.persistence.projection..")
                .check(CLASSES);
    }

    @Test
    void lactationPersistencePortMustNotExposePersistenceTypes() {
        noClasses().that().haveFullyQualifiedName(
                        "com.devmaster.goatfarm.milk.application.ports.out.LactationPersistencePort")
                .should().dependOnClassesThat().resideInAnyPackage("..milk.persistence..")
                .check(CLASSES);
    }
}
