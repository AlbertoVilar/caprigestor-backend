package com.devmaster.goatfarm.genealogy.business.bo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class GenealogyResponseVO {

    // Animal data
    private String goatName;
    private String goatRegistration;
    private String breeder;
    private String farmOwner;
    private String breed;
    private String color;
    private String status;
    private String gender;
    private String category;
    private String tod;
    private String toe;
    private String birthDate;

    // Parents
    private String fatherName;
    private String fatherRegistration;
    private String motherName;
    private String motherRegistration;

    // Paternal grandparents
    private String paternalGrandfatherName;
    private String paternalGrandfatherRegistration;
    private String paternalGrandmotherName;
    private String paternalGrandmotherRegistration;

    // Maternal grandparents
    private String maternalGrandfatherName;
    private String maternalGrandfatherRegistration;
    private String maternalGrandmotherName;
    private String maternalGrandmotherRegistration;

    // Paternal great-grandparents
    private String paternalGreatGrandfather1Name;
    private String paternalGreatGrandfather1Registration;
    private String paternalGreatGrandmother1Name;
    private String paternalGreatGrandmother1Registration;
    private String paternalGreatGrandfather2Name;
    private String paternalGreatGrandfather2Registration;
    private String paternalGreatGrandmother2Name;
    private String paternalGreatGrandmother2Registration;

    // Maternal great-grandparents
    private String maternalGreatGrandfather1Name;
    private String maternalGreatGrandfather1Registration;
    private String maternalGreatGrandmother1Name;
    private String maternalGreatGrandmother1Registration;
    private String maternalGreatGrandfather2Name;
    private String maternalGreatGrandfather2Registration;
    private String maternalGreatGrandmother2Name;
    private String maternalGreatGrandmother2Registration;
}

