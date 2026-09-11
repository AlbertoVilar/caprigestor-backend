package com.devmaster.goatfarm.goat.api.dto;

import com.devmaster.goatfarm.goat.enums.RegistrationRectificationSource;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GoatRegistrationRectificationRequestDTO {

    @NotBlank(message = "O TOD corrigido é obrigatório.")
    @Size(max = 15, message = "O TOD deve ter no máximo 15 caracteres.")
    private String tod;

    @NotBlank(message = "O TOE corrigido é obrigatório.")
    @Size(max = 15, message = "O TOE deve ter no máximo 15 caracteres.")
    private String toe;

    @NotNull(message = "A origem da retificação é obrigatória.")
    private RegistrationRectificationSource source;

    @NotBlank(message = "A referência da evidência é obrigatória.")
    @Size(max = 255, message = "A referência da evidência deve ter no máximo 255 caracteres.")
    private String evidenceReference;

    @NotBlank(message = "O motivo da retificação é obrigatório.")
    @Size(max = 500, message = "O motivo deve ter no máximo 500 caracteres.")
    private String reason;
}
