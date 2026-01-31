package com.devmaster.goatfarm.health.business.bo;

import lombok.Builder;

@Builder
public record HealthEventCancelRequestVO(
    String notes
) {}
