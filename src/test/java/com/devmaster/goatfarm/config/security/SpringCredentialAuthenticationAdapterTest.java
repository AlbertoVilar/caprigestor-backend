package com.devmaster.goatfarm.config.security;

import com.devmaster.goatfarm.authority.persistence.entity.Role;
import com.devmaster.goatfarm.authority.persistence.entity.User;
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
        User user = new User();
        user.setId(9L);
        user.setEmail("owner@example.com");
        user.setName("Owner");
        Role role = new Role();
        role.setAuthority("ROLE_FARM_OWNER");
        user.addRole(role);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(user);
        when(manager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);

        var principal = new SpringCredentialAuthenticationAdapter(manager).authenticate("owner@example.com", "secret");

        assertThat(principal.id()).isEqualTo(9L);
        assertThat(principal.email()).isEqualTo("owner@example.com");
        assertThat(principal.authorities()).containsExactly("ROLE_FARM_OWNER");
    }

    @Test
    void translatesBadCredentialsWithoutLeakingSpringException() {
        AuthenticationManager manager = mock(AuthenticationManager.class);
        when(manager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("bad"));

        assertThrows(InvalidArgumentException.class,
                () -> new SpringCredentialAuthenticationAdapter(manager).authenticate("owner@example.com", "wrong"));
    }
}
