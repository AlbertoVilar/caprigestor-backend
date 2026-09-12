package com.devmaster.goatfarm.security.unit;

import com.devmaster.goatfarm.authority.application.ports.out.FarmAccessQueryPort;
import com.devmaster.goatfarm.authority.application.ports.in.CurrentPrincipalQueryUseCase;
import com.devmaster.goatfarm.farm.application.ports.out.FarmOwnerQueryPort;
import com.devmaster.goatfarm.authority.persistence.entity.Role;
import com.devmaster.goatfarm.authority.persistence.entity.User;
import com.devmaster.goatfarm.config.security.OwnershipService;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

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
        FarmAccessQueryPort accessPort = Mockito.mock(FarmAccessQueryPort.class);
        FarmOwnerQueryPort ownerPort = Mockito.mock(FarmOwnerQueryPort.class);
        CurrentPrincipalQueryUseCase principalQuery = Mockito.mock(CurrentPrincipalQueryUseCase.class);
        when(principalQuery.requireCurrent()).thenReturn(new com.devmaster.goatfarm.authority.business.bo.AuthenticatedPrincipal(
                1L, "matrix@example.com", "Matrix", java.util.Set.of(role)));
        when(ownerPort.findOwnerId(10L)).thenReturn(Optional.of(sameFarmOwner ? 1L : 99L));
        when(accessPort.existsOperatorLink(10L, 1L)).thenReturn(linkedOperator);

        boolean result = new OwnershipService(principalQuery, accessPort, ownerPort).canManageFarm(10L);

        assertThat(result).as(scenario).isEqualTo(expected);
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

}
