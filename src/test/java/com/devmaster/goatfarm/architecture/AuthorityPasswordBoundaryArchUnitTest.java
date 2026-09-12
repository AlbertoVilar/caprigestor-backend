package com.devmaster.goatfarm.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Keeps Spring Security password hashing out of the Authority core.
 */
class AuthorityPasswordBoundaryArchUnitTest {

    private static final JavaClasses PRODUCTION_CLASSES = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.devmaster.goatfarm");

    @Test
    void authorityCoreMustNotDependOnPasswordEncoder() {
        noClasses().that().resideInAnyPackage("..authority.application..", "..authority.business..")
                .should().dependOnClassesThat()
                .haveFullyQualifiedName("org.springframework.security.crypto.password.PasswordEncoder")
                .because("Authority core uses PasswordHashingPort; Spring PasswordEncoder stays in infrastructure")
                .check(PRODUCTION_CLASSES);
    }
}
