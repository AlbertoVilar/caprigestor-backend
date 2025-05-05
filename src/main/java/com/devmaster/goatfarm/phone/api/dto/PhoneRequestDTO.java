package com.devmaster.goatfarm.phone.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class PhoneRequestDTO {

    private Long id;

    @NotBlank(message = "O DDD não pode estar em branco.")
    @Size(min = 2, max = 2, message = "O DDD deve ter 2 dígitos.")
    @Pattern(regexp = "^\\d{2}$", message = "O DDD deve conter apenas números.")
    private String ddd;

    @NotBlank(message = "O número de telefone não pode estar em branco.")
    @Size(min = 8, max = 9, message = "O número de telefone deve ter 8 ou 9 dígitos.")
    @Pattern(regexp = "^\\d{8,9}$", message = "O número de telefone deve conter apenas números.")
    private String number;

    private Long goatFarmId; // Somente o ID do GoatFarm

    public PhoneRequestDTO() {
    }

    public PhoneRequestDTO(Long id, String ddd, String number, Long goatFarmId) {
        this.id = id;
        this.ddd = ddd;
        this.number = number;
        this.goatFarmId = goatFarmId;
    }

    public Long getId() {
        return id;
    }

    public String getDdd() {
        return ddd;
    }

    public String getNumber() {
        return number;
    }

    public Long getGoatFarmId() {
        return goatFarmId;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setDdd(String ddd) {
        this.ddd = ddd;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public void setGoatFarmId(Long goatFarmId) {
        this.goatFarmId = goatFarmId;
    }
}
