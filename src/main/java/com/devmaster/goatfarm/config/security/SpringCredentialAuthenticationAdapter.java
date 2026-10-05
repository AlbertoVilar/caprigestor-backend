package com.devmaster.goatfarm.config.security;

import com.devmaster.goatfarm.authority.application.ports.out.CredentialAuthenticationPort;
import com.devmaster.goatfarm.authority.application.ports.out.UserPrincipalQueryPort;
import com.devmaster.goatfarm.authority.business.bo.AuthenticatedPrincipal;
import com.devmaster.goatfarm.config.exceptions.custom.InvalidArgumentException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;


/**
 * Outer adapter translating Spring Security credential authentication into the
 * technology-neutral principal consumed by Authority.
 */
@Component
public class SpringCredentialAuthenticationAdapter implements CredentialAuthenticationPort {

    private final AuthenticationManager authenticationManager;
    private final UserPrincipalQueryPort principalQueryPort;

    public SpringCredentialAuthenticationAdapter(AuthenticationManager authenticationManager,
                                                UserPrincipalQueryPort principalQueryPort) {
        this.authenticationManager = authenticationManager;
        this.principalQueryPort = principalQueryPort;
    }

    @Override
    public AuthenticatedPrincipal authenticate(String username, String rawPassword) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, rawPassword));
            return principalQueryPort.findByEmail(authentication.getName())
                    .orElseThrow(() -> new InvalidArgumentException("Email ou senha inválidos"));
        } catch (AuthenticationException | InvalidArgumentException exception) {
            throw new InvalidArgumentException("Email ou senha inválidos");
        }
    }
}
