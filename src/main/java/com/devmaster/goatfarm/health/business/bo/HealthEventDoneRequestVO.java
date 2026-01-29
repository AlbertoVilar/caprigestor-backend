package com.devmaster.goatfarm.health.business.bo;

import java.time.LocalDateTime;

public record HealthEventDoneRequestVO(

        LocalDateTime performedAt,
        String responsible,
        String notes
) {
}
