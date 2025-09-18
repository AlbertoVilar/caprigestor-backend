package com.devmaster.goatfarm.authority.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponseDTO {

    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn; // em segundos
    private UserResponseDTO user;
}