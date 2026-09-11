package com.devmaster.goatfarm.events.application.ports.out;

import java.util.List;

/** Application-owned page result; no Spring Data type crosses the port. */
public record EventPage<T>(List<T> content, long totalElements, int page, int size) {
}
