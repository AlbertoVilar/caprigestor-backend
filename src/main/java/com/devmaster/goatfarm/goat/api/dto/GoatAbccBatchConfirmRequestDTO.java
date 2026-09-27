package com.devmaster.goatfarm.goat.api.dto;

import com.devmaster.goatfarm.goat.enums.GoatStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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

    @NotNull(message = "Informe a situação local do animal no CapriGestor para confirmar a importação em lote.")
    private GoatStatus status;

    @Valid
    @NotEmpty(message = "Selecione ao menos um animal da página atual para importar.")
    private List<GoatAbccBatchConfirmItemDTO> items;
}
