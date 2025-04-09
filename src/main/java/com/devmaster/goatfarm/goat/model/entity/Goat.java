package com.devmaster.goatfarm.goat.model.entity;

import com.devmaster.goatfarm.farm.model.entity.GoatFarm;

import com.devmaster.goatfarm.goat.enums.Category;
import com.devmaster.goatfarm.goat.enums.Gender;
import com.devmaster.goatfarm.goat.enums.GoatBreed;
import com.devmaster.goatfarm.goat.enums.GoatStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "cabras")
public class Goat {

    @Id
    @Column(name = "numero_registro", unique = true, nullable = false, length = 20)
    private String numeroRegistro;

    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(name = "genero", nullable = false, length = 10)
    private Gender genero;

    @Enumerated(EnumType.STRING)
    @Column(name = "raca", length = 50)
    private GoatBreed breed;

    @Column(name = "cor", length = 30)
    private String cor;

    @Column(name = "data_nascimento", nullable = false)
    private LocalDate dataNascimento;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 15)
    private GoatStatus status;

    @Column(name = "TOD", length = 15)
    private String tod;

    @Column(name = "TOE", length = 15)
    private String toe;

    @Enumerated(EnumType.STRING)
    @Column(name = "categoria", length = 10)
    private Category categoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pai_id", referencedColumnName = "numero_registro")
    private Goat pai;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mae_id", referencedColumnName = "numero_registro")
    private Goat mae;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "capril_id")
    private GoatFarm capril;

    public Goat() {
    }

    public Goat(String numeroRegistro, String nome,
                Gender genero, GoatBreed breed,
                String cor, LocalDate dataNascimento,
                GoatStatus status, String tod, String toe,
                Category categoria, Goat pai, Goat mae, GoatFarm capril) {

        this.numeroRegistro = numeroRegistro;
        this.nome = nome;
        this.genero = genero;
        this.breed = breed;
        this.cor = cor;
        this.dataNascimento = dataNascimento;
        this.status = status;
        this.tod = tod;
        this.toe = toe;
        this.categoria = categoria;
        this.pai = pai;
        this.mae = mae;
        this.capril = capril;
    }

}
