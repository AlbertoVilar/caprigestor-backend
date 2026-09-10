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
}
