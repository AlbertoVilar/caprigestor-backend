package com.devmaster.goatfarm.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Prevents the Farm persistence entity from rebuilding the removed bidirectional
 * ORM collection to Goat persistence. Goat remains the owner of the farm
 * reference and Farm has no need to materialize the herd graph.
 */
class FarmGoatPersistenceBoundaryArchUnitTest {

    private static final JavaClasses IMPORTED_CLASSES = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.devmaster.goatfarm");

    @Test
    void farmPersistenceEntityMustNotDependOnGoatPersistenceEntity() {
        ArchRule rule = noClasses()
                .that().haveFullyQualifiedName("com.devmaster.goatfarm.farm.persistence.entity.GoatFarm")
                .should().dependOnClassesThat()
                .haveFullyQualifiedName("com.devmaster.goatfarm.goat.persistence.entity.GoatEntity")
                .because("Farm must not rebuild the removed bidirectional Goat JPA collection");

        rule.check(IMPORTED_CLASSES);
    }
}
