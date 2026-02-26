package com.devmaster.goatfarm.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class InventoryBoundaryArchUnitTest {

    @Test
    void inventoryMustNotDependOnMilkHealthOrReproductionModules() {
        JavaClasses imported = new ClassFileImporter()
                .importPackages("com.devmaster.goatfarm");

        noClasses()
                .that().resideInAPackage("..inventory..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("..milk..", "..health..", "..reproduction..")
                .because("inventory deve manter fronteira de contexto sem acoplamento direto.")
                .check(imported);
    }
}
