package com.devmaster.goatfarm.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class OwnershipSecurityBoundaryArchUnitTest {

    private static final JavaClasses IMPORTED_CLASSES = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.devmaster.goatfarm");

    @Test
    void ownershipServiceMustNotDependOnSpringDataRepositories() {
        noClasses()
                .that().haveFullyQualifiedName("com.devmaster.goatfarm.config.security.OwnershipService")
                .should().dependOnClassesThat().resideInAPackage("..persistence.repository..")
                .because("a autorização deve consultar persistência por portas mínimas")
                .check(IMPORTED_CLASSES);
    }

    @Test
    void criticalApplicationPortsMustNotDependOnPersistenceEntities() {
        noClasses()
                .that().haveFullyQualifiedName("com.devmaster.goatfarm.events.application.ports.out.EventPublisher")
                .or().haveFullyQualifiedName("com.devmaster.goatfarm.events.business.bo.EventPublication")
                .or().haveFullyQualifiedName("com.devmaster.goatfarm.goat.application.ports.out.GoatValidationQueryPort")
                .or().haveFullyQualifiedName("com.devmaster.goatfarm.farm.application.ports.out.FarmOwnerQueryPort")
                .or().haveFullyQualifiedName("com.devmaster.goatfarm.config.security.JwtService")
                .should().dependOnClassesThat().resideInAPackage("..persistence.entity..")
                .because("portas críticas devem transportar contratos de aplicação, não entidades JPA")
                .check(IMPORTED_CLASSES);
    }

    @Test
    void ownershipServiceMustDependOnlyOnAuthorizationContracts() {
        noClasses()
                .that().haveFullyQualifiedName("com.devmaster.goatfarm.config.security.OwnershipService")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "..authority.persistence.entity..", "..goat.persistence..", "..farm.persistence.entity..",
                        "..authority.application.ports.out.UserPersistencePort", "..farm.application.ports.out.GoatFarmPersistencePort",
                        "..goat.application.routing..")
                .check(IMPORTED_CLASSES);
    }

    @Test
    void businessAndApplicationPackagesMustNotReadSecurityContextDirectly() {
        noClasses()
                .that().resideInAnyPackage("..authority.business..", "..goat.business..", "..farm.business..", "..audit.business..")
                .should().dependOnClassesThat().haveFullyQualifiedName("org.springframework.security.core.context.SecurityContextHolder")
                .check(IMPORTED_CLASSES);
    }

    @Test
    void businessPackagesMustUseFarmAuthorizationPortInsteadOfConcreteService() {
        noClasses()
                .that().resideInAnyPackage("..business..")
                .should().dependOnClassesThat().haveFullyQualifiedName("com.devmaster.goatfarm.config.security.OwnershipService")
                .check(IMPORTED_CLASSES);
    }
}
