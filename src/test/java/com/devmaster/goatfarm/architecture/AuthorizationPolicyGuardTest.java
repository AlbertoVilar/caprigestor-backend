package com.devmaster.goatfarm.architecture;

import com.devmaster.goatfarm.config.security.authorization.AdminOnly;
import com.devmaster.goatfarm.config.security.authorization.AuthenticatedFarmRead;
import com.devmaster.goatfarm.config.security.authorization.CanManageFarm;
import com.devmaster.goatfarm.config.security.authorization.FarmOwnerOnly;
import com.devmaster.goatfarm.config.security.authorization.PublicEndpoint;
import com.devmaster.goatfarm.goat.api.controller.GoatController;
import com.devmaster.goatfarm.goat.api.dto.GoatRegistrationRectificationRequestDTO;
import org.junit.jupiter.api.Test;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Guard against adding a farm-scoped endpoint without an explicit policy.
 *
 * The guard intentionally checks intent markers in addition to raw
 * {@link PreAuthorize}. This keeps legitimate exceptions visible while making
 * an unannotated endpoint fail at test time instead of relying on memory.
 */
class AuthorizationPolicyGuardTest {

    private static final List<Class<? extends Annotation>> HTTP_MAPPINGS = List.of(
            GetMapping.class, PostMapping.class, PutMapping.class,
            PatchMapping.class, DeleteMapping.class, RequestMapping.class
    );

    @Test
    void everyFarmControllerEndpointMustDeclareAuthorizationIntent() throws Exception {
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(RestController.class));

        List<String> violations = new ArrayList<>();
        scanner.findCandidateComponents("com.devmaster.goatfarm").forEach(candidate -> {
            try {
                Class<?> controller = Class.forName(candidate.getBeanClassName());
                if (!isFarmController(controller)) {
                    return;
                }
                for (Method method : controller.getDeclaredMethods()) {
                    if (!isHttpEndpoint(method)) {
                        continue;
                    }
                    if (!hasAuthorizationIntent(controller, method)) {
                        violations.add(controller.getSimpleName() + "#" + method.getName()
                                + " has no explicit authorization intent");
                    }
                    if (usesFarmPolicy(controller, method) && !hasFarmIdParameter(method)) {
                        violations.add(controller.getSimpleName() + "#" + method.getName()
                                + " uses a farm policy without a farmId parameter");
                    }
                }
            } catch (ClassNotFoundException exception) {
                violations.add("Could not load controller " + candidate.getBeanClassName());
            }
        });

        assertThat(violations)
                .withFailMessage("Farm authorization drift detected: %s", violations)
                .isEmpty();
    }

    @Test
    void semanticPoliciesMustRemainBackedBySpringMethodSecurity() {
        assertThat(CanManageFarm.class.getAnnotation(PreAuthorize.class).value())
                .isEqualTo("@ownershipService.canManageFarm(#farmId)");
        assertThat(FarmOwnerOnly.class.getAnnotation(PreAuthorize.class).value())
                .isEqualTo("hasAuthority('ROLE_ADMIN') or (hasAuthority('ROLE_FARM_OWNER') and @ownershipService.isFarmOwner(#farmId))");
        assertThat(AdminOnly.class.getAnnotation(PreAuthorize.class).value())
                .isEqualTo("hasAuthority('ROLE_ADMIN')");
    }

    @Test
    void goatRegistrationEndpointsMustRetainOwnerOnlyPolicy() throws Exception {
        Method rectification = GoatController.class.getDeclaredMethod(
                "rectifyRegistration", Long.class, String.class,
                GoatRegistrationRectificationRequestDTO.class);
        Method history = GoatController.class.getDeclaredMethod(
                "registrationHistory", Long.class, String.class);

        assertThat(rectification.isAnnotationPresent(FarmOwnerOnly.class)).isTrue();
        assertThat(history.isAnnotationPresent(FarmOwnerOnly.class)).isTrue();
    }

    private boolean isFarmController(Class<?> controller) {
        RequestMapping mapping = controller.getAnnotation(RequestMapping.class);
        if (mapping == null) {
            return false;
        }
        return java.util.stream.Stream.of(mapping.value(), mapping.path())
                .flatMap(java.util.Arrays::stream)
                .anyMatch(path -> path.startsWith("/api/v1/goatfarms"));
    }

    private boolean isHttpEndpoint(Method method) {
        return HTTP_MAPPINGS.stream().anyMatch(method::isAnnotationPresent);
    }

    private boolean hasAuthorizationIntent(Class<?> controller, Method method) {
        return hasAny(method, CanManageFarm.class, FarmOwnerOnly.class, AdminOnly.class,
                PublicEndpoint.class, AuthenticatedFarmRead.class, PreAuthorize.class)
                || hasAny(controller, CanManageFarm.class, FarmOwnerOnly.class, AdminOnly.class,
                PublicEndpoint.class, AuthenticatedFarmRead.class, PreAuthorize.class);
    }

    private boolean usesFarmPolicy(Class<?> controller, Method method) {
        return hasAny(method, CanManageFarm.class, FarmOwnerOnly.class)
                || hasAny(controller, CanManageFarm.class, FarmOwnerOnly.class)
                || method.isAnnotationPresent(PreAuthorize.class)
                && method.getAnnotation(PreAuthorize.class).value().contains("#farmId");
    }

    private boolean hasFarmIdParameter(Method method) {
        for (Parameter parameter : method.getParameters()) {
            if ("farmId".equals(parameter.getName())) {
                return true;
            }
            org.springframework.web.bind.annotation.PathVariable pathVariable =
                    parameter.getAnnotation(org.springframework.web.bind.annotation.PathVariable.class);
            if (pathVariable != null && ("farmId".equals(pathVariable.value()) || "farmId".equals(pathVariable.name()))) {
                return true;
            }
        }
        return false;
    }

    @SafeVarargs
    private boolean hasAny(Method method, Class<? extends Annotation>... annotations) {
        for (Class<? extends Annotation> annotation : annotations) {
            if (method.isAnnotationPresent(annotation)) {
                return true;
            }
        }
        return false;
    }

    @SafeVarargs
    private boolean hasAny(Class<?> type, Class<? extends Annotation>... annotations) {
        for (Class<? extends Annotation> annotation : annotations) {
            if (type.isAnnotationPresent(annotation)) {
                return true;
            }
        }
        return false;
    }
}
