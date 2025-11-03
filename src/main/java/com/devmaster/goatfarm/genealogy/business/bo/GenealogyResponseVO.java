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


    public void setToe(String toe) { this.toe = toe; }
    
    public String getBirthDate() { return birthDate; }
    public void setBirthDate(String birthDate) { this.birthDate = birthDate; }
    
    public String getFatherName() { return fatherName; }
    public void setFatherName(String fatherName) { this.fatherName = fatherName; }
    
    public String getFatherRegistration() { return fatherRegistration; }
    public void setFatherRegistration(String fatherRegistration) { this.fatherRegistration = fatherRegistration; }
    
    public String getMotherName() { return motherName; }
    public void setMotherName(String motherName) { this.motherName = motherName; }
    
    public String getMotherRegistration() { return motherRegistration; }
    public void setMotherRegistration(String motherRegistration) { this.motherRegistration = motherRegistration; }
    
        public String getPaternalGrandfatherName() { return paternalGrandfatherName; }
    public void setPaternalGrandfatherName(String paternalGrandfatherName) { this.paternalGrandfatherName = paternalGrandfatherName; }
    
    public String getPaternalGrandfatherRegistration() { return paternalGrandfatherRegistration; }
    public void setPaternalGrandfatherRegistration(String paternalGrandfatherRegistration) { this.paternalGrandfatherRegistration = paternalGrandfatherRegistration; }
    
    public String getPaternalGrandmotherName() { return paternalGrandmotherName; }
    public void setPaternalGrandmotherName(String paternalGrandmotherName) { this.paternalGrandmotherName = paternalGrandmotherName; }
    
    public String getPaternalGrandmotherRegistration() { return paternalGrandmotherRegistration; }
    public void setPaternalGrandmotherRegistration(String paternalGrandmotherRegistration) { this.paternalGrandmotherRegistration = paternalGrandmotherRegistration; }
    
        public String getMaternalGrandfatherName() { return maternalGrandfatherName; }
    public void setMaternalGrandfatherName(String maternalGrandfatherName) { this.maternalGrandfatherName = maternalGrandfatherName; }
    
    public String getMaternalGrandfatherRegistration() { return maternalGrandfatherRegistration; }
    public void setMaternalGrandfatherRegistration(String maternalGrandfatherRegistration) { this.maternalGrandfatherRegistration = maternalGrandfatherRegistration; }
    
    public String getMaternalGrandmotherName() { return maternalGrandmotherName; }
    public void setMaternalGrandmotherName(String maternalGrandmotherName) { this.maternalGrandmotherName = maternalGrandmotherName; }
    
    public String getMaternalGrandmotherRegistration() { return maternalGrandmotherRegistration; }
    public void setMaternalGrandmotherRegistration(String maternalGrandmotherRegistration) { this.maternalGrandmotherRegistration = maternalGrandmotherRegistration; }
    
        public String getPaternalGreatGrandfather1Name() { return paternalGreatGrandfather1Name; }
    public void setPaternalGreatGrandfather1Name(String paternalGreatGrandfather1Name) { this.paternalGreatGrandfather1Name = paternalGreatGrandfather1Name; }
    
    public String getPaternalGreatGrandfather1Registration() { return paternalGreatGrandfather1Registration; }
    public void setPaternalGreatGrandfather1Registration(String paternalGreatGrandfather1Registration) { this.paternalGreatGrandfather1Registration = paternalGreatGrandfather1Registration; }
    
    public String getPaternalGreatGrandmother1Name() { return paternalGreatGrandmother1Name; }
    public void setPaternalGreatGrandmother1Name(String paternalGreatGrandmother1Name) { this.paternalGreatGrandmother1Name = paternalGreatGrandmother1Name; }
    
    public String getPaternalGreatGrandmother1Registration() { return paternalGreatGrandmother1Registration; }
    public void setPaternalGreatGrandmother1Registration(String paternalGreatGrandmother1Registration) { this.paternalGreatGrandmother1Registration = paternalGreatGrandmother1Registration; }
    
    public String getPaternalGreatGrandfather2Name() { return paternalGreatGrandfather2Name; }
    public void setPaternalGreatGrandfather2Name(String paternalGreatGrandfather2Name) { this.paternalGreatGrandfather2Name = paternalGreatGrandfather2Name; }
    
    public String getPaternalGreatGrandfather2Registration() { return paternalGreatGrandfather2Registration; }
    public void setPaternalGreatGrandfather2Registration(String paternalGreatGrandfather2Registration) { this.paternalGreatGrandfather2Registration = paternalGreatGrandfather2Registration; }
    
    public String getPaternalGreatGrandmother2Name() { return paternalGreatGrandmother2Name; }
    public void setPaternalGreatGrandmother2Name(String paternalGreatGrandmother2Name) { this.paternalGreatGrandmother2Name = paternalGreatGrandmother2Name; }
    
    public String getPaternalGreatGrandmother2Registration() { return paternalGreatGrandmother2Registration; }
    public void setPaternalGreatGrandmother2Registration(String paternalGreatGrandmother2Registration) { this.paternalGreatGrandmother2Registration = paternalGreatGrandmother2Registration; }
    
        public String getMaternalGreatGrandfather1Name() { return maternalGreatGrandfather1Name; }
    public void setMaternalGreatGrandfather1Name(String maternalGreatGrandfather1Name) { this.maternalGreatGrandfather1Name = maternalGreatGrandfather1Name; }
    
    public String getMaternalGreatGrandfather1Registration() { return maternalGreatGrandfather1Registration; }
    public void setMaternalGreatGrandfather1Registration(String maternalGreatGrandfather1Registration) { this.maternalGreatGrandfather1Registration = maternalGreatGrandfather1Registration; }
    
    public String getMaternalGreatGrandmother1Name() { return maternalGreatGrandmother1Name; }
    public void setMaternalGreatGrandmother1Name(String maternalGreatGrandmother1Name) { this.maternalGreatGrandmother1Name = maternalGreatGrandmother1Name; }
    
    public String getMaternalGreatGrandmother1Registration() { return maternalGreatGrandmother1Registration; }
    public void setMaternalGreatGrandmother1Registration(String maternalGreatGrandmother1Registration) { this.maternalGreatGrandmother1Registration = maternalGreatGrandmother1Registration; }
    
    public String getMaternalGreatGrandfather2Name() { return maternalGreatGrandfather2Name; }
    public void setMaternalGreatGrandfather2Name(String maternalGreatGrandfather2Name) { this.maternalGreatGrandfather2Name = maternalGreatGrandfather2Name; }
    
    public String getMaternalGreatGrandfather2Registration() { return maternalGreatGrandfather2Registration; }
    public void setMaternalGreatGrandfather2Registration(String maternalGreatGrandfather2Registration) { this.maternalGreatGrandfather2Registration = maternalGreatGrandfather2Registration; }
    
    public String getMaternalGreatGrandmother2Name() { return maternalGreatGrandmother2Name; }
    public void setMaternalGreatGrandmother2Name(String maternalGreatGrandmother2Name) { this.maternalGreatGrandmother2Name = maternalGreatGrandmother2Name; }
    
    public String getMaternalGreatGrandmother2Registration() { return maternalGreatGrandmother2Registration; }
    public void setMaternalGreatGrandmother2Registration(String maternalGreatGrandmother2Registration) { this.maternalGreatGrandmother2Registration = maternalGreatGrandmother2Registration; }
    
    public String getBreeder() { return breeder; }
    public void setBreeder(String breeder) { this.breeder = breeder; }
    
    public String getFarmOwner() { return farmOwner; }
    public void setFarmOwner(String farmOwner) { this.farmOwner = farmOwner; }
    
    public String getBreed() { return breed; }
    public void setBreed(String breed) { this.breed = breed; }
    
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public String getTod() { return tod; }
    public void setTod(String tod) { this.tod = tod; }
    public void setGoatName(String goatName) { this.goatName = goatName; }
    public void setGoatRegistration(String goatRegistration) { this.goatRegistration = goatRegistration; }
}

