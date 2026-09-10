package com.devmaster.goatfarm.goat.application.ports.out;

/** Pagination contract that does not expose Spring Data to the application port. */
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
