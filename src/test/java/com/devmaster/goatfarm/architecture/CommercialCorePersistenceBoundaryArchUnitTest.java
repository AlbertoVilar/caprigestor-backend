package com.devmaster.goatfarm.architecture;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class CommercialCorePersistenceBoundaryArchUnitTest {
    private static final JavaClasses PRODUCTION = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.devmaster.goatfarm.commercial");

    @Test
    void commercialApplicationAndBusinessMustNotDependOnJpaEntities() {
        noClasses().that().resideInAnyPackage(
                        "..commercial.application..",
                        "..commercial.business..")
                .and().doNotHaveSimpleName("OperationalFinanceBusiness")
                .and().doNotHaveSimpleName("OperationalFinancePersistencePort")
                .should().dependOnClassesThat().resideInAnyPackage("..persistence.entity..")
                .check(PRODUCTION);
    }
}
