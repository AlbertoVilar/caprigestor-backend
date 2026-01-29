package com.devmaster.goatfarm.authority.api;

import com.devmaster.goatfarm.authority.api.controller.UserController;
import com.devmaster.goatfarm.authority.application.ports.in.UserManagementUseCase;
import com.devmaster.goatfarm.authority.api.mapper.UserMapper;
import com.devmaster.goatfarm.authority.api.dto.UserResponseDTO;
import com.devmaster.goatfarm.authority.business.bo.UserResponseVO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserManagementUseCase userUseCase;

    @MockBean
    private UserMapper userMapper;

    // Em testes de camada web, o metamodel JPA pode ser mockado para evitar erros de contexto
    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    @WithMockUser(roles = "OPERATOR")
    public void whenGetUserById_thenReturns200() throws Exception {
        // Arrange: stub do use case e mapper
        UserResponseVO vo = new UserResponseVO(
                1L,
                "Test User",
                "test@example.com",
                "12345678901",
                java.util.List.of("ROLE_OPERATOR")
        );

        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(1L);
        dto.setName("Test User");
        dto.setEmail("test@example.com");
        dto.setCpf("12345678901");
        dto.setRoles(java.util.List.of("ROLE_OPERATOR"));

        when(userUseCase.findById(1L)).thenReturn(vo);
        when(userMapper.toResponseDTO(any(UserResponseVO.class))).thenReturn(dto);

        // Act & Assert
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk());
    }
}

