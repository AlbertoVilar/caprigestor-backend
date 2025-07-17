package com.devmaster.goatfarm.farm.business.bo;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GoatFarmRequestVO {
    private Long id;
    private String name;
    private String tod;
    private Long addressId;
    private Long ownerId;
    private List<Long> phoneIds; // ✅ Campo necessário

    public GoatFarmRequestVO() {}

    public GoatFarmRequestVO(Long id, String name, String tod, Long addressId, Long ownerId, List<Long> phoneIds) {
        this.id = id;
        this.name = name;
        this.tod = tod;
        this.addressId = addressId;
        this.ownerId = ownerId;
        this.phoneIds = phoneIds;
    }
}
