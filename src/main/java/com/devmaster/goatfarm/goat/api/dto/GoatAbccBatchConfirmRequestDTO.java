package com.devmaster.goatfarm.goat.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoatAbccBatchConfirmRequestDTO {

    @Valid
    @NotEmpty(message = "Selecione ao menos um animal da página atual para importar.")
    private List<GoatAbccBatchConfirmItemDTO> items;
}
