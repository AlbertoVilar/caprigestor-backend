package com.devmaster.goatfarm.goat.model.entity;

import com.devmaster.goatfarm.farm.model.entity.GoatFarm;

import com.devmaster.goatfarm.goat.enums.Category;
import com.devmaster.goatfarm.goat.enums.Gender;
import com.devmaster.goatfarm.goat.enums.GoatBreed;
import com.devmaster.goatfarm.goat.enums.GoatStatus;
import com.devmaster.goatfarm.owner.model.entity.Owner;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
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
        @Column(name = "raca", length = 50)
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

        @ManyToOne
        @JoinColumn(name = "owner_id")
        private Owner owner;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "capril_id")
        private GoatFarm farm;

        public Goat() {
        }



    @Override
    public String toString() {
        return "Goat{" +
                "registrationNumber='" + registrationNumber + '\'' +
                ", name='" + name + '\'' +
                '}';
    }

}
