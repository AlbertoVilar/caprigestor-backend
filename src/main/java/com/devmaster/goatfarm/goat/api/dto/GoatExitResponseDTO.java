package com.devmaster.goatfarm.goat.api.dto;

import com.devmaster.goatfarm.goat.enums.GoatExitType;
import com.devmaster.goatfarm.goat.enums.GoatStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoatExitResponseDTO {
    private String goatId;
    private GoatExitType exitType;
    private LocalDate exitDate;
    private String notes;
    private GoatStatus previousStatus;
    private GoatStatus currentStatus;
}
