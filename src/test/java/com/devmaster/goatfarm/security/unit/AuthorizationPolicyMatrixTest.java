package com.devmaster.goatfarm.security.unit;

import com.devmaster.goatfarm.authority.application.ports.out.FarmAccessQueryPort;
import com.devmaster.goatfarm.authority.application.ports.out.UserPersistencePort;
import com.devmaster.goatfarm.authority.persistence.entity.Role;
import com.devmaster.goatfarm.authority.persistence.entity.User;
import com.devmaster.goatfarm.config.security.OwnershipService;
import com.devmaster.goatfarm.farm.application.ports.out.GoatFarmPersistencePort;
import com.devmaster.goatfarm.farm.persistence.entity.GoatFarm;
import com.devmaster.goatfarm.goat.application.ports.out.GoatReferenceQueryPort;
import com.devmaster.goatfarm.goat.application.routing.GoatReferenceResolver;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/** Executable contract for the global farm-management policy. */
class AuthorizationPolicyMatrixTest {

    @ParameterizedTest(name = "{0}")
    @MethodSource("managementCases")
    void canManageFarm_preservesRoleLinkageAndCrossFarmMatrix(
            String scenario,
            String role,
            boolean sameFarmOwner,
            boolean linkedOperator,
            boolean expected
    ) {
        User current = user(1L, role);
        User farmOwner = sameFarmOwner ? current : user(99L, "ROLE_FARM_OWNER");
        GoatFarm farm = new GoatFarm();
        farm.setId(10L);
        farm.setUser(farmOwner);

        UserPersistencePort userPort = Mockito.mock(UserPersistencePort.class);
        GoatFarmPersistencePort farmPort = Mockito.mock(GoatFarmPersistencePort.class);
        GoatReferenceResolver goatReferenceResolver = new GoatReferenceResolver(
                Mockito.mock(GoatReferenceQueryPort.class));
        FarmAccessQueryPort accessPort = Mockito.mock(FarmAccessQueryPort.class);
        when(userPort.findByEmail("matrix@example.com")).thenReturn(Optional.of(current));
        when(farmPort.findById(10L)).thenReturn(Optional.of(farm));
        when(accessPort.existsOperatorLink(10L, 1L)).thenReturn(linkedOperator);
        authenticate("matrix@example.com");

        boolean result = new OwnershipService(farmPort, userPort, goatReferenceResolver, accessPort).canManageFarm(10L);

        assertThat(result).as(scenario).isEqualTo(expected);
        SecurityContextHolder.clearContext();
    }

    static Stream<Arguments> managementCases() {
        return Stream.of(
                Arguments.of("ADMIN bypasses farm ownership", "ROLE_ADMIN", false, false, true),
                Arguments.of("FARM_OWNER manages own farm", "ROLE_FARM_OWNER", true, false, true),
                Arguments.of("FARM_OWNER cannot cross farms", "ROLE_FARM_OWNER", false, false, false),
                Arguments.of("linked OPERATOR manages operational farm", "ROLE_OPERATOR", false, true, true),
                Arguments.of("unlinked OPERATOR is denied", "ROLE_OPERATOR", false, false, false),
                Arguments.of("legacy owner role is denied", "ROLE_OWNER", true, false, false)
        );
    }

    private static User user(Long id, String roleName) {
        User user = new User();
        user.setId(id);
        Role role = new Role();
        role.setAuthority(roleName);
        user.addRole(role);
        return user;
    }

    private static void authenticate(String email) {
        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext context = Mockito.mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(email);
        when(authentication.getName()).thenReturn(email);
        SecurityContextHolder.setContext(context);
    }
}
