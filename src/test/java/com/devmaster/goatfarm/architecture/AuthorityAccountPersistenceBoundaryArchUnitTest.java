package com.devmaster.goatfarm.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/** Keeps Authority account and role persistence entities behind the persistence adapters. */
class AuthorityAccountPersistenceBoundaryArchUnitTest {
    private static final JavaClasses PRODUCTION_CLASSES = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.devmaster.goatfarm");

    @Test
    void authorityCoreMustNotDependOnAccountOrRoleEntities() {
        noClasses().that().resideInAnyPackage("..authority.application..", "..authority.business..")
                .should().dependOnClassesThat().haveFullyQualifiedName("com.devmaster.goatfarm.authority.persistence.entity.User")
                .because("Authority core owns application account models; JPA User stays in persistence")
                .check(PRODUCTION_CLASSES);
        noClasses().that().resideInAnyPackage("..authority.application..", "..authority.business..")
                .should().dependOnClassesThat().haveFullyQualifiedName("com.devmaster.goatfarm.authority.persistence.entity.Role")
                .because("Authority core owns application role models; JPA Role stays in persistence")
                .check(PRODUCTION_CLASSES);
    }
}
