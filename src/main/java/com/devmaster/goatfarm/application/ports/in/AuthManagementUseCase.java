package com.devmaster.goatfarm.application.ports.in;

import com.devmaster.goatfarm.authority.api.dto.LoginRequestDTO;
import com.devmaster.goatfarm.authority.api.dto.LoginResponseDTO;
import com.devmaster.goatfarm.authority.api.dto.RefreshTokenRequestDTO;

/**
 * Porta de entrada (Use Case) para operações de autenticação.
 */
public interface AuthManagementUseCase {

    LoginResponseDTO login(LoginRequestDTO loginRequest);

    LoginResponseDTO refreshToken(RefreshTokenRequestDTO refreshRequest);
}