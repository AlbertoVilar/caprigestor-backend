package com.devmaster.goatfarm.authority.facade.dto;

import java.util.List;

/**
 * DTO de resposta do UserFacade para encapsular dados do usuário
 * sem expor detalhes internos dos VOs.
 */
public class UserFacadeResponseDTO {

    private Long id;
    private String name;
    private String email;
    private String cpf;
    private List<String> roles;

    public UserFacadeResponseDTO() {
    }

    public UserFacadeResponseDTO(Long id, String name, String email, String cpf, List<String> roles) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.cpf = cpf;
        this.roles = roles;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
}