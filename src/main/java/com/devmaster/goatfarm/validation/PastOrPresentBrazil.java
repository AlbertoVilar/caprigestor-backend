package com.devmaster.goatfarm.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Constraint(validatedBy = PastOrPresentBrazilValidator.class)
@Target({ FIELD })
@Retention(RUNTIME)
public @interface PastOrPresentBrazil {

    String message() default "A data de realização não pode ser futura";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
