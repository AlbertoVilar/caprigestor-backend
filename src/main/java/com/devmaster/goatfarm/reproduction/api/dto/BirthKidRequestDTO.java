package com.devmaster.goatfarm.reproduction.api.dto;

import com.devmaster.goatfarm.goat.enums.Category;
import com.devmaster.goatfarm.goat.enums.Gender;
import com.devmaster.goatfarm.goat.enums.GoatBreed;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
