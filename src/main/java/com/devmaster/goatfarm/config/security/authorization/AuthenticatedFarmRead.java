package com.devmaster.goatfarm.config.security.authorization;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marca uma leitura farm-scoped que permanece apenas autenticada por contrato;
 * ela não representa autorização de ownership ou de operação.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AuthenticatedFarmRead {
}
