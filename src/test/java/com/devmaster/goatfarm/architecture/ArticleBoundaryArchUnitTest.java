package com.devmaster.goatfarm.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class ArticleBoundaryArchUnitTest {

    @Test
    void articleApplicationAndBusinessMustNotDependOnPersistenceEntities() {
        JavaClasses imported = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.devmaster.goatfarm");

        noClasses()
                .that().resideInAnyPackage("..article.application..", "..article.business..")
                .should().dependOnClassesThat().resideInAnyPackage("..article.persistence.entity..")
                .because("Article application and business layers must remain independent from JPA entities")
                .check(imported);
    }
}
