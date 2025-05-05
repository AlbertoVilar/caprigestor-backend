package com.devmaster.goatfarm.farm.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
//@JsonIgnoreProperties(ignoreUnknown = true)
public class GoatFarmRequestDTO {
    private Long id;
    private String name;
    private String tod;
    private Long addressId;
    private Long ownerId; // Adicione este campo
    public GoatFarmRequestDTO() {
    }

    public GoatFarmRequestDTO(Long id, String name, String tod, Long addressId, Long ownerId) {
        this.id = id;
        this.name = name;
        this.tod = tod;
        this.addressId = addressId;
        this.ownerId = ownerId;
    }
}
