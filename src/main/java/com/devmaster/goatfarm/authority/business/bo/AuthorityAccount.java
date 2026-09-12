package com.devmaster.goatfarm.authority.business.bo;

import java.util.Set;

/**
 * Application-owned account representation. Persistence relationships and
 * framework interfaces stay outside the Authority core.
 */
public record AuthorityAccount(
        Long id,
        String name,
        String email,
        String cpf,
        String encodedPassword,
        Set<String> roles
) {
    public AuthorityAccount {
        roles = roles == null ? Set.of() : Set.copyOf(roles);
    }

    public boolean hasAuthority(String authority) {
        return roles.contains(authority);
    }
}
