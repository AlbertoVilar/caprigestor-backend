package com.devmaster.goatfarm.authority.business;

// Adicione aos imports existentes:
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.mock.web.MockHttpServletRequest;
import org.junit.jupiter.api.AfterEach;

import com.devmaster.goatfarm.authority.business.bo.UserRequestVO;
import com.devmaster.goatfarm.authority.business.bo.UserResponseVO;
import com.devmaster.goatfarm.authority.dao.RoleDAO;
import com.devmaster.goatfarm.authority.dao.UserDAO;
import com.devmaster.goatfarm.authority.mapper.UserMapper;
import com.devmaster.goatfarm.authority.model.entity.Role;
import com.devmaster.goatfarm.authority.model.entity.User;
import com.devmaster.goatfarm.authority.business.usersbusiness.UserBusiness;
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
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserBusinessTest {

    @Mock
    private UserDAO userDAO;

    @Mock
    private RoleDAO roleDAO;

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
        // ===== MOCK DO CONTEXTO HTTP (necessário para validateUserData) =====
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setRequestURI("/api/users");
        ServletRequestAttributes attributes = new ServletRequestAttributes(mockRequest);
        RequestContextHolder.setRequestAttributes(attributes);

        // ===== Resto do código que já estava aqui =====
        userRequestVO = new UserRequestVO();
        userRequestVO.setName("João Silva");
        userRequestVO.setEmail("joao@email.com");
        userRequestVO.setCpf("12345678900");
        userRequestVO.setPassword("senha123");
        userRequestVO.setConfirmPassword("senha123");
        userRequestVO.setRoles(List.of("ROLE_OPERATOR"));

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
    void tearDown() {
        // Limpa o contexto HTTP para não interferir em outros testes
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    @DisplayName("Deve criar usuário com sucesso quando não há duplicidade")
    void shouldCreateUserSuccessfully() {
        // ARRANGE
        when(userDAO.findUserByEmail("joao@email.com")).thenReturn(Optional.empty());
        when(userDAO.findUserByCpf("12345678900")).thenReturn(Optional.empty());
        when(roleDAO.findByAuthority("ROLE_OPERATOR")).thenReturn(Optional.of(operatorRole));
        when(passwordEncoder.encode("senha123")).thenReturn("$2a$10$hashedPassword");
        when(userDAO.saveUser(any(UserRequestVO.class), eq("$2a$10$hashedPassword"), anySet())).thenReturn(userResponseVO);

        // ACT
        UserResponseVO resultado = userBusiness.saveUser(userRequestVO);

        // ASSERT
        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getName()).isEqualTo("João Silva");
        assertThat(resultado.getEmail()).isEqualTo("joao@email.com");

        verify(userDAO, times(1)).findUserByEmail("joao@email.com");
        verify(userDAO, times(1)).findUserByCpf("12345678900");
        verify(passwordEncoder, times(1)).encode("senha123");
        verify(roleDAO, times(1)).findByAuthority("ROLE_OPERATOR");
        verify(userDAO, times(1)).saveUser(any(UserRequestVO.class), eq("$2a$10$hashedPassword"), anySet());
    }

    @Test
    @DisplayName("Deve lançar exceção ao salvar usuário com email duplicado")
    void shouldThrowExceptionWhenSavingUserWithDuplicateEmail() {
        // ARRANGE
        when(userDAO.findUserByEmail("joao@email.com")).thenReturn(Optional.of(new User()));

        // ACT & ASSERT
        assertThrows(DuplicateEntityException.class, () -> {
            userBusiness.saveUser(userRequestVO);
        });
    }
}