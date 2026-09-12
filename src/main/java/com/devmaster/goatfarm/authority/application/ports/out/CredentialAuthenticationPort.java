package com.devmaster.goatfarm.authority.application.ports.out;

import com.devmaster.goatfarm.authority.business.bo.AuthenticatedPrincipal;

/** Application boundary for credential authentication. */
public interface CredentialAuthenticationPort {

    AuthenticatedPrincipal authenticate(String username, String rawPassword);
}
