package com.devmaster.goatfarm.authority.application.ports.in;

import com.devmaster.goatfarm.authority.business.bo.LoginRequestVO;
import com.devmaster.goatfarm.authority.business.bo.LoginResponseVO;
import com.devmaster.goatfarm.authority.business.bo.RefreshTokenRequestVO;

/**
 * Porta de entrada (Use Case) para operações de autenticação.
 */
public interface AuthManagementUseCase {

    LoginResponseVO login(LoginRequestVO loginRequest);

    LoginResponseVO refreshToken(RefreshTokenRequestVO refreshRequest);
}
