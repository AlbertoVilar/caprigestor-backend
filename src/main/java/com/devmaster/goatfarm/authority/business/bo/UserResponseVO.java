package com.devmaster.goatfarm.authority.business.bo;

import java.util.ArrayList;
import java.util.List;

public class UserResponseVO {

    private Long id;
    private String name;
    private String email;
    private String cpf;

    List<String> roles = new ArrayList<>();

    public UserResponseVO(Long id, String name, String email, String cpf, List<String> roles) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.cpf = cpf;
        this.roles = roles;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getCpf() {
        return cpf;
    }

    public List<String> getRoles() {
        return roles;
    }
}
