package com.devmaster.goatfarm.goat.business.bo;

import com.devmaster.goatfarm.goat.enums.Category;
import com.devmaster.goatfarm.goat.enums.Gender;
import com.devmaster.goatfarm.goat.enums.GoatBreed;
import com.devmaster.goatfarm.goat.enums.GoatStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class GoatRequestVO {

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

    private String fatherRegistrationNumber;

    private String motherRegistrationNumber;

    private Long farmId;


    public GoatRequestVO() {
    }

    public GoatRequestVO(String registrationNumber,
                         String name, Gender gender,
                         GoatBreed breed, String color,
                         LocalDate birthDate,
                         GoatStatus status,
                         String tod,
                         String toe,
                         Category category,
                         String fatherRegistrationNumber,
                         String motherRegistrationNumber, Long farmId) {

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
        this.fatherRegistrationNumber = fatherRegistrationNumber;
        this.motherRegistrationNumber = motherRegistrationNumber;
        this.farmId = farmId;
    }


}
