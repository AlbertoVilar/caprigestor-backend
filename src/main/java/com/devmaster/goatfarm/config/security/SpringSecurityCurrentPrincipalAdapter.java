package com.devmaster.goatfarm.config.security;

import com.devmaster.goatfarm.authority.application.ports.in.CurrentPrincipalQueryUseCase;
import com.devmaster.goatfarm.authority.application.ports.out.UserPrincipalQueryPort;
import com.devmaster.goatfarm.authority.business.bo.AuthenticatedPrincipal;
import com.devmaster.goatfarm.config.exceptions.custom.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/** Outer adapter translating Spring Security authentication into a live principal. */
@Component
public class SpringSecurityCurrentPrincipalAdapter implements CurrentPrincipalQueryUseCase {
    private final UserPrincipalQueryPort userPrincipalQueryPort;

    public SpringSecurityCurrentPrincipalAdapter(UserPrincipalQueryPort userPrincipalQueryPort) {
        this.userPrincipalQueryPort = userPrincipalQueryPort;
    }

    @Override
    public Optional<AuthenticatedPrincipal> findCurrent() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return Optional.empty();
        }
        String email = authentication.getName();
        if (email == null || email.isBlank()) {
            return Optional.empty();
        }
        return userPrincipalQueryPort.findByEmail(email);
    }

    @Override
    public AuthenticatedPrincipal requireCurrent() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new UnauthorizedException("Usuário não autenticado");
        }
        String email = authentication.getName();
        return userPrincipalQueryPort.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Usuário autenticado não encontrado: " + email));
    }
}
