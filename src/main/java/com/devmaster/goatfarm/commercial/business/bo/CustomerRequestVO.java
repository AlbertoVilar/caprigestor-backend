package com.devmaster.goatfarm.commercial.business.bo;

public record CustomerRequestVO(
        String name,
        String document,
        String phone,
        String email,
        String notes
) {
}
