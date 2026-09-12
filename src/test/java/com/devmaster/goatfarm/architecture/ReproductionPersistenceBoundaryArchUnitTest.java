package com.devmaster.goatfarm.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/** Guards the A9A reproduction domain/application boundary. */
class ReproductionPersistenceBoundaryArchUnitTest {

    private static final JavaClasses PRODUCTION_CLASSES = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.devmaster.goatfarm");

    @Test
    void reproductionDomainMustRemainFrameworkAndPersistenceFree() {
        noClasses().that().resideInAnyPackage("..reproduction.domain..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "..reproduction.persistence..", "..reproduction.api..", "org.springframework..",
                        "jakarta.persistence..", "javax.persistence..").check(PRODUCTION_CLASSES);
    }

    @Test
    void reproductionBusinessMustNotDependOnPersistenceTypesOrFarmPersistence() {
        noClasses().that().resideInAnyPackage("..reproduction.business..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "..reproduction.persistence.entity..", "..reproduction.persistence.projection..",
                        "..farm.persistence..").check(PRODUCTION_CLASSES);
    }

    @Test
    void reproductionPersistencePortsMustExposeOnlyApplicationOrDomainTypes() {
        noClasses().that().resideInAnyPackage("..reproduction.application.ports..")
                .should().dependOnClassesThat().resideInAnyPackage("..reproduction.persistence..").check(PRODUCTION_CLASSES);
    }
}
