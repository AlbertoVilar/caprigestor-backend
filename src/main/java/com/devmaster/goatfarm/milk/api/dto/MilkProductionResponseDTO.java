package com.devmaster.goatfarm.milk.api.dto;

import com.devmaster.goatfarm.milk.enums.MilkingShift;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MilkProductionResponseDTO {
    private Long id;
    private LocalDate date;
    private MilkingShift shift;
    private BigDecimal volumeLiters;
    private String notes;
}
