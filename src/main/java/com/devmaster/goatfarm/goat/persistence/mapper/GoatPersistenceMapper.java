package com.devmaster.goatfarm.goat.persistence.mapper;

import com.devmaster.goatfarm.authority.persistence.entity.User;
import com.devmaster.goatfarm.farm.persistence.entity.GoatFarm;
import com.devmaster.goatfarm.goat.domain.Goat;
import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goat.domain.RegistrationIdentity;
import com.devmaster.goatfarm.goat.persistence.entity.GoatEntity;
import org.springframework.stereotype.Component;

/** Maps the framework-free Goat aggregate to the transitional JPA model. */
@Component
public class GoatPersistenceMapper {

    public GoatEntity toNewEntity(Goat goat, GoatFarm farm, User user) {
        GoatEntity entity = new GoatEntity();
        entity.setFarm(farm);
        entity.setUser(user);
        return toEntity(goat, entity);
    }

    public GoatEntity toEntity(Goat goat, GoatEntity entity) {
        entity.setRegistrationNumber(goat.registrationNumber());
        entity.setName(goat.name());
        entity.setGender(goat.gender());
        entity.setBreed(goat.breed());
        entity.setColor(goat.color());
        entity.setBirthDate(goat.birthDate());
        entity.setStatus(goat.status());
        entity.setExitType(goat.exitType());
        entity.setExitDate(goat.exitDate());
        entity.setExitNotes(goat.exitNotes());
        entity.setTod(goat.tod());
        entity.setToe(goat.toe());
        entity.setCategory(goat.category());
        entity.setFatherRegistrationNumberSnapshot(localParentRegistration(goat.father()));
        entity.setMotherRegistrationNumberSnapshot(localParentRegistration(goat.mother()));
        entity.setFatherTechnicalId(goat.father() != null && goat.father().id() != null
                ? goat.father().id().value() : null);
        entity.setMotherTechnicalId(goat.mother() != null && goat.mother().id() != null
                ? goat.mother().id().value() : null);
        entity.setExternalFatherRegistrationNumber(externalRegistration(goat.father()));
        entity.setExternalMotherRegistrationNumber(externalRegistration(goat.mother()));
        return entity;
    }

    public Goat toDomain(GoatEntity entity) {
        return Goat.rehydrate(
                GoatId.of(entity.getTechnicalId()),
                RegistrationIdentity.of(entity.getRegistrationNumber(), entity.getTod(), entity.getToe()),
                entity.getName(), entity.getGender(), entity.getBreed(), entity.getColor(),
                entity.getBirthDate(), entity.getStatus(), entity.getExitType(), entity.getExitDate(),
                entity.getExitNotes(), entity.getCategory(), toParent(entity.getTechnicalFather(),
                        entity.getFatherTechnicalId(), entity.getExternalFatherRegistrationNumber()),
                toParent(entity.getTechnicalMother(), entity.getMotherTechnicalId(), entity.getExternalMotherRegistrationNumber()),
                entity.getFarm() == null ? null : entity.getFarm().getId(),
                entity.getUser() == null ? null : entity.getUser().getId(),
                entity.getFarm() == null ? null : entity.getFarm().getName(),
                entity.getUser() == null ? null : entity.getUser().getName()
        );
    }

    private Goat.ParentReference toParent(
            GoatEntity technicalLocal,
            Long technicalId,
            String externalRegistration
    ) {
        GoatEntity local = technicalLocal;
        if (local != null) {
            return Goat.ParentReference.local(
                    GoatId.of(local.getTechnicalId() != null ? local.getTechnicalId() : technicalId),
                    local.getRegistrationNumber(), local.getName());
        }
        if (externalRegistration != null && !externalRegistration.isBlank()) {
            return Goat.ParentReference.external(externalRegistration);
        }
        if (technicalId != null) {
            return Goat.ParentReference.local(GoatId.of(technicalId), null, null);
        }
        return null;
    }

    private String externalRegistration(Goat.ParentReference parent) {
        if (parent == null || parent.isLocal()) {
            return null;
        }
        return parent.registrationNumber();
    }

    private String localParentRegistration(Goat.ParentReference parent) {
        return parent != null && parent.isLocal() ? parent.registrationNumber() : null;
    }
}
