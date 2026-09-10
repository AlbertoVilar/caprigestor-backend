package com.devmaster.goatfarm.goat.persistence.mapper;

import com.devmaster.goatfarm.authority.persistence.entity.User;
import com.devmaster.goatfarm.farm.persistence.entity.GoatFarm;
import com.devmaster.goatfarm.goat.domain.Goat;
import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goat.domain.RegistrationIdentity;
import com.devmaster.goatfarm.goat.enums.Category;
import com.devmaster.goatfarm.goat.enums.Gender;
import com.devmaster.goatfarm.goat.enums.GoatBreed;
import com.devmaster.goatfarm.goat.enums.GoatStatus;
import com.devmaster.goatfarm.goat.persistence.entity.GoatEntity;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GoatPersistenceMapperTest {

    private final GoatPersistenceMapper mapper = new GoatPersistenceMapper();

    @Test
    void persistedAggregatesUseTechnicalIdForEquality() {
        Goat first = Goat.rehydrate(new GoatId(3), RegistrationIdentity.of("A", null, null),
                "Primeiro", Gender.FEMEA, null, null, LocalDate.of(2024, 1, 1), GoatStatus.ATIVO,
                null, null, null, Category.PA, null, null, 1L, 1L, null, null);
        Goat sameIdentity = Goat.rehydrate(new GoatId(3), RegistrationIdentity.of("B", null, null),
                "Segundo", Gender.MACHO, null, null, LocalDate.of(2025, 1, 1), GoatStatus.ATIVO,
                null, null, null, Category.PA, null, null, 2L, 2L, null, null);

        assertThat(first).isEqualTo(sameIdentity);
        assertThat(first).isEqualTo(first);
        assertThat(first).isNotEqualTo("not a goat");
        assertThat(first.hashCode()).isEqualTo(new GoatId(3).hashCode());
        Goat unsaved = Goat.register(RegistrationIdentity.of("UNSAVED", null, null), "Novo", Gender.FEMEA,
                null, null, LocalDate.of(2025, 1, 1), GoatStatus.ATIVO, Category.PA, null, null, 1L, 1L);
        assertThat(unsaved.hashCode()).isNotEqualTo(0);
    }

    @Test
    void mapsTechnicalIdentityAndExternalParentWithoutJPAInDomain() {
        GoatEntity entity = new GoatEntity();
        entity.setTechnicalId(42L);
        entity.setRegistrationNumber(" 16432 2002 ");
        entity.setTod("16432"); entity.setToe("2002");
        entity.setName("Matriz"); entity.setGender(Gender.FEMEA); entity.setBreed(GoatBreed.SAANEN);
        entity.setBirthDate(LocalDate.of(2024, 1, 1)); entity.setStatus(GoatStatus.ATIVO); entity.setCategory(Category.PA);
        entity.setExternalFatherRegistrationNumber("ABCC-PAI-1");
        GoatFarm farm = new GoatFarm(); farm.setId(7L); farm.setName("Capril"); entity.setFarm(farm);
        User user = new User(); user.setId(8L); user.setName("Alberto"); entity.setUser(user);

        Goat domain = mapper.toDomain(entity);

        assertThat(domain.id()).isEqualTo(new GoatId(42));
        assertThat(domain.registrationNumber()).isEqualTo("164322002");
        assertThat(domain.father().registrationNumber()).isEqualTo("ABCC-PAI-1");
        assertThat(domain.father().isLocal()).isFalse();
        assertThat(domain.farmId()).isEqualTo(7L);
    }

    @Test
    void mapsLocalParentTechnicalShadowAndProfileBackToEntity() {
        Goat parent = Goat.rehydrate(new GoatId(9), RegistrationIdentity.of("P1", "1", "1"),
                "Pai", Gender.MACHO, null, null, LocalDate.of(2020, 1, 1), GoatStatus.ATIVO,
                null, null, null, Category.PA, null, null, 7L, 8L, null, null);
        Goat child = Goat.register(RegistrationIdentity.of("C1", "2", "2"), "Cria", Gender.FEMEA,
                GoatBreed.SAANEN, "Branca", LocalDate.of(2025, 1, 1), GoatStatus.ATIVO, Category.PA,
                Goat.ParentReference.local(parent.id(), parent.registrationNumber(), parent.name()), null, 7L, 8L);

        GoatEntity entity = mapper.toNewEntity(child, null, null, null, null);
        mapper.toEntity(child, entity, null, null);

        assertThat(entity.getRegistrationNumber()).isEqualTo("C1");
        assertThat(entity.getFatherTechnicalId()).isEqualTo(9L);
        assertThat(entity.getExternalFatherRegistrationNumber()).isNull();
    }

    @Test
    void mapsLocalParentFromRelationOrTechnicalShadowAndRejectsInvalidValueObjects() {
        GoatEntity localParent = new GoatEntity();
        localParent.setRegistrationNumber("PARENT");
        localParent.setName("Pai");
        localParent.setTechnicalId(null);
        GoatEntity child = new GoatEntity();
        child.setTechnicalId(20L);
        child.setRegistrationNumber("CHILD");
        child.setName("Cria");
        child.setGender(Gender.FEMEA);
        child.setBirthDate(LocalDate.of(2025, 1, 1));
        child.setStatus(GoatStatus.ATIVO);
        child.setFather(localParent);
        child.setFatherTechnicalId(9L);
        Goat mapped = mapper.toDomain(child);

        assertThat(mapped.father().id()).isEqualTo(new GoatId(9L));
        assertThat(mapped.father().registrationNumber()).isEqualTo("PARENT");

        GoatEntity shadowOnly = new GoatEntity();
        shadowOnly.setTechnicalId(21L);
        shadowOnly.setRegistrationNumber("SHADOW");
        shadowOnly.setName("Sombra");
        shadowOnly.setGender(Gender.FEMEA);
        shadowOnly.setBirthDate(LocalDate.of(2025, 1, 1));
        shadowOnly.setStatus(GoatStatus.ATIVO);
        shadowOnly.setMotherTechnicalId(8L);
        assertThat(mapper.toDomain(shadowOnly).mother().id()).isEqualTo(new GoatId(8L));

        assertThatThrownBy(() -> new GoatId(0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> RegistrationIdentity.of(" ", null, null)).isInstanceOf(IllegalArgumentException.class);
    }
}
