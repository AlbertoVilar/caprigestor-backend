package com.devmaster.goatfarm.farm.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GoatFarmRequestDTO {

    @NotBlank(message = "Nome da fazenda é obrigatório")
    @Size(min = 3, max = 100, message = "Nome da fazenda deve ter entre 3 e 100 caracteres")
    private String name;

    @Size(min = 5, max = 5, message = "TOD deve ter 5 caracteres")
    private String tod;

    @Schema(description = "URL do logo do capril (http/https)", example = "https://example.com/logo.png")
    private String logoUrl;

    @NotNull(message = "ID do usuário precisa ser passado")
    private Long userId;

    @NotNull(message = "ID do endereço precisa ser passado")
    private Long addressId;

    @NotNull(message = "Pelo menos um telefone precisa ser passado")
    private List<Long> phoneIds;

    private Integer version;
}
