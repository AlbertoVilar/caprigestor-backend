package com.devmaster.goatfarm.goat.persistence.adapter;

import com.devmaster.goatfarm.goat.application.ports.out.GoatPageQuery;
import com.devmaster.goatfarm.goat.domain.Goat;
import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goat.domain.RegistrationIdentity;
import com.devmaster.goatfarm.goat.enums.Category;
import com.devmaster.goatfarm.goat.enums.Gender;
import com.devmaster.goatfarm.goat.enums.GoatBreed;
import com.devmaster.goatfarm.goat.enums.GoatStatus;
import com.devmaster.goatfarm.goat.persistence.entity.GoatEntity;
import com.devmaster.goatfarm.goat.persistence.mapper.GoatPersistenceMapper;
import com.devmaster.goatfarm.goat.persistence.repository.GoatBreedCountProjection;
import com.devmaster.goatfarm.goat.persistence.repository.GoatRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GoatPersistenceAdapterTest {

    @Mock private GoatRepository repository;
    @Mock private GoatPersistenceMapper mapper;

    private GoatPersistenceAdapter adapter;
    private GoatEntity entity;
    private Goat domain;

    @BeforeEach
    void setUp() {
        adapter = new GoatPersistenceAdapter(repository, mapper);
        entity = entity(10L, "RG-10");
        domain = Goat.rehydrate(new GoatId(10L),
                RegistrationIdentity.of("RG-10", "10", "10"),
                "Matriz", Gender.FEMEA, GoatBreed.SAANEN, "Branca",
                LocalDate.of(2024, 1, 1), GoatStatus.ATIVO, null, null, null,
                Category.PA, null, null, 1L, 2L, "Capril", "Alberto");
        lenient().when(mapper.toDomain(any(GoatEntity.class))).thenReturn(domain);
    }

    @Test
    void savesDomainAggregateUsingTechnicalIdentityWhenPresent() {
        when(repository.findByTechnicalId(10L)).thenReturn(Optional.of(entity));
        when(mapper.toEntity(domain, entity, null, null)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);

        assertThat(adapter.save(domain)).isSameAs(domain);
        verify(mapper).toEntity(domain, entity, null, null);
    }

    @Test
    void resolvesCleanDomainAndReferenceQueries() {
        Page<GoatEntity> page = new PageImpl<>(List.of(entity), PageRequest.of(0, 2), 1);
        when(repository.findByTechnicalId(10L)).thenReturn(Optional.of(entity));
        when(repository.findByTechnicalIdAndFarmId(10L, 1L)).thenReturn(Optional.of(entity));
        when(repository.findByRegistrationNumber("RG-10")).thenReturn(Optional.of(entity));
        when(repository.findByRegistrationNumberAndFarmId("RG-10", 1L)).thenReturn(Optional.of(entity));
        when(repository.findAllByFarmId(anyLong(), any())).thenReturn(page);
        when(repository.findOffspringByParentTechnicalId(1L, 10L)).thenReturn(List.of(entity));

        assertThat(adapter.findById(new GoatId(10L))).contains(domain);
        assertThat(adapter.findByIdAndFarmId(new GoatId(10L), 1L)).contains(domain);
        assertThat(adapter.findDomainByRegistrationNumber("RG-10")).contains(domain);
        assertThat(adapter.findByRegistrationNumberAndFarmId("RG-10", 1L)).contains(domain);
        assertThat(adapter.findReferenceByRegistrationNumber("RG-10")).get()
                .satisfies(reference -> assertThat(reference.id()).isEqualTo(new GoatId(10L)));
        assertThat(adapter.findReferenceByRegistrationNumberAndFarmId("RG-10", 1L)).isPresent();
        assertThat(adapter.findReferenceByTechnicalIdAndFarmId(new GoatId(10L), 1L)).isPresent();
        assertThat(adapter.findAllByFarmId(1L, new GoatPageQuery(0, 2, "")).content()).containsExactly(domain);
        assertThat(adapter.findOffspringByParentId(1L, new GoatId(10L))).containsExactly(domain);
        assertThat(adapter.findOffspringByParentId(1L, null)).isEmpty();
    }

    @Test
    void exposesValidationSummaryAndTechnicalDelete() {
        when(repository.findByRegistrationNumberAndFarmId("RG-10", 1L)).thenReturn(Optional.of(entity));
        when(repository.findByRegistrationNumberAndFarmIdWithTechnicalFamilyGraph("RG-10", 1L))
                .thenReturn(Optional.of(entity));
        when(repository.existsByRegistrationNumber("RG-10")).thenReturn(true);
        when(repository.findByTechnicalId(10L)).thenReturn(Optional.of(entity));
        when(repository.countByFarmId(1L)).thenReturn(20L);
        when(repository.countByFarmIdAndGender(1L, Gender.MACHO)).thenReturn(4L);
        when(repository.countByFarmIdAndGender(1L, Gender.FEMEA)).thenReturn(16L);
        when(repository.countByFarmIdAndStatus(1L, GoatStatus.ATIVO)).thenReturn(17L);
        when(repository.countByFarmIdAndStatus(1L, GoatStatus.INATIVO)).thenReturn(1L);
        when(repository.countByFarmIdAndStatus(1L, GoatStatus.VENDIDO)).thenReturn(1L);
        when(repository.countByFarmIdAndStatus(1L, GoatStatus.FALECIDO)).thenReturn(1L);
        when(repository.countByFarmIdWithoutBreed(1L)).thenReturn(2L);
        GoatBreedCountProjection projection = new GoatBreedCountProjection() {
            public GoatBreed getBreed() { return GoatBreed.SAANEN; }
            public long getTotal() { return 8L; }
        };
        when(repository.countBreedsByFarmId(1L)).thenReturn(List.of(projection));

        assertThat(adapter.findForValidation("RG-10", 1L)).get()
                .satisfies(snapshot -> assertThat(snapshot.status()).isEqualTo(GoatStatus.ATIVO));
        assertThat(adapter.findGenealogyByRegistrationNumberAndFarmId("RG-10", 1L)).isPresent();
        assertThat(adapter.getHerdSummary(1L).total()).isEqualTo(20L);
        assertThat(adapter.existsByRegistrationNumber("RG-10")).isTrue();

        adapter.deleteById(new GoatId(10L));
        verify(repository).delete(entity);
    }

    private GoatEntity entity(Long technicalId, String registration) {
        GoatEntity result = new GoatEntity();
        result.setTechnicalId(technicalId);
        result.setRegistrationNumber(registration);
        result.setName("Matriz");
        result.setGender(Gender.FEMEA);
        result.setBreed(GoatBreed.SAANEN);
        result.setBirthDate(LocalDate.of(2024, 1, 1));
        result.setStatus(GoatStatus.ATIVO);
        result.setCategory(Category.PA);
        return result;
    }
}
