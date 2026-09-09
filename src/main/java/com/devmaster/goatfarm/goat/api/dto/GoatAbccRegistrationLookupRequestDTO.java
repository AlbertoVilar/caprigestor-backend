package com.devmaster.goatfarm.goat.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoatAbccRegistrationLookupRequestDTO {

    @NotNull(message = "Identificador da raça ABCC é obrigatório.")
    @Min(value = 1, message = "Identificador da raça ABCC deve ser maior ou igual a 1.")
    private Integer raceId;

    @NotBlank(message = "Número de registro é obrigatório.")
    @Size(max = 40, message = "Número de registro deve ter no máximo {max} caracteres.")
    private String registrationNumber;
}
