package com.devmaster.goatfarm.farm.business.bo;

import java.time.LocalDateTime;


public class GoatFarmResponseVO {

    private Long id;
    private String name;
    private String tod;
    private String logoUrl;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public GoatFarmResponseVO() {
    }

    public GoatFarmResponseVO(Long id,
                              String name,
                              String tod,
                              String logoUrl,
                              LocalDateTime createdAt,
                              LocalDateTime updatedAt) {

        this.id = id;
        this.name = name;
        this.tod = tod;
        this.logoUrl = logoUrl;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
