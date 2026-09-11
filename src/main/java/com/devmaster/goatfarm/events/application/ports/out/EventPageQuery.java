package com.devmaster.goatfarm.events.application.ports.out;

/** Application-owned pagination and sort request for operational events. */
public record EventPageQuery(int page, int size, String sort) {
}
