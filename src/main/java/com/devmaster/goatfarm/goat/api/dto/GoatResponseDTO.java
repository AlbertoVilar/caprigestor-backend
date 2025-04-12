package com.devmaster.goatfarm.goat.api.dto;

import com.devmaster.goatfarm.goat.enums.Category;
import com.devmaster.goatfarm.goat.enums.Gender;
import com.devmaster.goatfarm.goat.enums.GoatBreed;
import com.devmaster.goatfarm.goat.enums.GoatStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
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

    private Long farmId;
    private String farmName;

    public GoatResponseDTO(String registrationNumber,
                           String name, Gender gender,
                           GoatBreed breed, String color,
                           LocalDate birthDate, GoatStatus status,
                           String tod, String toe, Category category,
                           String fatherName, String fatherRegistrationNumber,
                           String motherName, String motherRegistrationNumber,
                           Long farmId, String farmName) {

        this.registrationNumber = registrationNumber;
        this.name = name;
        this.gender = gender;
        this.breed = breed;
        this.color = color;
        this.birthDate = birthDate;
        this.status = status;
        this.tod = tod;
        this.toe = toe;
        this.category = category;

        this.fatherName = fatherName;
        this.fatherRegistrationNumber = fatherRegistrationNumber;
        this.motherName = motherName;
        this.motherRegistrationNumber = motherRegistrationNumber;

        this.farmId = farmId;
        this.farmName = farmName;
    }
}
