package com.devmaster.goatfarm.authority.api.controller;

import com.devmaster.goatfarm.authority.api.dto.PasswordResetConfirmDTO;
import com.devmaster.goatfarm.authority.api.dto.PasswordResetRequestDTO;
import com.devmaster.goatfarm.authority.api.dto.PasswordResetResponseDTO;
import com.devmaster.goatfarm.authority.application.ports.in.PasswordResetManagementUseCase;
import com.devmaster.goatfarm.authority.business.bo.PasswordResetConfirmVO;
import com.devmaster.goatfarm.authority.business.bo.PasswordResetRequestVO;
import com.devmaster.goatfarm.authority.business.bo.PasswordResetResponseVO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/v1/auth/password-reset", "/api/auth/password-reset"})
public class PasswordResetController {

    private final PasswordResetManagementUseCase passwordResetManagementUseCase;

    public PasswordResetController(PasswordResetManagementUseCase passwordResetManagementUseCase) {
        this.passwordResetManagementUseCase = passwordResetManagementUseCase;
    }

    @PostMapping("/request")
    public ResponseEntity<PasswordResetResponseDTO> requestPasswordReset(@Valid @RequestBody PasswordResetRequestDTO requestDTO) {
        PasswordResetResponseVO responseVO = passwordResetManagementUseCase.requestPasswordReset(
                PasswordResetRequestVO.builder()
                        .email(requestDTO.getEmail())
                        .build()
        );

        return ResponseEntity.ok(PasswordResetResponseDTO.builder()
                .message(responseVO.getMessage())
                .build());
    }

    @PostMapping("/confirm")
    public ResponseEntity<PasswordResetResponseDTO> confirmPasswordReset(@Valid @RequestBody PasswordResetConfirmDTO confirmDTO) {
        PasswordResetResponseVO responseVO = passwordResetManagementUseCase.confirmPasswordReset(
                PasswordResetConfirmVO.builder()
                        .token(confirmDTO.getToken())
                        .newPassword(confirmDTO.getNewPassword())
                        .confirmPassword(confirmDTO.getConfirmPassword())
                        .build()
        );

        return ResponseEntity.ok(PasswordResetResponseDTO.builder()
                .message(responseVO.getMessage())
                .build());
    }
}
