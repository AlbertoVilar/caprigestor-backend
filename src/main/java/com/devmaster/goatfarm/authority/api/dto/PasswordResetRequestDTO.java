package com.devmaster.goatfarm.authority.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordResetRequestDTO {

    @NotBlank(message = "Email e obrigatorio")
    @Email(message = "Email deve ter formato valido")
    private String email;
}
