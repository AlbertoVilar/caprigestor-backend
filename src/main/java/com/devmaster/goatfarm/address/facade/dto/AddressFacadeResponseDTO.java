package com.devmaster.goatfarm.address.facade.dto;

/**
 * DTO de resposta do AddressFacade para encapsular dados do endereço
 * sem expor detalhes internos dos VOs.
 */
public class AddressFacadeResponseDTO {

    private Long id;
    private String street;
    private String city;
    private String neighborhood;
    private String state;
    private String zipCode;
    private String country;

    public AddressFacadeResponseDTO() {
    }

    public AddressFacadeResponseDTO(Long id, String street, String city, String neighborhood, 
                                   String state, String zipCode, String country) {
        this.id = id;
        this.street = street;
        this.city = city;
        this.neighborhood = neighborhood;
        this.state = state;
        this.zipCode = zipCode;
        this.country = country;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getNeighborhood() {
        return neighborhood;
    }

    public void setNeighborhood(String neighborhood) {
        this.neighborhood = neighborhood;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}