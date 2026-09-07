package com.devmaster.goatfarm.authority.api;

import com.devmaster.goatfarm.authority.api.controller.UserController;
import com.devmaster.goatfarm.authority.api.dto.UserRequestDTO;
import com.devmaster.goatfarm.authority.api.dto.UserResponseDTO;
import com.devmaster.goatfarm.authority.api.dto.UserUpdateRequestDTO;
import com.devmaster.goatfarm.authority.api.mapper.UserMapper;
import com.devmaster.goatfarm.authority.application.ports.in.UserManagementUseCase;
import com.devmaster.goatfarm.authority.business.bo.UserRequestVO;
import com.devmaster.goatfarm.authority.business.bo.UserResponseVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest(UserController.class)
@Import(UserControllerTest.MethodSecurityTestConfiguration.class)
class UserControllerTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class MethodSecurityTestConfiguration {
    }

    private static final String CREATE_PAYLOAD = """
            {
              "name": "Usuário Teste",
              "email": "user@example.com",
              "cpf": "12345678901",
              "password": "senha123",
              "confirmPassword": "senha123",
              "roles": ["ROLE_OPERATOR"]
            }
            """;

    private static final String CREATE_ADMIN_PAYLOAD = """
            {
              "name": "Usuário Teste",
              "email": "user@example.com",
              "cpf": "12345678901",
              "password": "senha123",
              "confirmPassword": "senha123",
              "roles": ["ROLE_ADMIN"]
            }
            """;

    private static final String UPDATE_PAYLOAD = """
            {
              "name": "Usuário Atualizado",
              "email": "updated@example.com",
              "cpf": "12345678901"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserManagementUseCase userUseCase;

    @MockBean
    private UserMapper userMapper;

    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    private UserResponseVO responseVO;
    private UserResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        responseVO = new UserResponseVO(
                1L,
                "Usuário Teste",
                "user@example.com",
                "12345678901",
                List.of("ROLE_OPERATOR")
        );

        responseDTO = new UserResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setName("Usuário Teste");
        responseDTO.setEmail("user@example.com");
        responseDTO.setCpf("12345678901");
        responseDTO.setRoles(List.of("ROLE_OPERATOR"));

        when(userMapper.toRequestVO(any(UserRequestDTO.class))).thenReturn(new UserRequestVO());
        when(userMapper.toRequestVO(any(UserUpdateRequestDTO.class))).thenReturn(new UserRequestVO());
        when(userMapper.toResponseDTO(any(UserResponseVO.class))).thenReturn(responseDTO);
        when(userUseCase.saveUser(any(UserRequestVO.class))).thenReturn(responseVO);
        when(userUseCase.updateUser(eq(1L), any(UserRequestVO.class))).thenReturn(responseVO);
        when(userUseCase.updateRoles(eq(1L), any())).thenReturn(responseVO);
        when(userUseCase.findById(1L)).thenReturn(responseVO);
        when(userUseCase.findByEmail("user@example.com")).thenReturn(responseVO);
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void adminCanCreateUser() throws Exception {
        mockMvc.perform(post("/api/v1/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CREATE_PAYLOAD))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(authorities = "ROLE_OPERATOR")
    void operatorCannotCreateUser() throws Exception {
        mockMvc.perform(post("/api/v1/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CREATE_PAYLOAD))
                .andExpect(status().isForbidden());

        verify(userUseCase, never()).saveUser(any());
    }

    @Test
    @WithMockUser(authorities = "ROLE_OPERATOR")
    void operatorCannotCreateAdminUser() throws Exception {
        mockMvc.perform(post("/api/v1/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CREATE_ADMIN_PAYLOAD))
                .andExpect(status().isForbidden());

        verify(userUseCase, never()).saveUser(any());
    }

    @Test
    @WithMockUser(authorities = "ROLE_FARM_OWNER")
    void farmOwnerCannotCreateUser() throws Exception {
        mockMvc.perform(post("/api/v1/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CREATE_PAYLOAD))
                .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedUserCannotCreateUser() throws Exception {
        mockMvc.perform(post("/api/v1/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CREATE_PAYLOAD))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void adminCanUpdateUser() throws Exception {
        mockMvc.perform(put("/api/v1/users/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(UPDATE_PAYLOAD))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "ROLE_OPERATOR")
    void operatorCannotUpdateAnotherUser() throws Exception {
        mockMvc.perform(put("/api/v1/users/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(UPDATE_PAYLOAD))
                .andExpect(status().isForbidden());

        verify(userUseCase, never()).updateUser(anyLong(), any());
    }

    @Test
    @WithMockUser(authorities = "ROLE_FARM_OWNER")
    void farmOwnerCannotUpdateUser() throws Exception {
        mockMvc.perform(put("/api/v1/users/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(UPDATE_PAYLOAD))
                .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedUserCannotUpdateUser() throws Exception {
        mockMvc.perform(put("/api/v1/users/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(UPDATE_PAYLOAD))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void adminCanUpdateRolesAndPassword() throws Exception {
        mockMvc.perform(patch("/api/v1/users/1/roles")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roles\":[\"ROLE_OPERATOR\"]}"))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/api/v1/users/1/password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"novaSenha123\",\"confirmPassword\":\"novaSenha123\"}"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(authorities = "ROLE_OPERATOR")
    void operatorCannotUpdateRolesOrAnotherUsersPassword() throws Exception {
        mockMvc.perform(patch("/api/v1/users/1/roles")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roles\":[\"ROLE_ADMIN\"]}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(patch("/api/v1/users/1/password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"novaSenha123\",\"confirmPassword\":\"novaSenha123\"}"))
                .andExpect(status().isForbidden());

        verify(userUseCase, never()).updateRoles(anyLong(), any());
        verify(userUseCase, never()).updatePassword(anyLong(), any());
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void adminCanQueryAdministrativeUserEndpoints() throws Exception {
        mockMvc.perform(get("/api/v1/users/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "ROLE_OPERATOR")
    void operatorCannotQueryAdministrativeUserEndpoints() throws Exception {
        mockMvc.perform(get("/api/v1/users/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void legacyDebugEndpointIsNotExposed() throws Exception {
        mockMvc.perform(get("/api/v1/users/debug/user@example.com"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "ROLE_FARM_OWNER")
    void farmOwnerCannotQueryAdministrativeUserEndpoints() throws Exception {
        mockMvc.perform(get("/api/v1/users/1"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isForbidden());
    }
}
