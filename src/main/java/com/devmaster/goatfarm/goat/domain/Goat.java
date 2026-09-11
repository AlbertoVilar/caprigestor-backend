package com.devmaster.goatfarm.goat.domain;

import com.devmaster.goatfarm.goat.enums.Category;
import com.devmaster.goatfarm.goat.enums.Gender;
import com.devmaster.goatfarm.goat.enums.GoatBreed;
import com.devmaster.goatfarm.goat.enums.GoatExitType;
import com.devmaster.goatfarm.goat.enums.GoatStatus;

import java.time.LocalDate;

/**
 * Framework-free Goat aggregate used by the application core.
 *
 * <p>Persistence concerns (JPA relations, lazy loading and database column
 * names) are intentionally absent. A parent is represented by a technical id
 * plus display data when available; an ABCC parent remains an external RG.</p>
 */
public final class Goat {

    private final GoatId id;
    private RegistrationIdentity registrationIdentity;
    private String name;
    private Gender gender;
    private GoatBreed breed;
    private String color;
    private LocalDate birthDate;
    private GoatStatus status;
    private GoatExitType exitType;
    private LocalDate exitDate;
    private String exitNotes;
    private Category category;
    private ParentReference father;
    private ParentReference mother;
    private final Long farmId;
    private final Long userId;
    private final String farmName;
    private final String userName;

    private Goat(
            GoatId id,
            RegistrationIdentity registrationIdentity,
            String name,
            Gender gender,
            GoatBreed breed,
            String color,
            LocalDate birthDate,
            GoatStatus status,
            GoatExitType exitType,
            LocalDate exitDate,
            String exitNotes,
            Category category,
            ParentReference father,
            ParentReference mother,
            Long farmId,
            Long userId,
            String farmName,
            String userName
    ) {
        this.id = id;
        this.registrationIdentity = registrationIdentity;
        this.name = name;
        this.gender = gender;
        this.breed = breed;
        this.color = color;
        this.birthDate = birthDate;
        this.status = status;
        this.exitType = exitType;
        this.exitDate = exitDate;
        this.exitNotes = exitNotes;
        this.category = category;
        this.father = father;
        this.mother = mother;
        this.farmId = farmId;
        this.userId = userId;
        this.farmName = farmName;
        this.userName = userName;
    }

    public static Goat register(
            RegistrationIdentity registrationIdentity,
            String name,
            Gender gender,
            GoatBreed breed,
            String color,
            LocalDate birthDate,
            GoatStatus status,
            Category category,
            ParentReference father,
            ParentReference mother,
            Long farmId,
            Long userId
    ) {
        return new Goat(null, registrationIdentity, name, gender, breed, color, birthDate, status,
                null, null, null, category, father, mother, farmId, userId, null, null);
    }

    public static Goat rehydrate(
            GoatId id,
            RegistrationIdentity registrationIdentity,
            String name,
            Gender gender,
            GoatBreed breed,
            String color,
            LocalDate birthDate,
            GoatStatus status,
            GoatExitType exitType,
            LocalDate exitDate,
            String exitNotes,
            Category category,
            ParentReference father,
            ParentReference mother,
            Long farmId,
            Long userId,
            String farmName,
            String userName
    ) {
        return new Goat(id, registrationIdentity, name, gender, breed, color, birthDate, status,
                exitType, exitDate, exitNotes, category, father, mother, farmId, userId, farmName, userName);
    }

    public void updateProfile(
            String name,
            Gender gender,
            GoatBreed breed,
            String color,
            LocalDate birthDate,
            GoatStatus status,
            Category category,
            ParentReference father,
            ParentReference mother
    ) {
        this.name = name;
        this.gender = gender;
        this.breed = breed;
        this.color = color;
        this.birthDate = birthDate;
        this.status = status;
        this.category = category;
        this.father = father;
        this.mother = mother;
    }

    public void markExit(GoatExitType exitType, LocalDate exitDate, String exitNotes, GoatStatus resultingStatus) {
        this.exitType = exitType;
        this.exitDate = exitDate;
        this.exitNotes = exitNotes;
        this.status = resultingStatus;
    }

    /** Activates a kid when the weaning lifecycle transition is completed. */
    public void activateAfterWeaning() {
        this.status = GoatStatus.ATIVO;
    }

    public GoatId id() { return id; }
    public RegistrationIdentity registrationIdentity() { return registrationIdentity; }
    public String registrationNumber() { return registrationIdentity.registrationNumber(); }
    public String tod() { return registrationIdentity.tod(); }
    public String toe() { return registrationIdentity.toe(); }
    public String name() { return name; }
    public Gender gender() { return gender; }
    public GoatBreed breed() { return breed; }
    public String color() { return color; }
    public LocalDate birthDate() { return birthDate; }
    public GoatStatus status() { return status; }
    public GoatExitType exitType() { return exitType; }
    public LocalDate exitDate() { return exitDate; }
    public String exitNotes() { return exitNotes; }
    public Category category() { return category; }
    public ParentReference father() { return father; }
    public ParentReference mother() { return mother; }
    public Long farmId() { return farmId; }
    public Long userId() { return userId; }
    public String farmName() { return farmName; }
    public String userName() { return userName; }

    /** Persisted aggregates are equal only through their immutable technical id. */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Goat that)) {
            return false;
        }
        return id != null && that.id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id == null ? System.identityHashCode(this) : id.hashCode();
    }

    public record ParentReference(GoatId id, String registrationNumber, String name) {
        public static ParentReference local(GoatId id, String registrationNumber, String name) {
            return new ParentReference(id, registrationNumber, name);
        }

        public static ParentReference external(String registrationNumber) {
            return new ParentReference(null, registrationNumber, null);
        }

        public boolean isLocal() {
            return id != null;
        }
    }
}
