package com.devmaster.goatfarm.genealogy.facade.dto;

/**
 * DTO de resposta do GenealogyFacade para encapsular dados da genealogia
 * sem expor detalhes internos dos VOs.
 */
public class GenealogyFacadeResponseDTO {

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

    // Pais
    private String fatherName;
    private String fatherRegistration;
    private String motherName;
    private String motherRegistration;

    // Avós paternos
    private String paternalGrandfatherName;
    private String paternalGrandfatherRegistration;
    private String paternalGrandmotherName;
    private String paternalGrandmotherRegistration;

    // Avós maternos
    private String maternalGrandfatherName;
    private String maternalGrandfatherRegistration;
    private String maternalGrandmotherName;
    private String maternalGrandmotherRegistration;

    public GenealogyFacadeResponseDTO() {
    }

    // Getters e Setters
    public String getGoatName() {
        return goatName;
    }

    public void setGoatName(String goatName) {
        this.goatName = goatName;
    }

    public String getGoatRegistration() {
        return goatRegistration;
    }

    public void setGoatRegistration(String goatRegistration) {
        this.goatRegistration = goatRegistration;
    }

    public String getBreeder() {
        return breeder;
    }

    public void setBreeder(String breeder) {
        this.breeder = breeder;
    }

    public String getFarmOwner() {
        return farmOwner;
    }

    public void setFarmOwner(String farmOwner) {
        this.farmOwner = farmOwner;
    }

    public String getBreed() {
        return breed;
    }

    public void setBreed(String breed) {
        this.breed = breed;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTod() {
        return tod;
    }

    public void setTod(String tod) {
        this.tod = tod;
    }

    public String getToe() {
        return toe;
    }

    public void setToe(String toe) {
        this.toe = toe;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public String getFatherName() {
        return fatherName;
    }

    public void setFatherName(String fatherName) {
        this.fatherName = fatherName;
    }

    public String getFatherRegistration() {
        return fatherRegistration;
    }

    public void setFatherRegistration(String fatherRegistration) {
        this.fatherRegistration = fatherRegistration;
    }

    public String getMotherName() {
        return motherName;
    }

    public void setMotherName(String motherName) {
        this.motherName = motherName;
    }

    public String getMotherRegistration() {
        return motherRegistration;
    }

    public void setMotherRegistration(String motherRegistration) {
        this.motherRegistration = motherRegistration;
    }

    public String getPaternalGrandfatherName() {
        return paternalGrandfatherName;
    }

    public void setPaternalGrandfatherName(String paternalGrandfatherName) {
        this.paternalGrandfatherName = paternalGrandfatherName;
    }

    public String getPaternalGrandfatherRegistration() {
        return paternalGrandfatherRegistration;
    }

    public void setPaternalGrandfatherRegistration(String paternalGrandfatherRegistration) {
        this.paternalGrandfatherRegistration = paternalGrandfatherRegistration;
    }

    public String getPaternalGrandmotherName() {
        return paternalGrandmotherName;
    }

    public void setPaternalGrandmotherName(String paternalGrandmotherName) {
        this.paternalGrandmotherName = paternalGrandmotherName;
    }

    public String getPaternalGrandmotherRegistration() {
        return paternalGrandmotherRegistration;
    }

    public void setPaternalGrandmotherRegistration(String paternalGrandmotherRegistration) {
        this.paternalGrandmotherRegistration = paternalGrandmotherRegistration;
    }

    public String getMaternalGrandfatherName() {
        return maternalGrandfatherName;
    }

    public void setMaternalGrandfatherName(String maternalGrandfatherName) {
        this.maternalGrandfatherName = maternalGrandfatherName;
    }

    public String getMaternalGrandfatherRegistration() {
        return maternalGrandfatherRegistration;
    }

    public void setMaternalGrandfatherRegistration(String maternalGrandfatherRegistration) {
        this.maternalGrandfatherRegistration = maternalGrandfatherRegistration;
    }

    public String getMaternalGrandmotherName() {
        return maternalGrandmotherName;
    }

    public void setMaternalGrandmotherName(String maternalGrandmotherName) {
        this.maternalGrandmotherName = maternalGrandmotherName;
    }

    public String getMaternalGrandmotherRegistration() {
        return maternalGrandmotherRegistration;
    }

    public void setMaternalGrandmotherRegistration(String maternalGrandmotherRegistration) {
        this.maternalGrandmotherRegistration = maternalGrandmotherRegistration;
    }
}