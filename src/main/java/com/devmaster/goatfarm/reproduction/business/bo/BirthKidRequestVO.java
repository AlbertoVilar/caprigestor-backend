package com.devmaster.goatfarm.reproduction.business.bo;

import com.devmaster.goatfarm.goat.enums.Category;
import com.devmaster.goatfarm.goat.enums.Gender;
import com.devmaster.goatfarm.goat.enums.GoatBreed;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BirthKidRequestVO {
    private String registrationNumber;
    private String name;
    private Gender gender;
    private GoatBreed breed;
    private String color;
    private LocalDate birthDate;
    private Category category;
}
