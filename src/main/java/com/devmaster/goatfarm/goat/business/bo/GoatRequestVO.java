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

    private String fatherName;
    private String fatherRegistrationNumber;

    private String motherName;
    private String motherRegistrationNumber;

    private Long farmId;
    private String farmName;

    public GoatRequestVO(String registrationNumber,
                         String name, Gender gender,
                         GoatBreed breed, String color,
                         LocalDate birthDate, GoatStatus status,
                         String tod, String toe, Category category,
                         String fatherName, String fatherRegistrationNumber,
                         String motherName, String motherRegistrationNumber,
                         Long farmId, String farmName) {
    }

}
