package com.devmaster.goatfarm.authority.application.ports.in;

import com.devmaster.goatfarm.authority.business.bo.AuthenticatedPrincipal;

import java.util.Optional;

/** Application boundary for resolving the caller using live persisted roles. */
public interface CurrentPrincipalQueryUseCase {
    Optional<AuthenticatedPrincipal> findCurrent();

    AuthenticatedPrincipal requireCurrent();
}
