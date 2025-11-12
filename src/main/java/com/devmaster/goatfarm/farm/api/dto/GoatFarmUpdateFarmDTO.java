package com.devmaster.goatfarm.farm.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GoatFarmUpdateFarmDTO {

    @NotBlank(message = "Nome da fazenda é obrigatório")
    @Size(min = 3, max = 100, message = "Nome da fazenda deve ter entre 3 e 100 caracteres")
    private String name;

    @Size(min = 5, max = 5, message = "TOD deve ter 5 caracteres")
    private String tod;

    private Integer version;
}
