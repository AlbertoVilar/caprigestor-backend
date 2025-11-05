package com.devmaster.goatfarm.genealogy.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "genealogia")
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

        @Column(name = "pai_nome")
    private String fatherName;

    @Column(name = "pai_registro")
    private String fatherRegistration;

    @Column(name = "mae_nome")
    private String motherName;

    @Column(name = "mae_registro")
    private String motherRegistration;

        @Column(name = "avo_paterno_nome")
    private String paternalGrandfatherName;

    @Column(name = "avo_paterno_registro")
    private String paternalGrandfatherRegistration;

    @Column(name = "avo_paterna_nome")
    private String paternalGrandmotherName;

    @Column(name = "avo_paterna_registro")
    private String paternalGrandmotherRegistration;

        @Column(name = "avo_materno_nome")
    private String maternalGrandfatherName;

    @Column(name = "avo_materno_registro")
    private String maternalGrandfatherRegistration;

    @Column(name = "avo_materna_nome")
    private String maternalGrandmotherName;

    @Column(name = "avo_materna_registro")
    private String maternalGrandmotherRegistration;

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

    public void setGoatName(String goatName) { this.goatName = goatName; }
    public void setGoatRegistration(String goatRegistration) { this.goatRegistration = goatRegistration; }
    public void setGoatCreator(String goatCreator) { this.goatCreator = goatCreator; }
    public void setGoatOwner(String goatOwner) { this.goatOwner = goatOwner; }
    public void setGoatBreed(String goatBreed) { this.goatBreed = goatBreed; }
    public void setGoatCoatColor(String goatCoatColor) { this.goatCoatColor = goatCoatColor; }
    public void setGoatStatus(String goatStatus) { this.goatStatus = goatStatus; }
    public void setGoatSex(String goatSex) { this.goatSex = goatSex; }
    public void setGoatCategory(String goatCategory) { this.goatCategory = goatCategory; }
    public void setGoatTOD(String goatTOD) { this.goatTOD = goatTOD; }
    public void setGoatTOE(String goatTOE) { this.goatTOE = goatTOE; }
    public void setGoatBirthDate(String goatBirthDate) { this.goatBirthDate = goatBirthDate; }
    
    public void setFatherName(String fatherName) { this.fatherName = fatherName; }
    public void setFatherRegistration(String fatherRegistration) { this.fatherRegistration = fatherRegistration; }
    public void setMotherName(String motherName) { this.motherName = motherName; }
    public void setMotherRegistration(String motherRegistration) { this.motherRegistration = motherRegistration; }
    
    public void setPaternalGrandfatherName(String paternalGrandfatherName) { this.paternalGrandfatherName = paternalGrandfatherName; }
    public void setPaternalGrandfatherRegistration(String paternalGrandfatherRegistration) { this.paternalGrandfatherRegistration = paternalGrandfatherRegistration; }
    public void setPaternalGrandmotherName(String paternalGrandmotherName) { this.paternalGrandmotherName = paternalGrandmotherName; }
    public void setPaternalGrandmotherRegistration(String paternalGrandmotherRegistration) { this.paternalGrandmotherRegistration = paternalGrandmotherRegistration; }
    
    public void setPaternalGreatGrandfather1Name(String paternalGreatGrandfather1Name) { this.paternalGreatGrandfather1Name = paternalGreatGrandfather1Name; }
    public void setPaternalGreatGrandfather1Registration(String paternalGreatGrandfather1Registration) { this.paternalGreatGrandfather1Registration = paternalGreatGrandfather1Registration; }
    public void setPaternalGreatGrandmother1Name(String paternalGreatGrandmother1Name) { this.paternalGreatGrandmother1Name = paternalGreatGrandmother1Name; }
    public void setPaternalGreatGrandmother1Registration(String paternalGreatGrandmother1Registration) { this.paternalGreatGrandmother1Registration = paternalGreatGrandmother1Registration; }
    public void setPaternalGreatGrandfather2Name(String paternalGreatGrandfather2Name) { this.paternalGreatGrandfather2Name = paternalGreatGrandfather2Name; }
    public void setPaternalGreatGrandfather2Registration(String paternalGreatGrandfather2Registration) { this.paternalGreatGrandfather2Registration = paternalGreatGrandfather2Registration; }
    public void setPaternalGreatGrandmother2Name(String paternalGreatGrandmother2Name) { this.paternalGreatGrandmother2Name = paternalGreatGrandmother2Name; }
    public void setPaternalGreatGrandmother2Registration(String paternalGreatGrandmother2Registration) { this.paternalGreatGrandmother2Registration = paternalGreatGrandmother2Registration; }
    
    public void setMaternalGrandfatherName(String maternalGrandfatherName) { this.maternalGrandfatherName = maternalGrandfatherName; }
    public void setMaternalGrandfatherRegistration(String maternalGrandfatherRegistration) { this.maternalGrandfatherRegistration = maternalGrandfatherRegistration; }
    public void setMaternalGreatGrandfather2Name(String maternalGreatGrandfather2Name) { this.maternalGreatGrandfather2Name = maternalGreatGrandfather2Name; }
    public void setMaternalGreatGrandfather2Registration(String maternalGreatGrandfather2Registration) { this.maternalGreatGrandfather2Registration = maternalGreatGrandfather2Registration; }
    public void setMaternalGreatGrandmother2Name(String maternalGreatGrandmother2Name) { this.maternalGreatGrandmother2Name = maternalGreatGrandmother2Name; }
    public void setMaternalGreatGrandmother2Registration(String maternalGreatGrandmother2Registration) { this.maternalGreatGrandmother2Registration = maternalGreatGrandmother2Registration; }
    public void setMaternalGreatGrandfather1Name(String maternalGreatGrandfather1Name) { this.maternalGreatGrandfather1Name = maternalGreatGrandfather1Name; }
    public void setMaternalGreatGrandfather1Registration(String maternalGreatGrandfather1Registration) { this.maternalGreatGrandfather1Registration = maternalGreatGrandfather1Registration; }
    public void setMaternalGreatGrandmother1Name(String maternalGreatGrandmother1Name) { this.maternalGreatGrandmother1Name = maternalGreatGrandmother1Name; }
    public void setMaternalGreatGrandmother1Registration(String maternalGreatGrandmother1Registration) { this.maternalGreatGrandmother1Registration = maternalGreatGrandmother1Registration; }
    public void setMaternalGrandmotherName(String maternalGrandmotherName) { this.maternalGrandmotherName = maternalGrandmotherName; }
    public void setMaternalGrandmotherRegistration(String maternalGrandmotherRegistration) { this.maternalGrandmotherRegistration = maternalGrandmotherRegistration; }
}

