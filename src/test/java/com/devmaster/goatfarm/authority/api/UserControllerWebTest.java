package com.devmaster.goatfarm.authority.api;

import com.devmaster.goatfarm.authority.api.controller.UserController;
import com.devmaster.goatfarm.authority.facade.UserFacade;
import com.devmaster.goatfarm.authority.mapper.UserMapper;
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
public class UserControllerWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserFacade userFacade;

    @MockBean
    private UserMapper userMapper;

    // Em testes de camada web, o metamodel JPA pode ser mockado para evitar erros de contexto
    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    @WithMockUser(roles = "OPERATOR")
    public void whenGetUserById_thenReturns200() throws Exception {
        // Arrange: stub do use case e mapper
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(1L);
        dto.setName("Test User");
        dto.setEmail("test@example.com");
        dto.setCpf("12345678901");
        dto.setRoles(java.util.List.of("ROLE_OPERATOR"));

        when(userFacade.findById(1L)).thenReturn(dto);

        // Act & Assert
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk());
    }
}




