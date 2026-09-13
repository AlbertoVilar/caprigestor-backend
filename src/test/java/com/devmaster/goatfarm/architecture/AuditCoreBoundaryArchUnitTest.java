package com.devmaster.goatfarm.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/** Zero-baseline guard for the Audit application/business core. */
class AuditCoreBoundaryArchUnitTest {

    private static final JavaClasses CLASSES = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.devmaster.goatfarm");

    @Test
    void auditCoreMustNotDependOnPersistenceEntities() {
        noClasses().that().resideInAnyPackage("..audit.application..", "..audit.business..")
                .should().dependOnClassesThat().resideInAnyPackage("..persistence.entity..")
                .check(CLASSES);
    }
}
