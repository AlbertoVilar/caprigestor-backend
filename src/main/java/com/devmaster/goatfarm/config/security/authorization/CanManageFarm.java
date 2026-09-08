package com.devmaster.goatfarm.config.security.authorization;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Permite uma operação operacional na fazenda identificada pelo parâmetro
 * {@code farmId}: ADMIN, FARM_OWNER da própria fazenda ou OPERATOR vinculado.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@PreAuthorize("@ownershipService.canManageFarm(#farmId)")
public @interface CanManageFarm {
}
