package com.devmaster.goatfarm.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class MilkProductionPaginationBoundaryArchUnitTest {

    @Test
    void milkProductionCoreMustNotDependOnSpringDataPagination() {
        JavaClasses imported = new ClassFileImporter().importPackages("com.devmaster.goatfarm");

        noClasses()
                .that().haveFullyQualifiedName("com.devmaster.goatfarm.milk.business.milkproductionservice.MilkProductionBusiness")
                .or().haveFullyQualifiedName("com.devmaster.goatfarm.milk.application.ports.in.MilkProductionUseCase")
                .or().haveFullyQualifiedName("com.devmaster.goatfarm.milk.application.ports.out.MilkProductionPersistencePort")
                .should().dependOnClassesThat().resideInAnyPackage("org.springframework.data.domain..")
                .because("Milk Production pagination must cross the application boundary as neutral PageQuery/PageResult types.")
                .check(imported);
    }
}
