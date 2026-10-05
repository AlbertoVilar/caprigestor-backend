package com.devmaster.goatfarm.application.pagination;

import java.util.List;
import java.util.function.Function;

public record PageResult<T>(List<T> content, long totalElements, int page, int size) {
    public PageResult {
        content = content == null ? List.of() : List.copyOf(content);
    }

    public <R> PageResult<R> map(Function<? super T, R> mapper) {
        return new PageResult<>(content.stream().map(mapper).toList(), totalElements, page, size);
    }
}
