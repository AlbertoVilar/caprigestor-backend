package com.devmaster.goatfarm.authority.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class RefreshTokenRequestDTO {

    @NotBlank(message = "Refresh token é obrigatório")
    private String refreshToken;
}