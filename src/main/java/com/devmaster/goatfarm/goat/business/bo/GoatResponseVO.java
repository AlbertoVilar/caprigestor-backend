package com.devmaster.goatfarm.goat.business.bo;

import com.devmaster.goatfarm.goat.enums.Category;
import com.devmaster.goatfarm.goat.enums.Gender;
import com.devmaster.goatfarm.goat.enums.GoatBreed;
import com.devmaster.goatfarm.goat.enums.GoatStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class GoatResponseVO {

    private String registrationNumber;
    private String name;
    private Gender gender;
    private GoatBreed breed;
    private String color;
    private LocalDate birthDate;
    private GoatStatus status;
    private String tod;
    private String toe;
    private Category category;

    private String fatherName;
    private String fatherRegistrationNumber;

    private String motherName;
    private String motherRegistrationNumber;

    private String userName;  // <-- Campo para o nome do usuário
    private Long farmId;

    private String farmName;

    public GoatResponseVO() {
    }

    public GoatResponseVO(String registrationNumber, String name, Gender gender, GoatBreed breed, 
                         String color, LocalDate birthDate, GoatStatus status, String tod, String toe, 
                         Category category, String fatherName, String fatherRegistrationNumber, 
                         String motherName, String motherRegistrationNumber, String userName, 
                         Long farmId, String farmName) {
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
        this.fatherName = fatherName;
        this.fatherRegistrationNumber = fatherRegistrationNumber;
        this.motherName = motherName;
        this.motherRegistrationNumber = motherRegistrationNumber;
        this.userName = userName;
        this.farmId = farmId;
        this.farmName = farmName;
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
    
    public String getFatherName() { return fatherName; }
    public void setFatherName(String fatherName) { this.fatherName = fatherName; }
    
    public String getFatherRegistrationNumber() { return fatherRegistrationNumber; }
    public void setFatherRegistrationNumber(String fatherRegistrationNumber) { this.fatherRegistrationNumber = fatherRegistrationNumber; }
    
    public String getMotherName() { return motherName; }
    public void setMotherName(String motherName) { this.motherName = motherName; }
    
    public String getMotherRegistrationNumber() { return motherRegistrationNumber; }
    public void setMotherRegistrationNumber(String motherRegistrationNumber) { this.motherRegistrationNumber = motherRegistrationNumber; }
    
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    
    public Long getFarmId() { return farmId; }
    public void setFarmId(Long farmId) { this.farmId = farmId; }
    
    public String getFarmName() { return farmName; }
    public void setFarmName(String farmName) { this.farmName = farmName; }

}
