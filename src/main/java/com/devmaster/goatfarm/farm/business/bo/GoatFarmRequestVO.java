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
    private Long userId;
    private List<Long> phoneIds; // ✅ Campo necessário

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
