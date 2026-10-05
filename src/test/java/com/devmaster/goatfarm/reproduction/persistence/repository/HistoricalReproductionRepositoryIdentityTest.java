package com.devmaster.goatfarm.reproduction.persistence.repository;

import com.devmaster.goatfarm.reproduction.enums.BreedingType;
import com.devmaster.goatfarm.reproduction.enums.PregnancyStatus;
import com.devmaster.goatfarm.reproduction.enums.ReproductiveEventType;
import com.devmaster.goatfarm.reproduction.persistence.entity.PregnancyEntity;
import com.devmaster.goatfarm.reproduction.persistence.entity.ReproductiveEventEntity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class HistoricalReproductionRepositoryIdentityTest {

    @Autowired
    private PregnancyRepository pregnancyRepository;

    @Autowired
    private ReproductiveEventRepository reproductiveEventRepository;

    @AfterEach
    void tearDown() {
        reproductiveEventRepository.deleteAll();
        pregnancyRepository.deleteAll();
    }

    @Test
    @DisplayName("Pregnancy query: Case A - row with matching non-null goatTechnicalId is returned")
    void pregnancy_caseA_matchingTechnicalIdReturned() {
        PregnancyEntity pA = pregnancyRepository.save(PregnancyEntity.builder()
                .farmId(1L)
                .goatTechnicalId(10L)
                .goatId("RG-10")
                .status(PregnancyStatus.ACTIVE)
                .breedingDate(LocalDate.of(2026, 1, 1))
                .build());

        List<PregnancyEntity> result = pregnancyRepository.findHistoricalCandidates(10L, "RG-OTHER");
        assertThat(result).extracting(PregnancyEntity::getId).containsExactly(pA.getId());
    }

    @Test
    @DisplayName("Pregnancy query: Case B - legacy row with goatTechnicalId == null and matching RG is returned")
    void pregnancy_caseB_legacyRowWithNullTechnicalIdAndMatchingRgReturned() {
        PregnancyEntity legacyP = pregnancyRepository.save(PregnancyEntity.builder()
                .farmId(1L)
                .goatTechnicalId(null)
                .goatId("RG-LEGACY")
                .status(PregnancyStatus.CLOSED)
                .breedingDate(LocalDate.of(2024, 1, 1))
                .build());

        List<PregnancyEntity> result = pregnancyRepository.findHistoricalCandidates(10L, "RG-LEGACY");
        assertThat(result).extracting(PregnancyEntity::getId).containsExactly(legacyP.getId());
    }

    @Test
    @DisplayName("Pregnancy query: Case C - row with DIFFERENT non-null goatTechnicalId and matching RG is NOT returned")
    void pregnancy_caseC_differentNonNullTechnicalIdWithMatchingRgNotReturned() {
        pregnancyRepository.save(PregnancyEntity.builder()
                .farmId(1L)
                .goatTechnicalId(99L)
                .goatId("RG-SHARED")
                .status(PregnancyStatus.ACTIVE)
                .breedingDate(LocalDate.of(2026, 1, 1))
                .build());

        List<PregnancyEntity> result = pregnancyRepository.findHistoricalCandidates(10L, "RG-SHARED");
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("ReproductiveEvent query: Case A - row with matching non-null goatTechnicalId is returned")
    void reproductiveEvent_caseA_matchingTechnicalIdReturned() {
        ReproductiveEventEntity eA = reproductiveEventRepository.save(ReproductiveEventEntity.builder()
                .farmId(1L)
                .goatTechnicalId(10L)
                .goatId("RG-10")
                .eventType(ReproductiveEventType.COVERAGE)
                .eventDate(LocalDate.of(2026, 1, 1))
                .breedingType(BreedingType.NATURAL)
                .build());

        List<ReproductiveEventEntity> result = reproductiveEventRepository.findHistoricalCandidates(10L, "RG-OTHER");
        assertThat(result).extracting(ReproductiveEventEntity::getId).containsExactly(eA.getId());
    }

    @Test
    @DisplayName("ReproductiveEvent query: Case B - legacy row with goatTechnicalId == null and matching RG is returned")
    void reproductiveEvent_caseB_legacyRowWithNullTechnicalIdAndMatchingRgReturned() {
        ReproductiveEventEntity legacyE = reproductiveEventRepository.save(ReproductiveEventEntity.builder()
                .farmId(1L)
                .goatTechnicalId(null)
                .goatId("RG-LEGACY")
                .eventType(ReproductiveEventType.COVERAGE)
                .eventDate(LocalDate.of(2024, 1, 1))
                .breedingType(BreedingType.NATURAL)
                .build());

        List<ReproductiveEventEntity> result = reproductiveEventRepository.findHistoricalCandidates(10L, "RG-LEGACY");
        assertThat(result).extracting(ReproductiveEventEntity::getId).containsExactly(legacyE.getId());
    }

    @Test
    @DisplayName("ReproductiveEvent query: Case C - row with DIFFERENT non-null goatTechnicalId and matching RG is NOT returned")
    void reproductiveEvent_caseC_differentNonNullTechnicalIdWithMatchingRgNotReturned() {
        reproductiveEventRepository.save(ReproductiveEventEntity.builder()
                .farmId(1L)
                .goatTechnicalId(99L)
                .goatId("RG-SHARED")
                .eventType(ReproductiveEventType.COVERAGE)
                .eventDate(LocalDate.of(2026, 1, 1))
                .breedingType(BreedingType.NATURAL)
                .build());

        List<ReproductiveEventEntity> result = reproductiveEventRepository.findHistoricalCandidates(10L, "RG-SHARED");
        assertThat(result).isEmpty();
    }
}
