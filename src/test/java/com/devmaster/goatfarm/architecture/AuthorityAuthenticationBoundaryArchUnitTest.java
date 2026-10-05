package com.devmaster.goatfarm.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/** Zero-baseline guard for concrete authentication/JWT dependencies in Authority core. */
class AuthorityAuthenticationBoundaryArchUnitTest {

    private static final JavaClasses PRODUCTION_CLASSES = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.devmaster.goatfarm");

    @Test
    void authorityCoreMustNotDependOnSpringAuthenticationOrJwtInfrastructure() {
        String[] forbiddenDependencies = {
                "org.springframework.security.authentication.AuthenticationManager",
                "org.springframework.security.core.Authentication",
                "org.springframework.security.authentication.UsernamePasswordAuthenticationToken",
                "org.springframework.security.oauth2.jwt.JwtDecoder",
                "com.devmaster.goatfarm.config.security.JwtService",
                "org.springframework.security.oauth2.jwt.Jwt"
        };
        for (String forbiddenDependency : forbiddenDependencies) {
            noClasses().that().resideInAnyPackage("..authority.application..", "..authority.business..")
                    .should().dependOnClassesThat().haveFullyQualifiedName(forbiddenDependency)
                    .because("Authority core depends on application ports; Spring authentication/JWT stays in adapters")
                    .check(PRODUCTION_CLASSES);
        }
    }
}
