package com.devmaster.goatfarm.architecture;

import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Structural guard for application ports. Application ports are technology
 * neutral and must never expose JPA persistence entities.
 */
class ApplicationPortPersistenceBoundaryArchUnitTest {

    private static final JavaClasses PRODUCTION_CLASSES = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.devmaster.goatfarm");

    @Test
    void applicationPortsMustNotDependOnPersistenceEntities() {
        Set<String> actual = discoverLegacyDependencies();
        assertTrue(actual.isEmpty(),
                () -> "Application ports must not depend on persistence entities: " + actual);
    }

    private static Set<String> discoverLegacyDependencies() {
        return PRODUCTION_CLASSES.stream()
                .filter(ApplicationPortPersistenceBoundaryArchUnitTest::isApplicationPort)
                .flatMap(javaClass -> javaClass.getDirectDependenciesFromSelf().stream())
                .filter(ApplicationPortPersistenceBoundaryArchUnitTest::targetsPersistenceEntity)
                .map(ApplicationPortPersistenceBoundaryArchUnitTest::format)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    private static boolean isApplicationPort(com.tngtech.archunit.core.domain.JavaClass javaClass) {
        return javaClass.getPackageName().contains(".application.ports.");
    }

    private static boolean targetsPersistenceEntity(Dependency dependency) {
        return dependency.getTargetClass().getPackageName().contains(".persistence.entity");
    }

    private static String format(Dependency dependency) {
        return dependency.getOriginClass().getName() + " -> " + dependency.getTargetClass().getName();
    }
}
