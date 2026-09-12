package com.devmaster.goatfarm.architecture;

import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Explicit, source-controlled guard for the current application-port JPA debt.
 *
 * <p>The allowlist is intentionally an exact set of source/target pairs. This
 * prevents a new leak from replacing a legacy one while keeping the remaining
 * debt visible and reducible in review.</p>
 */
class ApplicationPortPersistenceBoundaryArchUnitTest {

    private static final JavaClasses PRODUCTION_CLASSES = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.devmaster.goatfarm");

    /**
     * Legacy baseline. Update this set in the same reviewed change that removes
     * or intentionally renames one of these dependencies.
     */
    private static final Set<String> EXPECTED_LEGACY_DEPENDENCIES = Set.of(
            "com.devmaster.goatfarm.address.application.ports.out.AddressPersistencePort"
                    + " -> com.devmaster.goatfarm.address.persistence.entity.Address",
            "com.devmaster.goatfarm.article.application.ports.out.ArticlePersistencePort"
                    + " -> com.devmaster.goatfarm.article.persistence.entity.Article",
            "com.devmaster.goatfarm.audit.application.ports.out.OperationalAuditPersistencePort"
                    + " -> com.devmaster.goatfarm.audit.persistence.entity.OperationalAuditEntry",
            "com.devmaster.goatfarm.authority.application.ports.out.PasswordResetTokenPersistencePort"
                    + " -> com.devmaster.goatfarm.authority.persistence.entity.PasswordResetToken",
            "com.devmaster.goatfarm.authority.application.ports.out.RefreshSessionPersistencePort"
                    + " -> com.devmaster.goatfarm.authority.persistence.entity.RefreshSession",
            "com.devmaster.goatfarm.authority.application.ports.out.RolePersistencePort"
                    + " -> com.devmaster.goatfarm.authority.persistence.entity.Role",
            "com.devmaster.goatfarm.authority.application.ports.out.UserPersistencePort"
                    + " -> com.devmaster.goatfarm.authority.persistence.entity.User",
            "com.devmaster.goatfarm.commercial.application.ports.out.CommercialPersistencePort"
                    + " -> com.devmaster.goatfarm.commercial.persistence.entity.AnimalSale",
            "com.devmaster.goatfarm.commercial.application.ports.out.CommercialPersistencePort"
                    + " -> com.devmaster.goatfarm.commercial.persistence.entity.Customer",
            "com.devmaster.goatfarm.commercial.application.ports.out.CommercialPersistencePort"
                    + " -> com.devmaster.goatfarm.commercial.persistence.entity.MilkSale",
            "com.devmaster.goatfarm.commercial.application.ports.out.OperationalFinancePersistencePort"
                    + " -> com.devmaster.goatfarm.commercial.persistence.entity.OperationalExpense",
            "com.devmaster.goatfarm.farm.application.ports.out.GoatFarmPersistencePort"
                    + " -> com.devmaster.goatfarm.farm.persistence.entity.GoatFarm",
            "com.devmaster.goatfarm.health.application.ports.out.HealthEventPersistencePort"
                    + " -> com.devmaster.goatfarm.health.persistence.entity.HealthEvent",
            "com.devmaster.goatfarm.milk.application.ports.out.FarmMilkProductionPersistencePort"
                    + " -> com.devmaster.goatfarm.milk.persistence.entity.FarmMilkProduction",
            "com.devmaster.goatfarm.milk.application.ports.out.MilkProductionPersistencePort"
                    + " -> com.devmaster.goatfarm.milk.persistence.entity.MilkProduction",
            "com.devmaster.goatfarm.phone.application.ports.out.PhonePersistencePort"
                    + " -> com.devmaster.goatfarm.phone.persistence.entity.Phone"
    );

    @Test
    void applicationPortPersistenceDependenciesMustMatchExplicitLegacyBaseline() {
        Set<String> actual = discoverLegacyDependencies();
        Set<String> unexpected = new TreeSet<>(actual);
        unexpected.removeAll(EXPECTED_LEGACY_DEPENDENCIES);
        Set<String> disappeared = new TreeSet<>(EXPECTED_LEGACY_DEPENDENCIES);
        disappeared.removeAll(actual);

        assertEquals(
                EXPECTED_LEGACY_DEPENDENCIES,
                actual,
                () -> "Application-port persistence debt baseline changed."
                        + " Unexpected/new dependencies: " + unexpected
                        + ". Expected dependencies that disappeared: " + disappeared
                        + ". Update the explicit allowlist only as part of the reviewed architectural change."
        );
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
