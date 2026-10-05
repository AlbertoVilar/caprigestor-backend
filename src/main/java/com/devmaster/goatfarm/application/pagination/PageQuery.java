package com.devmaster.goatfarm.application.pagination;

import java.util.List;

public record PageQuery(int page, int size, List<SortSpec> sort) {
    public PageQuery {
        sort = sort == null ? List.of() : List.copyOf(sort);
    }
}
