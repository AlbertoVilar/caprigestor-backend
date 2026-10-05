package com.devmaster.goatfarm.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class HealthHistoricalDossierBoundaryArchUnitTest {

    private static final JavaClasses IMPORTED_CLASSES = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.devmaster.goatfarm");

    @Test
    void businessMustDependOnPortsNotRepositories() {
        noClasses()
                .that().haveFullyQualifiedName("com.devmaster.goatfarm.goatownership.business.FarmGoatHistoricalHealthQueryBusiness")
                .should().dependOnClassesThat().resideInAPackage("..persistence.repository..")
                .because("the historical health business service must depend only on ports, not repositories")
                .check(IMPORTED_CLASSES);
    }

    @Test
    void healthPersistenceAdapterOwnsRepositoryAccess() {
        classes()
                .that().haveFullyQualifiedName("com.devmaster.goatfarm.health.persistence.adapter.FarmGoatHistoricalHealthPersistenceAdapter")
                .should().dependOnClassesThat().haveFullyQualifiedName("com.devmaster.goatfarm.health.persistence.repository.HealthEventRepository")
                .check(IMPORTED_CLASSES);
    }

    @Test
    void adapterMustImplementNeutralGoatownershipApplicationPort() {
        classes()
                .that().haveFullyQualifiedName("com.devmaster.goatfarm.health.persistence.adapter.FarmGoatHistoricalHealthPersistenceAdapter")
                .should().implement("com.devmaster.goatfarm.goatownership.application.ports.out.FarmGoatHistoricalHealthQueryPort")
                .check(IMPORTED_CLASSES);
    }

    @Test
    void noJpaEntityCrossesApplicationBoundary() {
        noClasses()
                .that().resideInAnyPackage("..goatownership.application..", "..goatownership.business..")
                .should().dependOnClassesThat().resideInAPackage("..health.persistence.entity..")
                .because("technology-neutral application models must be used instead of JPA entities")
                .check(IMPORTED_CLASSES);
    }

    @Test
    void noPaginationTypesCrossApplicationBoundary() {
        noClasses()
                .that().resideInAnyPackage("..goatownership.application..", "..goatownership.business..")
                .should().dependOnClassesThat().resideInAPackage("org.springframework.data.domain..")
                .because("Spring Data pagination types must not cross application port boundaries")
                .check(IMPORTED_CLASSES);
    }
}
