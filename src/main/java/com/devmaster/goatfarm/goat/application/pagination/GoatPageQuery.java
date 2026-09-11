package com.devmaster.goatfarm.goat.application.pagination;

/** Pagination query owned by the Goat application boundary. */
public record GoatPageQuery(int page, int size, String sort) {

    public GoatPageQuery {
        if (page < 0) {
            throw new IllegalArgumentException("page must not be negative");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("size must be positive");
        }
        sort = sort == null ? "" : sort;
    }
}
