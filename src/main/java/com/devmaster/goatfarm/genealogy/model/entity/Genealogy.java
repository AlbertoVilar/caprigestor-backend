package com.devmaster.goatfarm.genealogy.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "genealogias")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Genealogy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // Dados do animal
    @Column(name = "nome_animal")
    private String goatName;

    @Column(name = "registro_animal")
    private String goatRegistration;

    @Column(name = "criador")
    private String goatCreator;

    @Column(name = "proprietario")
    private String goatOwner;

    @Column(name = "raca")
    private String goatBreed;

    @Column(name = "pelagem")
    private String goatCoatColor;

    @Column(name = "situacao")
    private String goatStatus;

    @Column(name = "sexo")
    private String goatSex;

    @Column(name = "categoria")
    private String goatCategory;

    @Column(name = "tod")
    private String goatTOD;

    @Column(name = "toe")
    private String goatTOE;

    @Column(name = "data_nascimento")
    private String goatBirthDate;

    // Pais
    @Column(name = "pai_nome")
    private String fatherName;

    @Column(name = "pai_registro")
    private String fatherRegistration;

    @Column(name = "mae_nome")
    private String motherName;

    @Column(name = "mae_registro")
    private String motherRegistration;

    // Avós paternos
    @Column(name = "avo_paterno_nome")
    private String paternalGrandfatherName;

    @Column(name = "avo_paterno_registro")
    private String paternalGrandfatherRegistration;

    @Column(name = "avo_paterna_nome")
    private String paternalGrandmotherName;

    @Column(name = "avo_paterna_registro")
    private String paternalGrandmotherRegistration;

    // Avós maternos
    @Column(name = "avo_materno_nome")
    private String maternalGrandfatherName;

    @Column(name = "avo_materno_registro")
    private String maternalGrandfatherRegistration;

    @Column(name = "avo_materna_nome")
    private String maternalGrandmotherName;

    @Column(name = "avo_materna_registro")
    private String maternalGrandmotherRegistration;

    // Bisavós paternos
    @Column(name = "bisavo_paterno_1_nome")
    private String paternalGreatGrandfather1Name;

    @Column(name = "bisavo_paterno_1_registro")
    private String paternalGreatGrandfather1Registration;

    @Column(name = "bisavo_paterna_1_nome")
    private String paternalGreatGrandmother1Name;

    @Column(name = "bisavo_paterna_1_registro")
    private String paternalGreatGrandmother1Registration;

    @Column(name = "bisavo_paterno_2_nome")
    private String paternalGreatGrandfather2Name;

    @Column(name = "bisavo_paterno_2_registro")
    private String paternalGreatGrandfather2Registration;

    @Column(name = "bisavo_paterna_2_nome")
    private String paternalGreatGrandmother2Name;

    @Column(name = "bisavo_paterna_2_registro")
    private String paternalGreatGrandmother2Registration;

    // Bisavós maternos
    @Column(name = "bisavo_materno_1_nome")
    private String maternalGreatGrandfather1Name;

    @Column(name = "bisavo_materno_1_registro")
    private String maternalGreatGrandfather1Registration;

    @Column(name = "bisavo_materna_1_nome")
    private String maternalGreatGrandmother1Name;

    @Column(name = "bisavo_materna_1_registro")
    private String maternalGreatGrandmother1Registration;

    @Column(name = "bisavo_materno_2_nome")
    private String maternalGreatGrandfather2Name;

    @Column(name = "bisavo_materno_2_registro")
    private String maternalGreatGrandfather2Registration;

    @Column(name = "bisavo_materna_2_nome")
    private String maternalGreatGrandmother2Name;

    @Column(name = "bisavo_materna_2_registro")
    private String maternalGreatGrandmother2Registration;

    // Getters e setters manuais
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getGoatName() { return goatName; }
    public void setGoatName(String goatName) { this.goatName = goatName; }
    
    public String getGoatRegistration() { return goatRegistration; }
    public void setGoatRegistration(String goatRegistration) { this.goatRegistration = goatRegistration; }
    
    public String getGoatCreator() { return goatCreator; }
    public void setGoatCreator(String goatCreator) { this.goatCreator = goatCreator; }
    
    public String getGoatOwner() { return goatOwner; }
    public void setGoatOwner(String goatOwner) { this.goatOwner = goatOwner; }
    
    public String getGoatBreed() { return goatBreed; }
    public void setGoatBreed(String goatBreed) { this.goatBreed = goatBreed; }
    
    public String getGoatCoatColor() { return goatCoatColor; }
    public void setGoatCoatColor(String goatCoatColor) { this.goatCoatColor = goatCoatColor; }
    
    public String getGoatStatus() { return goatStatus; }
    public void setGoatStatus(String goatStatus) { this.goatStatus = goatStatus; }
    
    public String getGoatSex() { return goatSex; }
    public void setGoatSex(String goatSex) { this.goatSex = goatSex; }
    
    public String getGoatCategory() { return goatCategory; }
    public void setGoatCategory(String goatCategory) { this.goatCategory = goatCategory; }
    
    public String getGoatTOD() { return goatTOD; }
    public void setGoatTOD(String goatTOD) { this.goatTOD = goatTOD; }
    
    public String getGoatTOE() { return goatTOE; }
    public void setGoatTOE(String goatTOE) { this.goatTOE = goatTOE; }
    
    public String getGoatBirthDate() { return goatBirthDate; }
    public void setGoatBirthDate(String goatBirthDate) { this.goatBirthDate = goatBirthDate; }
    
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
}
