package com.devmaster.goatfarm.authority.application.ports.in;

import com.devmaster.goatfarm.authority.business.bo.PasswordResetConfirmVO;
import com.devmaster.goatfarm.authority.business.bo.PasswordResetRequestVO;
import com.devmaster.goatfarm.authority.business.bo.PasswordResetResponseVO;

public interface PasswordResetManagementUseCase {

    PasswordResetResponseVO requestPasswordReset(PasswordResetRequestVO requestVO);

    PasswordResetResponseVO confirmPasswordReset(PasswordResetConfirmVO confirmVO);
}
