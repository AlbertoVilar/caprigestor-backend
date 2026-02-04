package com.devmaster.goatfarm.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDateTime;
import java.time.ZoneId;

public class PastOrPresentBrazilValidator implements ConstraintValidator<PastOrPresentBrazil, LocalDateTime> {

    private static final ZoneId BRAZIL_ZONE = ZoneId.of("America/Sao_Paulo");

    @Override
    public boolean isValid(LocalDateTime value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        LocalDateTime nowInBrazil = LocalDateTime.now(BRAZIL_ZONE);
        return !value.isAfter(nowInBrazil);
    }
}
