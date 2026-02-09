package com.devmaster.goatfarm.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class MilkReproductionBoundaryArchUnitTest {

    private static final JavaClasses IMPORTED_CLASSES = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.devmaster.goatfarm");

    @Test
    void milkMustNotDependOnReproductionPersistenceEntities() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..milk..")
                .should().dependOnClassesThat().resideInAPackage("..reproduction.persistence.entity..");

        rule.check(IMPORTED_CLASSES);
    }

    @Test
    void milkMustNotDependOnReproductionApiOrBusiness() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..milk..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("..reproduction.api..", "..reproduction.business..");

        rule.check(IMPORTED_CLASSES);
    }
}
