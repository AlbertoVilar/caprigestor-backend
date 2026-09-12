package com.devmaster.goatfarm.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.assertj.core.api.Assertions.assertThat;

class ReproductionUseCaseDecompositionArchUnitTest {
    private static final JavaClasses PRODUCTION_CLASSES = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.devmaster.goatfarm");

    @Test
    void controllerMustDependOnInboundPortsNotBusinessImplementations() {
        noClasses().that().resideInAPackage("..reproduction.api.controller..")
                .should().dependOnClassesThat().resideInAPackage("..reproduction.business.reproductionservice..").check(PRODUCTION_CLASSES);
    }

    @Test
    void focusedServicesMustNotCallEachOther() {
        var focused = java.util.Set.of("BreedingCommandBusiness", "PregnancyCommandBusiness", "BirthCommandBusiness",
                "WeaningCommandBusiness", "ReproductionQueryBusiness");
        PRODUCTION_CLASSES.stream().filter(c -> focused.contains(c.getSimpleName())).forEach(origin ->
                origin.getDirectDependenciesFromSelf().forEach(dependency -> {
                    String target = dependency.getTargetClass().getSimpleName();
                    if (focused.contains(target) && !target.equals(origin.getSimpleName())) {
                        throw new AssertionError(origin.getName() + " must not depend on " + dependency.getTargetClass().getName());
                    }
                }));
    }

    @Test
    void legacyGodServiceAndBroadCommandPortMustBeAbsent() {
        assertThat(PRODUCTION_CLASSES.stream().noneMatch(c -> c.getName().endsWith("ReproductionBusiness"))).isTrue();
        assertThat(PRODUCTION_CLASSES.stream().noneMatch(c -> c.getName().endsWith("ReproductionCommandUseCase"))).isTrue();
    }
}
