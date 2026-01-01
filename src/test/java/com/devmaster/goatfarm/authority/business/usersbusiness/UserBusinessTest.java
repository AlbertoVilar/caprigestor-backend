package com.devmaster.goatfarm.authority.business.usersbusiness;

// Removed unused imports
// import org.springframework.web.context.request.RequestContextHolder;
// import org.springframework.web.context.request.ServletRequestAttributes;
// import org.springframework.mock.web.MockHttpServletRequest;
// import org.junit.jupiter.api.AfterEach;

import com.devmaster.goatfarm.authority.business.bo.UserRequestVO;
import com.devmaster.goatfarm.authority.business.bo.UserResponseVO;
import com.devmaster.goatfarm.application.ports.out.RolePersistencePort;
import com.devmaster.goatfarm.application.ports.out.UserPersistencePort;
import com.devmaster.goatfarm.authority.mapper.UserMapper;
import com.devmaster.goatfarm.authority.model.entity.Role;
import com.devmaster.goatfarm.authority.model.entity.User;
import com.devmaster.goatfarm.config.exceptions.DuplicateEntityException;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserBusinessTest {

    @Mock
    private UserPersistencePort userPort;

    @Mock
    private RolePersistencePort rolePort;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

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

    // Removed tearDown method
    // @AfterEach
    // void tearDown() {
    //    RequestContextHolder.resetRequestAttributes();
    // }

    @Test
    @DisplayName("Deve criar usuário com sucesso quando não há duplicidade")
    void shouldCreateUserSuccessfully() {
        when(userPort.findByEmail("joao@email.com")).thenReturn(Optional.empty());
        when(userPort.findByCpf("12345678900")).thenReturn(Optional.empty());
        when(rolePort.findByAuthority("ROLE_OPERATOR")).thenReturn(Optional.of(operatorRole));
        when(passwordEncoder.encode("senha123")).thenReturn("$2a$10$hashedPassword");

        when(userMapper.toEntity(any(UserRequestVO.class))).thenReturn(userEntity);
        when(userPort.save(any(User.class))).thenReturn(userEntity);
        when(userMapper.toResponseVO(any(User.class))).thenReturn(userResponseVO);

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
        verify(userMapper, times(1)).toResponseVO(any(User.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao salvar usuário com email duplicado")
    void shouldThrowExceptionWhenSavingUserWithDuplicateEmail() {
        when(userPort.findByEmail("joao@email.com")).thenReturn(Optional.of(new User()));

        assertThrows(DuplicateEntityException.class, () -> userBusiness.saveUser(userRequestVO));
    }
}