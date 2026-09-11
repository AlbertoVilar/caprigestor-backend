package com.devmaster.goatfarm.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/** Protects the Goat/Genealogy direction and application read boundary. */
class GoatGenealogyBoundaryArchUnitTest {

    private static final JavaClasses IMPORTED_CLASSES = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.devmaster.goatfarm");

    @Test
    void goatProductionMustNotDependOnGenealogy() {
        noClasses().that().resideInAPackage("..goat..")
                .should().dependOnClassesThat().resideInAPackage("..genealogy..")
                .because("Goat must remain independent from the Genealogy bounded context")
                .check(IMPORTED_CLASSES);
    }

    @Test
    void genealogyMustUseGoatApplicationBoundaryNotGoatInternals() {
        noClasses().that().resideInAPackage("..genealogy..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "..goat.persistence..", "..goat.business..", "..goat.api..")
                .because("Genealogy must consume Goat public application contracts")
                .check(IMPORTED_CLASSES);
    }

    @Test
    void genealogyReadContractMustBeGoatInbound() {
        Assertions.assertTrue(IMPORTED_CLASSES.stream().noneMatch(clazz ->
                        clazz.getName().equals("com.devmaster.goatfarm.goat.application.ports.out.GoatGenealogyQueryPort")),
                "The old outbound genealogy contract must not remain");
        Assertions.assertTrue(IMPORTED_CLASSES.stream().anyMatch(clazz ->
                        clazz.getName().equals("com.devmaster.goatfarm.goat.application.ports.in.GoatGenealogyReadUseCase")),
                "The public genealogy read contract must be Goat inbound");
    }
}
