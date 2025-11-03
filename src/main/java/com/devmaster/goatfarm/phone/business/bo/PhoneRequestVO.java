package com.devmaster.goatfarm.phone.business.bo;

public class PhoneRequestVO {

    private Long id;
    private String ddd;
    private String number;
    private Long goatFarmId; 
    public PhoneRequestVO() {
    }

    public PhoneRequestVO(Long id, String ddd, String number, Long goatFarmId) {
        this.id = id;
        this.ddd = ddd;
        this.number = number;
        this.goatFarmId = goatFarmId;
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

    public Long getGoatFarmId() {
        return goatFarmId;
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

    public void setGoatFarmId(Long goatFarmId) {
        this.goatFarmId = goatFarmId;
    }
}

