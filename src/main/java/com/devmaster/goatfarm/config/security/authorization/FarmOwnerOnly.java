package com.devmaster.goatfarm.config.security.authorization;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Permite uma operação patrimonial/administrativa somente a ADMIN ou ao
 * FARM_OWNER da fazenda identificada pelo parâmetro {@code farmId}.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@PreAuthorize("hasAuthority('ROLE_ADMIN') or (hasAuthority('ROLE_FARM_OWNER') and @ownershipService.isFarmOwner(#farmId))")
public @interface FarmOwnerOnly {
}
