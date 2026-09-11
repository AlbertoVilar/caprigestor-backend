package com.devmaster.goatfarm.goat.business.bo;

import com.devmaster.goatfarm.goat.enums.RegistrationRectificationSource;

public record GoatRegistrationRectificationRequestVO(
        String tod,
        String toe,
        RegistrationRectificationSource source,
        String evidenceReference,
        String reason
) {
}
