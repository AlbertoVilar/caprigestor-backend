package com.devmaster.goatfarm.application.pagination;

import java.util.Objects;

public record SortSpec(String field, SortDirection direction) {
    public SortSpec {
        Objects.requireNonNull(field, "field");
        Objects.requireNonNull(direction, "direction");
    }
}
