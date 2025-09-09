package com.devmaster.goatfarm.farm.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GoatFarmRequestDTO {
    private Long id;

    @NotNull(message = "O nome da fazenda é obrigatório.")
    private String name;

    @Size(min = 5, max = 5, message = "O TOD da fazenda deve conter exatamente 5 caracteres.")
    private String tod;

    @NotNull(message = "O endereço da fazenda é obrigatório.")
    private Long addressId;

    @NotNull(message = "O usuário da fazenda é obrigatório.")
    private Long userId;

    @NotNull(message = "É obrigatório informar ao menos um telefone.")
    @Size(min = 1, message = "A fazenda deve conter ao menos um telefone.")
    private List<Long> phoneIds;

    // Getters e Setters manuais para garantir compilação
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getTod() { return tod; }
    public void setTod(String tod) { this.tod = tod; }
    
    public Long getAddressId() { return addressId; }
    public void setAddressId(Long addressId) { this.addressId = addressId; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public List<Long> getPhoneIds() { return phoneIds; }
    public void setPhoneIds(List<Long> phoneIds) { this.phoneIds = phoneIds; }

    public GoatFarmRequestDTO() {}

    public GoatFarmRequestDTO(Long id, String name, String tod, Long addressId, Long userId, List<Long> phoneIds) {
        this.id = id;
        this.name = name;
        this.tod = tod;
        this.addressId = addressId;
        this.userId = userId;
        this.phoneIds = phoneIds;
    }
}
