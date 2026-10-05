package com.devmaster.goatfarm.farm.application.model;

import java.util.List;

/** Minimal owner projection required by Farm; credentials and persistence relations never cross this boundary. */
public record OwnerReference(Long id, String name, String email, String cpf, List<String> roles) {
    public OwnerReference { roles = roles == null ? List.of() : List.copyOf(roles); }
}
