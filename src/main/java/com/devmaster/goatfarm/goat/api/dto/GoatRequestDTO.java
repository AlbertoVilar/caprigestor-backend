package com.devmaster.goatfarm.goat.api.dto;

import com.devmaster.goatfarm.goat.enums.Category;
import com.devmaster.goatfarm.goat.enums.Gender;
import com.devmaster.goatfarm.goat.enums.GoatBreed;
import com.devmaster.goatfarm.goat.enums.GoatStatus;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class GoatRequestDTO {

    @NotBlank(message = "O número de registro não pode estar em branco.")
    @Size(min = 10, max = 12, message = "O registro deve ter entre {min} e {max} caracteres.")
    private String registrationNumber;

    @NotBlank(message = "O nome não pode estar em branco.")
    @Size(min = 3, max = 60, message = "O nome deve ter entre {min} e {max} caracteres.")
    private String name;

    @NotNull(message = "O sexo não pode estar em branco.")
    private Gender gender;

    @NotNull(message = "A raça não pode estar em branco.")
    private GoatBreed breed;

    @NotBlank(message = "A cor não pode estar em branco.")
    private String color;

    @NotNull(message = "A data de nascimento não pode estar em branco.")
    private LocalDate birthDate;

    @NotNull(message = "O status não pode estar em branco.")
    private GoatStatus status;

    @NotBlank(message = "A TOD não pode estar em branco.")
    @Size(min = 5, max = 5, message = "A TOD deve ter {max} caracteres.")
    private String tod;

    @NotBlank(message = "A TOE não pode estar em branco.")
    @Size(min = 5, max = 7, message = "A TOE deve ter entre {min} e {max} caracteres.")
    private String toe;

    @NotNull(message = "A categoria não pode estar vazia.")
    private Category category;

    @Size(min = 10, max = 12, message = "O número de registro do pai deve ter entre {min} e {max} caracteres.")
    private String fatherRegistrationNumber;

    @Size(min = 10, max = 12, message = "O número de registro da mãe deve ter entre {min} e {max} caracteres.")
    private String motherRegistrationNumber;

    @NotNull(message = "O ID da fazenda é obrigatório.")
    private Long farmId;

    @NotNull(message = "O ID do usuário é obrigatório.")
    private Long userId;

    public GoatRequestDTO() {
    }
}
