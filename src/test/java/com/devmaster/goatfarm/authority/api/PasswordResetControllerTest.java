package com.devmaster.goatfarm.authority.api;

import com.devmaster.goatfarm.authority.api.controller.PasswordResetController;
import com.devmaster.goatfarm.authority.api.dto.PasswordResetResponseDTO;
import com.devmaster.goatfarm.authority.application.ports.in.PasswordResetManagementUseCase;
import com.devmaster.goatfarm.authority.business.AdminMaintenanceBusiness;
import com.devmaster.goatfarm.authority.business.bo.PasswordResetResponseVO;
import com.devmaster.goatfarm.config.exceptions.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.autoconfigure.mail.MailSenderValidatorAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = true)
@Import(GlobalExceptionHandler.class)
@ImportAutoConfiguration(classes = {MailSenderAutoConfiguration.class, MailSenderValidatorAutoConfiguration.class})
@ActiveProfiles("test")
class PasswordResetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordResetController passwordResetController;

    @MockBean
    private PasswordResetManagementUseCase passwordResetManagementUseCase;

    @MockBean
    private AdminMaintenanceBusiness adminMaintenanceBusiness;

    @Test
    void contextLoads() {
        org.junit.jupiter.api.Assertions.assertNotNull(passwordResetController);
    }

    @Test
    void shouldAllowPublicPasswordResetRequestWithoutAuthentication() throws Exception {
        when(passwordResetManagementUseCase.requestPasswordReset(any()))
                .thenReturn(PasswordResetResponseVO.builder()
                        .message("Se existir uma conta com esse email, enviaremos um link de redefinicao.")
                        .build());

        mockMvc.perform(post("/api/v1/auth/password-reset/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new java.util.LinkedHashMap<>() {{
                            put("email", "qa.reset@example.com");
                        }})))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Se existir uma conta com esse email, enviaremos um link de redefinicao."));

        verify(passwordResetManagementUseCase).requestPasswordReset(any());
    }

    @Test
    void shouldAllowPublicPasswordResetConfirmWithoutAuthentication() throws Exception {
        when(passwordResetManagementUseCase.confirmPasswordReset(any()))
                .thenReturn(PasswordResetResponseVO.builder()
                        .message("Senha redefinida com sucesso.")
                        .build());

        mockMvc.perform(post("/api/v1/auth/password-reset/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new java.util.LinkedHashMap<>() {{
                            put("token", "raw-token");
                            put("newPassword", "NovaSenha123");
                            put("confirmPassword", "NovaSenha123");
                        }})))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Senha redefinida com sucesso."));

        verify(passwordResetManagementUseCase).confirmPasswordReset(any());
    }
}
