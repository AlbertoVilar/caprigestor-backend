package com.devmaster.goatfarm.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class ReproductionHistoricalDossierBoundaryArchUnitTest {

    private static final JavaClasses IMPORTED_CLASSES = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.devmaster.goatfarm");

    @Test
    void businessMustDependOnPortsNotRepositories() {
        noClasses()
                .that().haveFullyQualifiedName("com.devmaster.goatfarm.goatownership.business.FarmGoatHistoricalReproductionQueryBusiness")
                .should().dependOnClassesThat().resideInAPackage("..persistence.repository..")
                .because("the historical reproduction business service must depend only on ports, not repositories")
                .check(IMPORTED_CLASSES);
    }

    @Test
    void reproductionPersistenceAdapterOwnsRepositoryAccess() {
        classes()
                .that().haveFullyQualifiedName("com.devmaster.goatfarm.reproduction.persistence.adapter.FarmGoatHistoricalReproductionPersistenceAdapter")
                .should().dependOnClassesThat().haveFullyQualifiedName("com.devmaster.goatfarm.reproduction.persistence.repository.PregnancyRepository")
                .andShould().dependOnClassesThat().haveFullyQualifiedName("com.devmaster.goatfarm.reproduction.persistence.repository.ReproductiveEventRepository")
                .check(IMPORTED_CLASSES);
    }

    @Test
    void adapterMustImplementNeutralGoatownershipApplicationPort() {
        classes()
                .that().haveFullyQualifiedName("com.devmaster.goatfarm.reproduction.persistence.adapter.FarmGoatHistoricalReproductionPersistenceAdapter")
                .should().implement("com.devmaster.goatfarm.goatownership.application.ports.out.FarmGoatHistoricalReproductionQueryPort")
                .check(IMPORTED_CLASSES);
    }

    @Test
    void noJpaEntityCrossesApplicationBoundary() {
        noClasses()
                .that().resideInAnyPackage("..goatownership.application..", "..goatownership.business..")
                .should().dependOnClassesThat().resideInAPackage("..reproduction.persistence.entity..")
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

    @Test
    void noApiDtoCrossesApplicationBoundary() {
        noClasses()
                .that().resideInAnyPackage("..goatownership.application..", "..goatownership.business..")
                .should().dependOnClassesThat().resideInAPackage("..reproduction.api.dto..")
                .because("HTTP DTOs must not cross the application layer")
                .check(IMPORTED_CLASSES);
    }

    @Test
    void reproductionAdapterMustNotImportGoatownershipPersistenceRepositories() {
        noClasses()
                .that().haveFullyQualifiedName("com.devmaster.goatfarm.reproduction.persistence.adapter.FarmGoatHistoricalReproductionPersistenceAdapter")
                .should().dependOnClassesThat().resideInAPackage("..goatownership.persistence..")
                .because("reproduction adapter must not depend on goatownership persistence repositories")
                .check(IMPORTED_CLASSES);
    }

    @Test
    void controllerMustNotDependOnRepositories() {
        noClasses()
                .that().haveFullyQualifiedName("com.devmaster.goatfarm.goatownership.api.controller.FarmGoatRegistryController")
                .should().dependOnClassesThat().resideInAPackage("..persistence.repository..")
                .because("controller must depend only on use cases and mappers, never repositories")
                .check(IMPORTED_CLASSES);
    }

    @Test
    void reproductionApiMapperMustNotDependOnRepositories() {
        noClasses()
                .that().haveFullyQualifiedName("com.devmaster.goatfarm.goatownership.api.mapper.FarmGoatHistoricalReproductionApiMapper")
                .should().dependOnClassesThat().resideInAPackage("..persistence.repository..")
                .because("API mapper must depend only on DTOs and application models, never repositories")
                .check(IMPORTED_CLASSES);
    }

    @Test
    void apiDtosMustNotDependOnJpaEntities() {
        noClasses()
                .that().resideInAPackage("..goatownership.api.dto..")
                .should().dependOnClassesThat().resideInAnyPackage("..persistence.entity..", "jakarta.persistence..")
                .because("HTTP DTOs must not depend on JPA entities")
                .check(IMPORTED_CLASSES);
    }

    @Test
    void apiDtosMustNotDependOnSpringDataDomain() {
        noClasses()
                .that().resideInAPackage("..goatownership.api.dto..")
                .should().dependOnClassesThat().resideInAPackage("org.springframework.data.domain..")
                .because("HTTP DTOs must not depend on Spring Data domain types")
                .check(IMPORTED_CLASSES);
    }
}
