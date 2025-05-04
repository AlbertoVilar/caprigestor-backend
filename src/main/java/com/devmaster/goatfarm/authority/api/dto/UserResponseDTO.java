package com.devmaster.goatfarm.authority.api.dto;

import java.util.ArrayList;
import java.util.List;

public class UserResponseDTO {

    private Long id;
    private String name;
    private String email;


    List<String> roles = new ArrayList<>();

    public UserResponseDTO(Long id, String name, String email, List<String> roles) {
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
