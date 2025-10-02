package com.devmaster.goatfarm.goat.model.entity;

import com.devmaster.goatfarm.farm.model.entity.GoatFarm;

import com.devmaster.goatfarm.goat.enums.Category;
import com.devmaster.goatfarm.goat.enums.Gender;
import com.devmaster.goatfarm.goat.enums.GoatBreed;
import com.devmaster.goatfarm.goat.enums.GoatStatus;
import com.devmaster.goatfarm.authority.model.entity.User;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
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
        @Column(name = "raca", length = 50)
        private GoatBreed breed;

        @Column(name = "cor", length = 30)
        private String color;

        @Column(name = "data_nascimento", nullable = false)
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
        @JoinColumn(name = "pai_num_registro", referencedColumnName = "num_registro")
        private Goat father;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "mae_num_registro", referencedColumnName = "num_registro")
        private Goat mother;

        @ManyToOne
    @JoinColumn(name = "usuario_id")
    @JsonBackReference
    private User user;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "capril_id")
        private GoatFarm farm;

        public Goat() {
    }

    // Getters e setters manuais
    public String getRegistrationNumber() { return registrationNumber; }
    public void setRegistrationNumber(String registrationNumber) { this.registrationNumber = registrationNumber; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public Gender getGender() { return gender; }
    public void setGender(Gender gender) { this.gender = gender; }
    
    public GoatBreed getBreed() { return breed; }
    public void setBreed(GoatBreed breed) { this.breed = breed; }
    
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    
    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }
    
    public GoatStatus getStatus() { return status; }
    public void setStatus(GoatStatus status) { this.status = status; }
    
    public String getTod() { return tod; }
    public void setTod(String tod) { this.tod = tod; }
    
    public String getToe() { return toe; }
    public void setToe(String toe) { this.toe = toe; }
    
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    
    public Goat getFather() { return father; }
    public void setFather(Goat father) { this.father = father; }
    
    public Goat getMother() { return mother; }
    public void setMother(Goat mother) { this.mother = mother; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public GoatFarm getFarm() { return farm; }
    public void setFarm(GoatFarm farm) { this.farm = farm; }

    @Override
    public String toString() {
        return "Goat{" +
                "registrationNumber='" + registrationNumber + '\'' +
                ", name='" + name + '\'' +
                '}';
    }

}
