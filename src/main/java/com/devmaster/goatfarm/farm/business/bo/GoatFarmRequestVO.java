package com.devmaster.goatfarm.farm.business.bo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GoatFarmRequestVO {
    private Long id;
    private String name;
    private String tod;
    private Long addressId;
    private Long ownerId; // Adicione este campo

    public GoatFarmRequestVO() {
    }

    public GoatFarmRequestVO(Long id, String name, String tod, Long addressId, Long ownerId) {
        this.id = id;
        this.name = name;
        this.tod = tod;
        this.addressId = addressId;
        this.ownerId = ownerId;
    }
}
