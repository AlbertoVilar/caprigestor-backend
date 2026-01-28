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
    private String logoUrl;
    private Long addressId;
    private Long userId;
    private List<Long> phoneIds;
    private Integer version;

    public GoatFarmRequestVO() {}

    public GoatFarmRequestVO(Long id, String name, String tod, Long addressId, Long userId, List<Long> phoneIds) {
        this.id = id;
        this.name = name;
        this.tod = tod;
        this.addressId = addressId;
        this.userId = userId;
        this.phoneIds = phoneIds;
    }
}

