package com.devmaster.goatfarm.authority.business;

import com.devmaster.goatfarm.authority.application.ports.in.CurrentPrincipalQueryUseCase;
import com.devmaster.goatfarm.authority.application.ports.out.*;
import com.devmaster.goatfarm.authority.business.bo.*;
import com.devmaster.goatfarm.authority.business.mapper.AuthorityBusinessMapper;
import com.devmaster.goatfarm.authority.business.usersbusiness.UserBusiness;
import com.devmaster.goatfarm.config.exceptions.DuplicateEntityException;
import com.devmaster.goatfarm.config.exceptions.custom.UnauthorizedException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserBusinessTest {
    @Mock UserPersistencePort userPort; @Mock RolePersistencePort rolePort; @Mock AuthorityBusinessMapper mapper;
    @Mock PasswordHashingPort hashing; @Mock RefreshSessionPersistencePort sessions; @Mock CurrentPrincipalQueryUseCase principalQuery;
    @InjectMocks UserBusiness business;
    private UserRequestVO request() { UserRequestVO r = new UserRequestVO(); r.setName("João Silva"); r.setEmail("joao@email.com"); r.setCpf("12345678900"); r.setPassword("senha123"); r.setConfirmPassword("senha123"); r.setRoles(List.of("ROLE_OPERATOR")); return r; }
    private AuthorityAccount account() { return new AuthorityAccount(1L, "João Silva", "joao@email.com", "12345678900", "encoded", Set.of("ROLE_OPERATOR")); }
    @Test void shouldCreateUserSuccessfully() {
        AuthorityAccount a = account(); when(userPort.findByEmail(anyString())).thenReturn(Optional.empty()); when(userPort.findByCpf(anyString())).thenReturn(Optional.empty());
        when(rolePort.findByAuthority("ROLE_OPERATOR")).thenReturn(Optional.of(new AuthorityRole("ROLE_OPERATOR", "Operator"))); when(hashing.hash("senha123")).thenReturn("encoded"); when(mapper.toAccount(any())).thenReturn(a); when(userPort.save(any())).thenReturn(a); when(mapper.toResponseVO(a)).thenReturn(new UserResponseVO(1L, "João Silva", "joao@email.com", "12345678900", List.of("ROLE_OPERATOR")));
        assertThat(business.saveUser(request()).getId()).isEqualTo(1L); verify(userPort).save(any(AuthorityAccount.class));
    }
    @Test void duplicateEmailIsRejected() { when(userPort.findByEmail("joao@email.com")).thenReturn(Optional.of(account())); assertThrows(DuplicateEntityException.class, () -> business.saveUser(request())); }
    @Test void operatorCannotUpdatePassword() { when(principalQuery.requireCurrent()).thenReturn(new AuthenticatedPrincipal(1L, "a", "A", Set.of("ROLE_OPERATOR"))); assertThrows(UnauthorizedException.class, () -> business.updatePassword(2L, "novaSenha123")); verifyNoInteractions(hashing); }
    @Test void adminCanUpdatePassword() { when(principalQuery.requireCurrent()).thenReturn(new AuthenticatedPrincipal(1L, "a", "A", Set.of("ROLE_ADMIN"))); when(hashing.hash("novaSenha123")).thenReturn("encoded-new"); business.updatePassword(2L, "novaSenha123"); verify(userPort).updatePassword(2L, "encoded-new"); verify(sessions).revokeAllForUser(eq(2L), any(Instant.class), eq("password_changed_by_admin")); }
    @Test void adminRoleChangeRevokesSessions() { when(principalQuery.requireCurrent()).thenReturn(new AuthenticatedPrincipal(1L, "a", "A", Set.of("ROLE_ADMIN"))); when(rolePort.findByAuthority("ROLE_OPERATOR")).thenReturn(Optional.of(new AuthorityRole("ROLE_OPERATOR", "Operator"))); when(userPort.findById(2L)).thenReturn(Optional.of(account())); when(userPort.save(any())).thenReturn(account()); when(mapper.toResponseVO(any())).thenReturn(new UserResponseVO(1L, "João Silva", "joao@email.com", "12345678900", List.of("ROLE_OPERATOR"))); business.updateRoles(2L, List.of("ROLE_OPERATOR")); verify(sessions).revokeAllForUser(eq(1L), any(Instant.class), eq("roles_changed")); }
    @Test void operatorCannotUpdateRolesBeforeResolution() { when(principalQuery.requireCurrent()).thenReturn(new AuthenticatedPrincipal(1L, "a", "A", Set.of("ROLE_OPERATOR"))); assertThrows(UnauthorizedException.class, () -> business.updateRoles(2L, List.of("ROLE_ADMIN"))); verifyNoInteractions(rolePort, userPort); }
}
