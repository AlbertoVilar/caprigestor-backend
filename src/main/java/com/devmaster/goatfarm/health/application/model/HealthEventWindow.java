package com.devmaster.goatfarm.health.application.model;

import java.util.List;

/**
 * Bounded, intent-oriented result used by farm health alerts.
 *
 * <p>This is deliberately not a generic pagination model: the caller asks for
 * the first matching events while retaining the total count for the alert.</p>
 */
public record HealthEventWindow(List<HealthEventRecord> content, long totalElements) {
    public HealthEventWindow {
        content = content == null ? List.of() : List.copyOf(content);
    }
}
