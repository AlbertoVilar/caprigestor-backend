package com.devmaster.goatfarm.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/** Zero-baseline guard for the I3-C Farm, Address and Phone application cores. */
class FarmAddressPhoneCoreBoundaryArchUnitTest {
    private static final JavaClasses CLASSES = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.devmaster.goatfarm");

    @Test
    void farmAddressAndPhoneCoreMustNotDependOnPersistenceEntities() {
        noClasses().that().resideInAnyPackage("..farm.application..", "..farm.business..", "..address.application..", "..address.business..", "..phone.application..", "..phone.business..")
                .should().dependOnClassesThat().resideInAnyPackage("..persistence.entity..")
                .check(CLASSES);
    }
}
