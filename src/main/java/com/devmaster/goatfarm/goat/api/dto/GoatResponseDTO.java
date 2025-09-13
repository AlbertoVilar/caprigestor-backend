package com.devmaster.goatfarm.goat.api.dto;

import com.devmaster.goatfarm.goat.enums.Category;
import com.devmaster.goatfarm.goat.enums.Gender;
import com.devmaster.goatfarm.goat.enums.GoatBreed;
import com.devmaster.goatfarm.goat.enums.GoatStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoatResponseDTO {

    private String registrationNumber;
    private String name;
    private Gender gender;
    private GoatBreed breed;
    private String color;
    private LocalDate birthDate;
    private GoatStatus status;
    private String tod;
    private String toe;
    private Category category;

    private String fatherName;
    private String fatherRegistrationNumber;

    private String motherName;
    private String motherRegistrationNumber;

    private String userName;  // <-- Campo para o nome do usuário

    private Long farmId;
    private String farmName;

   }
