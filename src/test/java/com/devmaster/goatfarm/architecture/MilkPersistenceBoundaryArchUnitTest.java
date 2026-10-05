package com.devmaster.goatfarm.architecture;

import org.junit.jupiter.api.Test;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;

class MilkPersistenceBoundaryArchUnitTest {
    @Test
    void milkProductionBusinessMustNotDependOnPersistence() {
        var classes = new ClassFileImporter().withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.devmaster.goatfarm");
        noClasses().that().resideInAnyPackage("..milk.business..")
                .should().dependOnClassesThat().resideInAnyPackage("..milk.persistence.entity..", "..milk.persistence.projection..")
                .check(classes);
    }

    @Test
    void farmMilkBusinessMustNotDependOnFarmPersistence() {
        var classes = new ClassFileImporter().withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.devmaster.goatfarm");
        noClasses().that().haveFullyQualifiedName("com.devmaster.goatfarm.milk.business.farmmilkproductionservice.FarmMilkProductionBusiness")
                .should().dependOnClassesThat().resideInAnyPackage("..farm.persistence..")
                .check(classes);
    }

    @Test
    void pregnancyTableSqlMustNotReturnToMilkPersistence() throws Exception {
        Path root = Path.of("src/main/java/com/devmaster/goatfarm/milk/persistence");
        if (!Files.exists(root)) return;
        try (Stream<Path> files = Files.walk(root)) {
            boolean containsPregnancySql = files.filter(Files::isRegularFile).anyMatch(path -> {
                try { return Files.readString(path).toLowerCase().contains("from pregnancy")
                        || Files.readString(path).toLowerCase().contains("join pregnancy"); }
                catch (Exception e) { throw new IllegalStateException(e); }
            });
            assertTrue(!containsPregnancySql, "Milk persistence must not query pregnancy directly");
        }
    }
}
