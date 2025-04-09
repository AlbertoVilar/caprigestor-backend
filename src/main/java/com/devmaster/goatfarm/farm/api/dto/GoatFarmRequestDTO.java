package com.devmaster.goatfarm.farm.api.dto;

public class GoatFarmRequestDTO {

    private Long id;
    private String name;
    private String tod;

    public GoatFarmRequestDTO(Long id, String name, String tod) {

        this.id = id;
        this.name = name;
        this.tod = tod;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTod() {
        return tod;
    }

    public void setTod(String tod) {
        this.tod = tod;
    }
}
