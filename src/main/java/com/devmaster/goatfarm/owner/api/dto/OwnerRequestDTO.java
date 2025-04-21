package com.devmaster.goatfarm.owner.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.validator.constraints.br.CPF;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OwnerRequestDTO {

    @NotBlank(message = "O nome do proprietário não pode estar em branco.")
    @Size(min = 3, max = 150, message = "O nome do proprietário deve ter entre 3 e 255 caracteres.")
    private String name;

    @NotBlank(message = "O CPF não pode estar em branco.")
    @CPF(message = "O CPF informado é inválido.")
    @Pattern(regexp = "^\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}$", message = "O CPF deve estar no formato XXX.XXX.XXX-XX.")
    private String cpf;

    @NotBlank(message = "O email não pode estar em branco.")
    @Email(message = "O email informado é inválido.")
    @Size(max = 255, message = "O email não pode ter mais de 255 caracteres.")
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "O email deve estar em um formato válido.")
    private String email;
}
