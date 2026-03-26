package com.devmaster.goatfarm.commercial.business.bo;

public record CustomerResponseVO(
        Long id,
        String name,
        String document,
        String phone,
        String email,
        String notes,
        boolean active
) {
}
