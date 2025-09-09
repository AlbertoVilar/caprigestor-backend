package com.devmaster.goatfarm.farm.business.bo;

import com.devmaster.goatfarm.phone.business.bo.PhoneResponseVO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoatFarmFullResponseVO {

    private Long id;
    private String name;
    private String tod;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Usuário
    private Long userId;
    private String userName;
    private String userEmail;
    private String userCpf;

    // Endereço
    private Long addressId;
    private String street;
    private String district;
    private String city;
    private String state;
    private String postalCode;

    // Telefones
    private List<PhoneResponseVO> phones;

    // Getters e Setters manuais para garantir compilação
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getTod() { return tod; }
    public void setTod(String tod) { this.tod = tod; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    
    public String getUserCpf() { return userCpf; }
    public void setUserCpf(String userCpf) { this.userCpf = userCpf; }
    
    public Long getAddressId() { return addressId; }
    public void setAddressId(Long addressId) { this.addressId = addressId; }
    
    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }
    
    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }
    
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    
    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
    
    public List<PhoneResponseVO> getPhones() { return phones; }
    public void setPhones(List<PhoneResponseVO> phones) { this.phones = phones; }
}
