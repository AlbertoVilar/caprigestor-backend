package com.devmaster.goatfarm.goat.application.pagination;

import java.util.List;
import java.util.function.Function;

/** Immutable page result owned by the Goat application boundary. */
public record GoatPage<T>(List<T> content, long totalElements, int page, int size) {

    public GoatPage {
        content = List.copyOf(content == null ? List.of() : content);
    }

    public <R> GoatPage<R> map(Function<T, R> mapper) {
        return new GoatPage<>(content.stream().map(mapper).toList(), totalElements, page, size);
    }
}
