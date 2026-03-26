package com.devmaster.goatfarm.commercial.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CustomerRequestDTO(
        @NotBlank(message = "Nome do cliente e obrigatorio")
        String name,
        String document,
        String phone,
        @Email(message = "Email do cliente invalido")
        String email,
        String notes
) {
}
