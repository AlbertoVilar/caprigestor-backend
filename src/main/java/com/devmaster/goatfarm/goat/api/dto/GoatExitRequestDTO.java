package com.devmaster.goatfarm.goat.api.dto;

import com.devmaster.goatfarm.goat.enums.GoatExitType;
import jakarta.validation.constraints.NotNull;
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
public class GoatExitRequestDTO {

    @NotNull(message = "O tipo de saida e obrigatorio.")
    private GoatExitType exitType;

    @NotNull(message = "A data de saida e obrigatoria.")
    private LocalDate exitDate;

    private String notes;
}
