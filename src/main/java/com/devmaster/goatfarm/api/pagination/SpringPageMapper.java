package com.devmaster.goatfarm.api.pagination;

import com.devmaster.goatfarm.application.pagination.PageQuery;
import com.devmaster.goatfarm.application.pagination.PageResult;
import com.devmaster.goatfarm.application.pagination.SortDirection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

public final class SpringPageMapper {
    private SpringPageMapper() { }

    public static PageQuery toQuery(Pageable pageable) {
        List<com.devmaster.goatfarm.application.pagination.SortSpec> sort = pageable.getSort().stream()
                .map(order -> new com.devmaster.goatfarm.application.pagination.SortSpec(order.getProperty(),
                        order.getDirection().isAscending() ? SortDirection.ASC : SortDirection.DESC))
                .toList();
        return new PageQuery(pageable.getPageNumber(), pageable.getPageSize(), sort);
    }

    public static <T> Page<T> toSpringPage(PageResult<T> result, Pageable pageable) {
        return new PageImpl<>(result.content(), pageable, result.totalElements());
    }
}
