package com.devmaster.goatfarm.config.security;

import com.devmaster.goatfarm.authority.application.ports.out.UserPrincipalQueryPort;
import com.devmaster.goatfarm.authority.business.bo.AuthenticatedPrincipal;
import com.devmaster.goatfarm.config.exceptions.custom.InvalidArgumentException;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SpringCredentialAuthenticationAdapterTest {

    @Test
    void translatesAuthenticatedUserToApplicationPrincipal() {
        AuthenticationManager manager = mock(AuthenticationManager.class);
        UserPrincipalQueryPort query = mock(UserPrincipalQueryPort.class);
        when(query.findByEmail("owner@example.com")).thenReturn(java.util.Optional.of(
                new AuthenticatedPrincipal(9L, "owner@example.com", "Owner", java.util.Set.of("ROLE_FARM_OWNER"))));
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("owner@example.com");
        when(manager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);

        var principal = new SpringCredentialAuthenticationAdapter(manager, query).authenticate("owner@example.com", "secret");

        assertThat(principal.id()).isEqualTo(9L);
        assertThat(principal.email()).isEqualTo("owner@example.com");
        assertThat(principal.authorities()).containsExactly("ROLE_FARM_OWNER");
    }

    @Test
    void translatesBadCredentialsWithoutLeakingSpringException() {
        AuthenticationManager manager = mock(AuthenticationManager.class);
        UserPrincipalQueryPort query = mock(UserPrincipalQueryPort.class);
        when(manager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("bad"));

        assertThrows(InvalidArgumentException.class,
                () -> new SpringCredentialAuthenticationAdapter(manager, query).authenticate("owner@example.com", "wrong"));
    }
}
