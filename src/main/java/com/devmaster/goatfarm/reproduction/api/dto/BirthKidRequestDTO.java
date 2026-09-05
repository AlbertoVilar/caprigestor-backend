package com.devmaster.goatfarm.reproduction.api.dto;

import com.devmaster.goatfarm.goat.enums.Category;
import com.devmaster.goatfarm.goat.enums.Gender;
import com.devmaster.goatfarm.goat.enums.GoatBreed;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BirthKidRequestDTO {

    @NotBlank(message = "Registro da cria é obrigatório")
    @Size(min = 10, max = 12, message = "Registro da cria deve ter entre 10 e 12 caracteres")
    @Pattern(
            regexp = "^(?=.{10,12}$)[0-9]+[A-Za-z]?$",
            message = "Registro da cria deve conter números e, opcionalmente, uma letra final"
    )
    private String registrationNumber;

    @NotBlank(message = "Nome da cria é obrigatório")
    private String name;

    @NotNull(message = "Sexo da cria é obrigatório")
    private Gender gender;

    private GoatBreed breed;
    private String color;
    private LocalDate birthDate;
    private Category category;
}
