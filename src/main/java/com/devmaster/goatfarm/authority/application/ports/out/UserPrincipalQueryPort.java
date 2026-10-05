package com.devmaster.goatfarm.authority.application.ports.out;

import com.devmaster.goatfarm.authority.business.bo.AuthenticatedPrincipal;

import java.util.Optional;

/** Minimal persistence query for the currently authenticated application identity. */
public interface UserPrincipalQueryPort {
    Optional<AuthenticatedPrincipal> findByEmail(String email);
}
