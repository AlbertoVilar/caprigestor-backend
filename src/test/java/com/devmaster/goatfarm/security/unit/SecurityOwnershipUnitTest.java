package com.devmaster.goatfarm.security.unit;

import com.devmaster.goatfarm.authority.application.ports.in.CurrentPrincipalQueryUseCase;
import com.devmaster.goatfarm.authority.application.ports.out.FarmAccessQueryPort;
import com.devmaster.goatfarm.authority.business.bo.AuthenticatedPrincipal;
import com.devmaster.goatfarm.authority.persistence.entity.Role;
import com.devmaster.goatfarm.authority.persistence.entity.User;
import com.devmaster.goatfarm.config.security.OwnershipService;
import com.devmaster.goatfarm.farm.application.ports.out.FarmOwnerQueryPort;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecurityOwnershipUnitTest {

    @Mock
    private CurrentPrincipalQueryUseCase currentPrincipalQuery;
    @Mock
    private FarmAccessQueryPort farmAccessQueryPort;
    @Mock
    private FarmOwnerQueryPort farmOwnerQueryPort;

    private OwnershipService ownershipService;

    private User currentUser;

    @BeforeEach
    void setUp() {
        currentPrincipalQuery = mock(CurrentPrincipalQueryUseCase.class);
        ownershipService = new OwnershipService(currentPrincipalQuery, farmAccessQueryPort, farmOwnerQueryPort);

        currentUser = new User();
        currentUser.setId(1L);
        currentUser.setEmail("user@test.com");
        
        lenient().when(currentPrincipalQuery.requireCurrent()).thenAnswer(inv -> new AuthenticatedPrincipal(
                currentUser.getId(), currentUser.getEmail(), currentUser.getName(),
                currentUser.getRoles().stream().map(Role::getAuthority).collect(java.util.stream.Collectors.toSet())));
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void canManageFarm_shouldReturnTrue_whenAdmin() {
        Role adminRole = new Role();
        adminRole.setAuthority("ROLE_ADMIN");
        currentUser.getRoles().clear();
        currentUser.addRole(adminRole);

        // No need to mock farm repo for admin as it returns early
        boolean result = ownershipService.canManageFarm(10L);

        assertTrue(result);
    }

    @Test
    void canManageFarm_shouldReturnTrue_whenOwnerOfFarm() {
        Role ownerRole = new Role();
        ownerRole.setAuthority("ROLE_FARM_OWNER");
        currentUser.getRoles().clear();
        currentUser.addRole(ownerRole);

        // Mock farm lookup
        when(farmOwnerQueryPort.findOwnerId(10L)).thenReturn(Optional.of(1L));

        boolean result = ownershipService.canManageFarm(10L);

        assertTrue(result);
    }

    @Test
    void canManageFarm_shouldReturnFalse_whenOwnerOfDifferentFarm() {
        Role ownerRole = new Role();
        ownerRole.setAuthority("ROLE_FARM_OWNER");
        currentUser.getRoles().clear();
        currentUser.addRole(ownerRole);

        when(farmOwnerQueryPort.findOwnerId(10L)).thenReturn(Optional.of(99L));

        boolean result = ownershipService.canManageFarm(10L);

        assertFalse(result);
    }

    @Test
    void canManageFarm_shouldReturnFalse_whenDirectOwnerHasNonOfficialRole() {
        Role nonOfficialRole = new Role();
        nonOfficialRole.setAuthority("ROLE_OWNER");
        currentUser.getRoles().clear();
        currentUser.addRole(nonOfficialRole);

        assertFalse(ownershipService.canManageFarm(10L));
    }

    @Test
    void verifyFarmOwnership_shouldRejectDirectOwnerWithoutOfficialRole() {
        Role nonOfficialRole = new Role();
        nonOfficialRole.setAuthority("ROLE_OWNER");
        currentUser.getRoles().clear();
        currentUser.addRole(nonOfficialRole);

        assertThrows(AccessDeniedException.class, () -> ownershipService.verifyFarmOwnership(10L));
    }

    @Test
    void verifyFarmOwnership_shouldAllowOfficialOwnerOfFarm() {
        Role ownerRole = new Role();
        ownerRole.setAuthority("ROLE_FARM_OWNER");
        currentUser.getRoles().clear();
        currentUser.addRole(ownerRole);
        when(farmOwnerQueryPort.findOwnerId(10L)).thenReturn(Optional.of(1L));

        assertDoesNotThrow(() -> ownershipService.verifyFarmOwnership(10L));
    }

    @Test
    void canManageFarm_shouldReturnTrue_whenOperatorLinkedToFarm() {
        Role operatorRole = new Role();
        operatorRole.setAuthority("ROLE_OPERATOR");
        currentUser.getRoles().clear();
        currentUser.addRole(operatorRole);

        when(farmAccessQueryPort.existsOperatorLink(10L, 1L)).thenReturn(true);

        boolean result = ownershipService.canManageFarm(10L);

        assertTrue(result);
    }

    @Test
    void canManageFarm_shouldReturnFalse_whenOperatorNotLinked() {
        Role operatorRole = new Role();
        operatorRole.setAuthority("ROLE_OPERATOR");
        currentUser.getRoles().clear();
        currentUser.addRole(operatorRole);

        when(farmAccessQueryPort.existsOperatorLink(10L, 1L)).thenReturn(false);

        boolean result = ownershipService.canManageFarm(10L);

        assertFalse(result);
    }

}
