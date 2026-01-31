package com.devmaster.goatfarm.health.business.bo;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record HealthEventDoneRequestVO(
    LocalDateTime performedAt,
    String responsible,
    String notes
) {}
