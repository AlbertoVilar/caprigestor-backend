package com.devmaster.goatfarm.reproduction.business.bo;

import com.devmaster.goatfarm.goat.enums.Category;
import com.devmaster.goatfarm.goat.enums.Gender;
import com.devmaster.goatfarm.goat.enums.GoatBreed;
import com.devmaster.goatfarm.goat.enums.GoatStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BirthKidResponseVO {
    private String registrationNumber;
    private String name;
    private Gender gender;
    private GoatBreed breed;
    private String color;
    private LocalDate birthDate;
    private GoatStatus status;
    private Category category;
    private String fatherRegistrationNumber;
    private String motherRegistrationNumber;
}
