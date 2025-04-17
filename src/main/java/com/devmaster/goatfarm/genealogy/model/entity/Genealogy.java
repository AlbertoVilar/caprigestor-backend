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
}
