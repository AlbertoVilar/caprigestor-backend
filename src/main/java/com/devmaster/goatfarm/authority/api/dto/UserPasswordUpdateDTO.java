package com.devmaster.goatfarm.authority.api.dto;

import jakarta.validation.constraints.NotBlank;

public class UserPasswordUpdateDTO {

    @NotBlank(message = "Senha é obrigatória")
    private String password;

    @NotBlank(message = "Confirmação de senha é obrigatória")
    private String confirmPassword;

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
}