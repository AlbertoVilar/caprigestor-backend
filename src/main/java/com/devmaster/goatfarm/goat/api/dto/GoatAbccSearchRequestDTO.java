package com.devmaster.goatfarm.goat.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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
public class GoatAbccSearchRequestDTO {

    @NotNull(message = "Raça ABCC é obrigatória.")
    private Integer raceId;

    @NotBlank(message = "Afixo é obrigatório.")
    @Size(max = 80, message = "Afixo deve ter no máximo {max} caracteres.")
    private String affix;

    @Min(value = 1, message = "Página deve ser maior ou igual a 1.")
    private Integer page;

    @Pattern(regexp = "0|1", message = "Filtro de sexo inválido. Use 1 para macho ou 0 para fêmea.")
    private String sex;

    @Size(max = 20, message = "TOD deve ter no máximo {max} caracteres.")
    private String tod;

    @Size(max = 20, message = "TOE deve ter no máximo {max} caracteres.")
    private String toe;

    @Size(max = 120, message = "Nome deve ter no máximo {max} caracteres.")
    private String name;

    @Pattern(regexp = "0|1", message = "Filtro de DNA inválido. Use 1 para sim ou 0 para não.")
    private String dna;
}

