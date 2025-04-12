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
        @Column(name = "num_registro", unique = true, nullable = false, length = 20)
        private String registrationNumber;

        @Column(name = "nome", nullable = false, length = 100)
        private String name;

        @Enumerated(EnumType.STRING)
        @Column(name = "sexo", nullable = false, length = 10)
        private Gender gender;

        @Enumerated(EnumType.STRING)
        @Column(name = "raça", length = 50)
        private GoatBreed breed;

        @Column(name = "cor", length = 30)
        private String color;

        @Column(name = "nascimento", nullable = false)
        private LocalDate birthDate;

        @Enumerated(EnumType.STRING)
        @Column(name = "status", nullable = false, length = 15)
        private GoatStatus status;

        @Column(name = "tod", length = 15)
        private String tod;

        @Column(name = "toe", length = 15)
        private String toe;

        @Enumerated(EnumType.STRING)
        @Column(name = "categoria", length = 10)
        private Category category;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "pai_id", referencedColumnName = "num_registro")
        private Goat father;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "mae_id", referencedColumnName = "num_registro")
        private Goat mother;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "capril_id")
        private GoatFarm farm;

        public Goat() {
        }

        public Goat(String registrationNumber, String name,
                    Gender gender, GoatBreed breed,
                    String color, LocalDate birthDate,
                    GoatStatus status, String tod, String toe,
                    Category category, Goat father, Goat mother, GoatFarm farm) {

            this.registrationNumber = registrationNumber;
            this.name = name;
            this.gender = gender;
            this.breed = breed;
            this.color = color;
            this.birthDate = birthDate;
            this.status = status;
            this.tod = tod;
            this.toe = toe;
            this.category = category;
            this.father = father;
            this.mother = mother;
            this.farm = farm;

    }

    @Override
    public String toString() {
        return "Goat{" +
                "registrationNumber='" + registrationNumber + '\'' +
                ", name='" + name + '\'' +
                '}';
    }

}
