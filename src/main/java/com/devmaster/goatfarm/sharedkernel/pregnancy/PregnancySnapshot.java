package com.devmaster.goatfarm.sharedkernel.pregnancy;

import java.time.LocalDate;

public record PregnancySnapshot(boolean active, LocalDate breedingDate, LocalDate confirmDate) {
}
