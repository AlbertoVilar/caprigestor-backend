package com.devmaster.goatfarm.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class ArticleFarmPaginationBoundaryArchUnitTest {

    private static final JavaClasses IMPORTED_CLASSES = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.devmaster.goatfarm");

    @Test
    void articleApplicationAndBusinessMustNotDependOnSpringDataPagination() {
        noClasses().that().resideInAnyPackage("..article.application..", "..article.business..")
                .should().dependOnClassesThat().resideInAnyPackage("org.springframework.data.domain..")
                .check(IMPORTED_CLASSES);
    }

    @Test
    void farmApplicationAndBusinessMustNotDependOnSpringDataPagination() {
        noClasses().that().resideInAnyPackage("..farm.application..", "..farm.business..")
                .should().dependOnClassesThat().resideInAnyPackage("org.springframework.data.domain..")
                .check(IMPORTED_CLASSES);
    }
}
