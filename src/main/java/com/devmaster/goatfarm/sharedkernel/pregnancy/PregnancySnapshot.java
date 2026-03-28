package com.devmaster.goatfarm.sharedkernel.pregnancy;

import java.time.LocalDate;

public record PregnancySnapshot(boolean active, LocalDate breedingDate, LocalDate confirmDate, String closeReason) {

    public PregnancySnapshot(boolean active, LocalDate breedingDate, LocalDate confirmDate) {
        this(active, breedingDate, confirmDate, null);
    }
}
