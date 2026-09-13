package com.devmaster.goatfarm.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class HealthBoundaryArchUnitTest {

    @Test
    void healthApplicationAndBusinessMustNotDependOnPersistenceEntities() {
        JavaClasses imported = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.devmaster.goatfarm");

        noClasses()
                .that().resideInAnyPackage("..health.application..", "..health.business..")
                .should().dependOnClassesThat().resideInAnyPackage("..persistence.entity..")
                .because("Health application and business layers must remain independent from JPA entities")
                .check(imported);
    }
}
