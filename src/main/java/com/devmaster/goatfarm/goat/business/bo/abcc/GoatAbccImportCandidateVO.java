package com.devmaster.goatfarm.goat.business.bo.abcc;

import com.devmaster.goatfarm.goat.enums.Category;
import com.devmaster.goatfarm.goat.enums.Gender;
import com.devmaster.goatfarm.goat.enums.GoatBreed;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/** ABCC-derived creation data, deliberately excluding CapriGestor lifecycle status. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoatAbccImportCandidateVO {
    private String registrationNumber;
    private String name;
    private Gender gender;
    private GoatBreed breed;
    private String color;
    private LocalDate birthDate;
    private String tod;
    private String toe;
    private Category category;
    private String fatherRegistrationNumber;
    private String motherRegistrationNumber;
}
