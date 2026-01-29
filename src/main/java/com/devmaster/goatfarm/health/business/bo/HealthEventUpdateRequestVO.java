package com.devmaster.goatfarm.health.business.bo;

import com.devmaster.goatfarm.health.domain.enums.AdministrationRoute;
import com.devmaster.goatfarm.health.domain.enums.DoseUnit;
import com.devmaster.goatfarm.health.domain.enums.HealthEventType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record HealthEventUpdateRequestVO(

        HealthEventType type,
        String title,
        String description,
        LocalDate scheduledDate,
        String productName,
        BigDecimal dose,
        DoseUnit doseUnit,
        AdministrationRoute route,
        Integer withdrawalMilkDays,
        Integer withdrawalMeatDays,
        String notes
) {

}
