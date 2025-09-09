package com.devmaster.goatfarm.goat.api.dto;

import com.devmaster.goatfarm.goat.enums.Category;
import com.devmaster.goatfarm.goat.enums.Gender;
import com.devmaster.goatfarm.goat.enums.GoatBreed;
import com.devmaster.goatfarm.goat.enums.GoatStatus;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class GoatRequestDTO {

    @NotBlank(message = "O número de registro não pode estar em branco.")
    @Size(min = 10, max = 12, message = "O registro deve ter entre {min} e {max} caracteres.")
    private String registrationNumber;

    @NotBlank(message = "O nome não pode estar em branco.")
    @Size(min = 3, max = 60, message = "O nome deve ter entre {min} e {max} caracteres.")
    private String name;

    @NotNull(message = "O sexo não pode estar em branco.")
    private Gender gender;

    @NotNull(message = "A raça não pode estar em branco.")
    private GoatBreed breed;

    @NotBlank(message = "A cor não pode estar em branco.")
    private String color;

    @NotNull(message = "A data de nascimento não pode estar em branco.")
    private LocalDate birthDate;

    @NotNull(message = "O status não pode estar em branco.")
    private GoatStatus status;

    @NotBlank(message = "A TOD não pode estar em branco.")
    @Size(min = 5, max = 5, message = "A TOD deve ter {max} caracteres.")
    private String tod;

    @NotBlank(message = "A TOE não pode estar em branco.")
    @Size(min = 5, max = 7, message = "A TOE deve ter entre {min} e {max} caracteres.")
    private String toe;

    @NotNull(message = "A categoria não pode estar vazia.")
    private Category category;

    @Size(min = 10, max = 12, message = "O número de registro do pai deve ter entre {min} e {max} caracteres.")
    private String fatherRegistrationNumber;

    @Size(min = 10, max = 12, message = "O número de registro da mãe deve ter entre {min} e {max} caracteres.")
    private String motherRegistrationNumber;

    @NotNull(message = "O ID da fazenda é obrigatório.")
    private Long farmId;

    @NotNull(message = "O ID do usuário é obrigatório.")
    private Long userId;

    public GoatRequestDTO() {
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
    
    public String getFatherRegistrationNumber() { return fatherRegistrationNumber; }
    public void setFatherRegistrationNumber(String fatherRegistrationNumber) { this.fatherRegistrationNumber = fatherRegistrationNumber; }
    
    public String getMotherRegistrationNumber() { return motherRegistrationNumber; }
    public void setMotherRegistrationNumber(String motherRegistrationNumber) { this.motherRegistrationNumber = motherRegistrationNumber; }
    
    public Long getFarmId() { return farmId; }
    public void setFarmId(Long farmId) { this.farmId = farmId; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}
