package com.devmaster.goatfarm.authority.business.bo;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRequestVO {

    private String name;
    private String email;
    private String cpf;
    private String password;
    private String confirmPassword;
    List<String> roles = new ArrayList<>();


    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getCpf() {
        return cpf;
    }

    public String getPassword() {
        return password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public List<String> getRoles() {
        return roles;
    }
}
