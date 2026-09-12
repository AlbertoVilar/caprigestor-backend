package com.devmaster.goatfarm.config.security;

import com.devmaster.goatfarm.authority.application.ports.out.CredentialAuthenticationPort;
import com.devmaster.goatfarm.authority.business.bo.AuthenticatedPrincipal;
import com.devmaster.goatfarm.authority.persistence.entity.User;
import com.devmaster.goatfarm.config.exceptions.custom.InvalidArgumentException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * Outer adapter translating Spring Security credential authentication into the
 * technology-neutral principal consumed by Authority.
 */
@Component
public class SpringCredentialAuthenticationAdapter implements CredentialAuthenticationPort {

    private final AuthenticationManager authenticationManager;

    public SpringCredentialAuthenticationAdapter(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Override
    public AuthenticatedPrincipal authenticate(String username, String rawPassword) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, rawPassword));
            User user = (User) authentication.getPrincipal();
            return new AuthenticatedPrincipal(
                    user.getId(),
                    user.getEmail(),
                    user.getName(),
                    user.getRoles().stream().map(role -> role.getAuthority()).collect(Collectors.toSet())
            );
        } catch (AuthenticationException | ClassCastException exception) {
            throw new InvalidArgumentException("Email ou senha inválidos");
        }
    }
}
