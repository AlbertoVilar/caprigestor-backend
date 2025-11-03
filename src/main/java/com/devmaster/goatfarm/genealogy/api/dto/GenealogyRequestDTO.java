package com.devmaster.goatfarm.genealogy.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class GenealogyRequestDTO {

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

        private String fatherName;
    private String fatherRegistration;
    private String motherName;
    private String motherRegistration;

        private String paternalGrandfatherName;
    private String paternalGrandfatherRegistration;
    private String paternalGrandmotherName;
    private String paternalGrandmotherRegistration;

        private String maternalGrandfatherName;
    private String maternalGrandfatherRegistration;
    private String maternalGrandmotherName;
    private String maternalGrandmotherRegistration;

        private String paternalGreatGrandfather1Name;
    private String paternalGreatGrandfather1Registration;
    private String paternalGreatGrandmother1Name;
    private String paternalGreatGrandmother1Registration;
    private String paternalGreatGrandfather2Name;
    private String paternalGreatGrandfather2Registration;
    private String paternalGreatGrandmother2Name;
    private String paternalGreatGrandmother2Registration;

        private String maternalGreatGrandfather1Name;
    private String maternalGreatGrandfather1Registration;
    private String maternalGreatGrandmother1Name;
    private String maternalGreatGrandmother1Registration;
    private String maternalGreatGrandfather2Name;
    private String maternalGreatGrandfather2Registration;
    private String maternalGreatGrandmother2Name;
    private String maternalGreatGrandmother2Registration;

    public String getGoatName() { return goatName; }
    public String getGoatRegistration() { return goatRegistration; }
    public String getBreeder() { return breeder; }
    public String getFarmOwner() { return farmOwner; }
    public String getBreed() { return breed; }
    public String getColor() { return color; }
    public String getStatus() { return status; }
    public String getGender() { return gender; }
    public String getCategory() { return category; }
    public String getTod() { return tod; }
    public String getToe() { return toe; }
    public String getBirthDate() { return birthDate; }

    public String getFatherName() { return fatherName; }
    public String getFatherRegistration() { return fatherRegistration; }
    public String getMotherName() { return motherName; }
    public String getMotherRegistration() { return motherRegistration; }

    public String getPaternalGrandfatherName() { return paternalGrandfatherName; }
    public String getPaternalGrandfatherRegistration() { return paternalGrandfatherRegistration; }
    public String getPaternalGrandmotherName() { return paternalGrandmotherName; }
    public String getPaternalGrandmotherRegistration() { return paternalGrandmotherRegistration; }

    public String getMaternalGrandfatherName() { return maternalGrandfatherName; }
    public String getMaternalGrandfatherRegistration() { return maternalGrandfatherRegistration; }
    public String getMaternalGrandmotherName() { return maternalGrandmotherName; }
    public String getMaternalGrandmotherRegistration() { return maternalGrandmotherRegistration; }

    public String getPaternalGreatGrandfather1Name() { return paternalGreatGrandfather1Name; }
    public String getPaternalGreatGrandfather1Registration() { return paternalGreatGrandfather1Registration; }
    public String getPaternalGreatGrandmother1Name() { return paternalGreatGrandmother1Name; }
    public String getPaternalGreatGrandmother1Registration() { return paternalGreatGrandmother1Registration; }
    public String getPaternalGreatGrandfather2Name() { return paternalGreatGrandfather2Name; }
    public String getPaternalGreatGrandfather2Registration() { return paternalGreatGrandfather2Registration; }
    public String getPaternalGreatGrandmother2Name() { return paternalGreatGrandmother2Name; }
    public String getPaternalGreatGrandmother2Registration() { return paternalGreatGrandmother2Registration; }

    public String getMaternalGreatGrandfather1Name() { return maternalGreatGrandfather1Name; }
    public String getMaternalGreatGrandfather1Registration() { return maternalGreatGrandfather1Registration; }
    public String getMaternalGreatGrandmother1Name() { return maternalGreatGrandmother1Name; }
    public String getMaternalGreatGrandmother1Registration() { return maternalGreatGrandmother1Registration; }
    public String getMaternalGreatGrandfather2Name() { return maternalGreatGrandfather2Name; }
    public String getMaternalGreatGrandfather2Registration() { return maternalGreatGrandfather2Registration; }
    public String getMaternalGreatGrandmother2Name() { return maternalGreatGrandmother2Name; }
    public String getMaternalGreatGrandmother2Registration() { return maternalGreatGrandmother2Registration; }
}

