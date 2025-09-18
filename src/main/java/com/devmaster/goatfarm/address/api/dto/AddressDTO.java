package com.devmaster.goatfarm.address.api.dto;

public class AddressDTO {
    // Estrutura mínima para compilar
    private Long id;
    private String street;
    private String city;
    private String state;
    private String zipCode;
    // getters e setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public String getZipCode() { return zipCode; }
    public void setZipCode(String zipCode) { this.zipCode = zipCode; }
}
