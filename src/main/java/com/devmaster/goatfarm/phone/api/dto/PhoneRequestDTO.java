package com.devmaster.goatfarm.phone.api.dto;

public class PhoneRequestDTO {

    private Long id;
    private String ddd;
    private String number;

    public PhoneRequestDTO() {
    }

    public PhoneRequestDTO(Long id, String ddd, String number) {
        this.id = id;
        this.ddd = ddd;
        this.number = number;
    }

    public Long getId() {
        return id;
    }

    public String getDdd() {
        return ddd;
    }

    public String getNumber() {
        return number;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setDdd(String ddd) {
        this.ddd = ddd;
    }

    public void setNumber(String number) {
        this.number = number;
    }
}
