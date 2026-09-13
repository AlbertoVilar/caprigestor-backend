package com.devmaster.goatfarm.api.pagination;

import com.devmaster.goatfarm.application.pagination.PageQuery;
import com.devmaster.goatfarm.application.pagination.SortDirection;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SpringPageMapperCharacterizationTest {

    @Test
    void mapsEverySpringSortOrderInOriginalOrder() {
        var pageable = PageRequest.of(2, 12, Sort.by(
                new Sort.Order(Sort.Direction.DESC, "publishedAt"),
                new Sort.Order(Sort.Direction.ASC, "title")));

        PageQuery query = SpringPageMapper.toQuery(pageable);

        assertEquals(2, query.page());
        assertEquals(12, query.size());
        assertEquals("publishedAt", query.sort().get(0).field());
        assertEquals(SortDirection.DESC, query.sort().get(0).direction());
        assertEquals("title", query.sort().get(1).field());
        assertEquals(SortDirection.ASC, query.sort().get(1).direction());
    }
}
