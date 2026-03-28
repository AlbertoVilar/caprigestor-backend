package com.devmaster.goatfarm.authority.business.bo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordResetConfirmVO {

    private String token;
    private String newPassword;
    private String confirmPassword;
}
