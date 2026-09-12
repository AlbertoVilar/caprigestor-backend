package com.devmaster.goatfarm.authority.business;

import com.devmaster.goatfarm.authority.business.bo.UserRequestVO;
import com.devmaster.goatfarm.authority.business.bo.UserResponseVO;
import com.devmaster.goatfarm.authority.application.ports.out.RolePersistencePort;
import com.devmaster.goatfarm.authority.application.ports.out.UserPersistencePort;
import com.devmaster.goatfarm.authority.application.ports.out.RefreshSessionPersistencePort;
import com.devmaster.goatfarm.authority.application.ports.in.CurrentPrincipalQueryUseCase;
import com.devmaster.goatfarm.authority.business.bo.AuthenticatedPrincipal;
import com.devmaster.goatfarm.authority.business.mapper.AuthorityBusinessMapper;
import com.devmaster.goatfarm.authority.persistence.entity.Role;
import com.devmaster.goatfarm.authority.persistence.entity.User;
import com.devmaster.goatfarm.authority.business.usersbusiness.UserBusiness;
import com.devmaster.goatfarm.config.exceptions.DuplicateEntityException;
import com.devmaster.goatfarm.config.exceptions.custom.UnauthorizedException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class UserBusinessTest {

    @Mock
    private UserPersistencePort userPort;

    @Mock
    private RolePersistencePort rolePort;

    @Mock
    private AuthorityBusinessMapper authorityBusinessMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RefreshSessionPersistencePort refreshSessionPersistencePort;

    @Mock
    private CurrentPrincipalQueryUseCase currentPrincipalQuery;

    @InjectMocks
    private UserBusiness userBusiness;

    private UserRequestVO userRequestVO;
    private User userEntity;
    private UserResponseVO userResponseVO;
    private Role operatorRole;

    @BeforeEach
    void setUp() {
        // Removed RequestContextHolder setup
        
        userRequestVO = new UserRequestVO();
        userRequestVO.setName("João Silva");
        userRequestVO.setEmail("joao@email.com");
        userRequestVO.setCpf("12345678900");
        userRequestVO.setPassword("senha123");
        userRequestVO.setConfirmPassword("senha123");
        userRequestVO.setRoles(List.of("ROLE_OPERATOR"));

        userEntity = new User();
        userEntity.setId(1L);
        userEntity.setName("João Silva");
        userEntity.setEmail("joao@email.com");
        userEntity.setCpf("12345678900");

        userResponseVO = new UserResponseVO(
                1L,
                "João Silva",
                "joao@email.com",
                "12345678900",
                List.of("ROLE_OPERATOR"));

        operatorRole = new Role();
        operatorRole.setId(1L);
        operatorRole.setAuthority("ROLE_OPERATOR");
    }

    @AfterEach
    void clearSecurityContext() {
    }

    @Test
    @DisplayName("Deve criar usuário com sucesso quando não há duplicidade")
    void shouldCreateUserSuccessfully() {
        when(userPort.findByEmail("joao@email.com")).thenReturn(Optional.empty());
        when(userPort.findByCpf("12345678900")).thenReturn(Optional.empty());
        when(rolePort.findByAuthority("ROLE_OPERATOR")).thenReturn(Optional.of(operatorRole));
        when(passwordEncoder.encode("senha123")).thenReturn("$2a$10$hashedPassword");

        when(authorityBusinessMapper.toEntity(any(UserRequestVO.class))).thenReturn(userEntity);
        when(userPort.save(any(User.class))).thenReturn(userEntity);
        when(authorityBusinessMapper.toResponseVO(any(User.class))).thenReturn(userResponseVO);

        UserResponseVO resultado = userBusiness.saveUser(userRequestVO);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getName()).isEqualTo("João Silva");
        assertThat(resultado.getEmail()).isEqualTo("joao@email.com");

        verify(userPort, times(1)).findByEmail("joao@email.com");
        verify(userPort, times(1)).findByCpf("12345678900");
        verify(passwordEncoder, times(1)).encode("senha123");
        verify(rolePort, times(1)).findByAuthority("ROLE_OPERATOR");
        verify(userPort, times(1)).save(any(User.class));
        verify(authorityBusinessMapper, times(1)).toResponseVO(any(User.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao salvar usuário com email duplicado")
    void shouldThrowExceptionWhenSavingUserWithDuplicateEmail() {
        when(userPort.findByEmail("joao@email.com")).thenReturn(Optional.of(new User()));

        assertThrows(DuplicateEntityException.class, () -> userBusiness.saveUser(userRequestVO));
    }

    @Test
    @DisplayName("Operador não deve alterar senha pela API administrativa nem iniciar a criptografia")
    void operatorCannotUpdatePasswordBeforeEncoding() {
        authenticateAs(userEntity, operatorRole);

        assertThrows(UnauthorizedException.class, () -> userBusiness.updatePassword(2L, "novaSenha123"));

        verify(passwordEncoder, never()).encode(any());
        verify(userPort, never()).updatePassword(any(), any());
    }

    @Test
    @DisplayName("Operador não deve alterar nem a própria senha pela API administrativa")
    void operatorCannotUpdateOwnPasswordThroughAdministrativeUseCase() {
        authenticateAs(userEntity, operatorRole);

        assertThrows(UnauthorizedException.class, () -> userBusiness.updatePassword(1L, "novaSenha123"));

        verify(passwordEncoder, never()).encode(any());
        verify(userPort, never()).updatePassword(any(), any());
    }

    @Test
    @DisplayName("Administrador deve alterar senha após autorização")
    void adminCanUpdatePasswordAfterAuthorization() {
        Role adminRole = role("ROLE_ADMIN");
        authenticateAs(userEntity, adminRole);
        when(passwordEncoder.encode("novaSenha123")).thenReturn("senha-codificada");

        userBusiness.updatePassword(2L, "novaSenha123");

        verify(passwordEncoder).encode("novaSenha123");
        verify(userPort).updatePassword(2L, "senha-codificada");
        verify(refreshSessionPersistencePort).revokeAllForUser(eq(2L), any(Instant.class), eq("password_changed_by_admin"));
    }

    @Test
    @DisplayName("Mudança administrativa de roles deve revogar sessões existentes")
    void adminRoleChangeRevokesExistingSessions() {
        Role adminRole = role("ROLE_ADMIN");
        authenticateAs(userEntity, adminRole);
        when(rolePort.findByAuthority("ROLE_OPERATOR")).thenReturn(Optional.of(operatorRole));
        when(userPort.findById(2L)).thenReturn(Optional.of(userEntity));
        when(userPort.save(userEntity)).thenReturn(userEntity);
        when(authorityBusinessMapper.toResponseVO(userEntity)).thenReturn(userResponseVO);

        userBusiness.updateRoles(2L, List.of("ROLE_OPERATOR"));

        verify(refreshSessionPersistencePort).revokeAllForUser(eq(1L), any(Instant.class), eq("roles_changed"));
    }

    @Test
    @DisplayName("Operador não deve resolver ou persistir roles antes da autorização")
    void operatorCannotUpdateRolesBeforeResolutionOrPersistence() {
        authenticateAs(userEntity, operatorRole);

        assertThrows(UnauthorizedException.class,
                () -> userBusiness.updateRoles(2L, List.of("ROLE_ADMIN")));

        verify(rolePort, never()).findByAuthority(any());
        verify(userPort, never()).findById(any());
        verify(userPort, never()).save(any());
    }

    @Test
    @DisplayName("Mudança de roles deve ser autorizada antes de qualquer mutação do usuário")
    void operatorCannotMutateUserWhenRolesAreRequested() {
        authenticateAs(userEntity, operatorRole);
        userRequestVO.setRoles(List.of("ROLE_ADMIN"));

        assertThrows(UnauthorizedException.class, () -> userBusiness.updateUser(2L, userRequestVO));

        verify(userPort, never()).findById(any());
        verify(rolePort, never()).findByAuthority(any());
        verify(passwordEncoder, never()).encode(any());
        verify(userPort, never()).save(any());
    }

    private void authenticateAs(User user, Role assignedRole) {
        user.getRoles().clear();
        user.addRole(assignedRole);
        when(currentPrincipalQuery.requireCurrent()).thenReturn(new AuthenticatedPrincipal(
                user.getId(), user.getEmail(), user.getName(), java.util.Set.of(assignedRole.getAuthority())));
    }

    private Role role(String authority) {
        Role role = new Role();
        role.setAuthority(authority);
        return role;
    }
}
