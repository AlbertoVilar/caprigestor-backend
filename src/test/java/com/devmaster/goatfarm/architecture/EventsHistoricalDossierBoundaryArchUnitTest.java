package com.devmaster.goatfarm.architecture;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "com.devmaster.goatfarm")
class EventsHistoricalDossierBoundaryArchUnitTest {
    @ArchTest
    static final ArchRule businessDoesNotDependOnEventsPersistence = noClasses()
            .that().haveFullyQualifiedName("com.devmaster.goatfarm.goatownership.business.FarmGoatHistoricalEventsQueryBusiness")
            .should().dependOnClassesThat().resideInAnyPackage("..events.persistence..");

    @ArchTest
    static final ArchRule adapterImplementsNeutralPort = classes()
            .that().haveFullyQualifiedName("com.devmaster.goatfarm.events.persistence.adapter.FarmGoatHistoricalEventsPersistenceAdapter")
            .should().implement("com.devmaster.goatfarm.goatownership.application.ports.out.FarmGoatHistoricalEventsQueryPort");
}
