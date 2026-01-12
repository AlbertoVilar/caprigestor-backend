package com.devmaster.goatfarm.reproduction.api.dto;

import com.devmaster.goatfarm.reproduction.enums.BreedingType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BreedingRequestDTO {
    @NotNull(message = "Date is required")
    private LocalDate eventDate;

    @NotNull(message = "Breeding type is required")
    private BreedingType breedingType;

    private String breederRef;
    private String notes;
}
