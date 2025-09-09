package com.devmaster.goatfarm.authority.business.bo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRequestVO {

    private String name;
    private String email;
    private String cpf;
    private String password;
    private String confirmPassword;
    private List<String> roles;
}
