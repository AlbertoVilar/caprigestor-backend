package com.devmaster.goatfarm.authority.api.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class UserRolesUpdateDTO {

    @NotEmpty(message = "Lista de roles não pode ser vazia")
    private List<String> roles;

    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }
}