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
}
