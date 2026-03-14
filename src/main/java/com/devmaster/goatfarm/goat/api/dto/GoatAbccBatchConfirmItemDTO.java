package com.devmaster.goatfarm.goat.api.dto;

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
public class GoatAbccBatchConfirmItemDTO {

    @NotBlank(message = "Identificador externo da ABCC é obrigatório.")
    @Size(max = 32, message = "Identificador externo deve ter no máximo {max} caracteres.")
    private String externalId;
}
