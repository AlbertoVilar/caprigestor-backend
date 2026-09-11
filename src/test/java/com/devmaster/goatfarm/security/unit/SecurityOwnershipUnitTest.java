package com.devmaster.goatfarm.security.unit;

import com.devmaster.goatfarm.authority.application.ports.out.UserPersistencePort;
import com.devmaster.goatfarm.authority.application.ports.out.FarmAccessQueryPort;
import com.devmaster.goatfarm.authority.business.bo.AuthenticatedPrincipal;
import com.devmaster.goatfarm.authority.persistence.entity.Role;
import com.devmaster.goatfarm.authority.persistence.entity.User;
import com.devmaster.goatfarm.config.security.OwnershipService;
import com.devmaster.goatfarm.farm.application.ports.out.GoatFarmPersistencePort;
import com.devmaster.goatfarm.farm.persistence.entity.GoatFarm;
import com.devmaster.goatfarm.goat.application.routing.GoatReferenceResolver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

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
    private GoatFarmPersistencePort goatFarmPort;
    @Mock
    private UserPersistencePort userPort;
    @Mock
    private GoatReferenceResolver goatReferenceResolver;
    @Mock
    private FarmAccessQueryPort farmAccessQueryPort;

    @InjectMocks
    private OwnershipService ownershipService;

    private User currentUser;
    private GoatFarm farm;

    @BeforeEach
    void setUp() {
        // Setup Security Context
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        currentUser = new User();
        currentUser.setId(1L);
        currentUser.setEmail("user@test.com");
        
        lenient().when(authentication.isAuthenticated()).thenReturn(true);
        lenient().when(authentication.getPrincipal()).thenReturn("user@test.com"); // Not anonymous
        lenient().when(authentication.getName()).thenReturn("user@test.com");
        
        lenient().when(userPort.findByEmail("user@test.com")).thenReturn(Optional.of(currentUser));

        farm = new GoatFarm();
        farm.setId(10L);
        farm.setUser(currentUser);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
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
        when(goatFarmPort.findById(10L)).thenReturn(Optional.of(farm));

        boolean result = ownershipService.canManageFarm(10L);

        assertTrue(result);
    }

    @Test
    void canManageFarm_shouldReturnFalse_whenOwnerOfDifferentFarm() {
        Role ownerRole = new Role();
        ownerRole.setAuthority("ROLE_FARM_OWNER");
        currentUser.getRoles().clear();
        currentUser.addRole(ownerRole);

        User otherUser = new User();
        otherUser.setId(99L);
        farm.setUser(otherUser);

        when(goatFarmPort.findById(10L)).thenReturn(Optional.of(farm));

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
        when(goatFarmPort.findById(10L)).thenReturn(Optional.of(farm));

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

    @Test
    void getCurrentPrincipal_shouldExposeOnlyApplicationIdentity() {
        currentUser.setName("Alberto");
        Role ownerRole = new Role();
        ownerRole.setAuthority("ROLE_FARM_OWNER");
        currentUser.addRole(ownerRole);

        AuthenticatedPrincipal principal = ownershipService.getCurrentPrincipal();

        assertEquals(1L, principal.id());
        assertEquals("user@test.com", principal.email());
        assertEquals("Alberto", principal.name());
        assertTrue(principal.hasAuthority("ROLE_FARM_OWNER"));
    }
}
