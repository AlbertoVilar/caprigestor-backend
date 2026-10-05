package com.devmaster.goatfarm.security.unit;

import com.devmaster.goatfarm.authority.application.ports.out.UserPrincipalQueryPort;
import com.devmaster.goatfarm.authority.business.bo.AuthenticatedPrincipal;
import com.devmaster.goatfarm.config.exceptions.custom.UnauthorizedException;
import com.devmaster.goatfarm.config.security.SpringSecurityCurrentPrincipalAdapter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SpringSecurityCurrentPrincipalAdapterTest {
    @Mock UserPrincipalQueryPort principalQuery;

    @AfterEach
    void clearContext() { SecurityContextHolder.clearContext(); }

    @Test
    void resolvesLivePersistedPrincipalAndRoles() {
        AuthenticatedPrincipal persisted = new AuthenticatedPrincipal(7L, "live@example.com", "Live", Set.of("ROLE_OPERATOR"));
        when(principalQuery.findByEmail("live@example.com")).thenReturn(Optional.of(persisted));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("live@example.com", "token", Set.of()));

        AuthenticatedPrincipal result = new SpringSecurityCurrentPrincipalAdapter(principalQuery).requireCurrent();

        assertThat(result).isEqualTo(persisted);
        assertThat(result.hasAuthority("ROLE_OPERATOR")).isTrue();
        verify(principalQuery).findByEmail("live@example.com");
    }

    @Test
    void anonymousAndUnauthenticatedAreEmptyOrUnauthorized() {
        SpringSecurityCurrentPrincipalAdapter adapter = new SpringSecurityCurrentPrincipalAdapter(principalQuery);
        assertThat(adapter.findCurrent()).isEmpty();
        assertThatThrownBy(adapter::requireCurrent).isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void missingPersistenceUserIsNotReplacedByJwtAuthorities() {
        when(principalQuery.findByEmail("missing@example.com")).thenReturn(Optional.empty());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("missing@example.com", "token", Set.of(() -> "ROLE_ADMIN")));

        SpringSecurityCurrentPrincipalAdapter adapter = new SpringSecurityCurrentPrincipalAdapter(principalQuery);
        assertThat(adapter.findCurrent()).isEmpty();
        assertThatThrownBy(adapter::requireCurrent).isInstanceOf(UnauthorizedException.class);
    }
}
