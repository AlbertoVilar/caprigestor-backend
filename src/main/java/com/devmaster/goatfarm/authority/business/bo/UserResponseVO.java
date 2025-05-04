package com.devmaster.goatfarm.authority.business.bo;

import java.util.ArrayList;
import java.util.List;

public class UserResponseVO {

    private Long id;
    private String name;
    private String email;


    List<String> roles = new ArrayList<>();

    public UserResponseVO(Long id, String name, String email, List<String> roles) {
        this.id = id;
        this.name = name;
        this.email = email;
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

    public List<String> getRoles() {
        return roles;
    }
}
