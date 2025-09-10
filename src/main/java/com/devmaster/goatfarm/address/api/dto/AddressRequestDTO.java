package com.devmaster.goatfarm.address.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class AddressRequestDTO {

    private Long id;

    @NotBlank(message = "A rua não pode estar em branco.")
    @Size(max = 255, message = "A rua não pode ter mais de 255 caracteres.")
    private String street;

    @NotBlank(message = "O bairro não pode estar em branco.")
    @Size(max = 100, message = "O bairro não pode ter mais de 100 caracteres.")
    private String neighborhood;

    @NotBlank(message = "A cidade não pode estar em branco.")
    @Size(max = 100, message = "A cidade não pode ter mais de 100 caracteres.")
    private String city;

    @NotBlank(message = "O estado não pode estar em branco.")
    @Size(max = 50, message = "O estado não pode ter mais de 50 caracteres.")
    private String state;

    @NotBlank(message = "O código postal não pode estar em branco.")
    @Pattern(regexp = "^\\d{5}-?\\d{3}$", message = "O código postal deve estar no formato XXXXX-XXX ou XXXXX.")
    @JsonProperty("zipCode")
    private String postalCode;

    @NotBlank(message = "O país não pode estar em branco.")
    @Size(max = 100, message = "O país não pode ter mais de 100 caracteres.")
    private String country;

    public AddressRequestDTO() {
    }

    public AddressRequestDTO(Long id,
                             String street,
                             String neighborhood,
                             String city,
                             String state,
                             String postalCode,
                             String country) {

        this.id = id;
        this.street = street;
        this.neighborhood = neighborhood;
        this.city = city;
        this.state = state;
        this.postalCode = postalCode;
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

    public String getNeighborhood() {
        return neighborhood;
    }

    public void setNeighborhood(String neighborhood) {
        this.neighborhood = neighborhood;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}