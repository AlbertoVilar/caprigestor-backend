package com.devmaster.goatfarm.reproduction.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BirthRequestDTO {

    @NotNull(message = "Data do parto é obrigatória")
    private LocalDate birthDate;

    private String fatherRegistrationNumber;

    private String notes;

    @NotEmpty(message = "É necessário informar ao menos uma cria")
    @Valid
    private List<BirthKidRequestDTO> kids;
}
