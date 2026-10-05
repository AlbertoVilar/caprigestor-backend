package com.devmaster.goatfarm.milk.persistence.repository;

import com.devmaster.goatfarm.milk.enums.LactationStatus;
import com.devmaster.goatfarm.milk.enums.MilkProductionStatus;
import com.devmaster.goatfarm.milk.enums.MilkingShift;
import com.devmaster.goatfarm.milk.persistence.entity.LactationEntity;
import com.devmaster.goatfarm.milk.persistence.entity.MilkProductionEntity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class HistoricalMilkLactationRepositoryIdentityTest {

    @Autowired
    private LactationRepository lactationRepository;

    @Autowired
    private MilkProductionRepository milkProductionRepository;

    @AfterEach
    void tearDown() {
        milkProductionRepository.deleteAll();
        lactationRepository.deleteAll();
    }

    @Test
    @DisplayName("Lactation query: Case A - row with matching goatTechnicalId is returned")
    void lactation_caseA_matchingTechnicalIdReturned() {
        LactationEntity lacA = lactationRepository.save(LactationEntity.builder()
                .farmId(1L)
                .goatTechnicalId(10L)
                .goatId("RG-10")
                .status(LactationStatus.ACTIVE)
                .startDate(LocalDate.of(2025, 1, 1))
                .build());

        List<LactationEntity> result = lactationRepository.findByGoatTechnicalIdOrRg(10L, "RG-OTHER");
        assertThat(result).extracting(LactationEntity::getId).containsExactly(lacA.getId());
    }

    @Test
    @DisplayName("Lactation query: Case B - legacy row with goatTechnicalId == null and matching RG is returned")
    void lactation_caseB_legacyRowWithNullTechnicalIdAndMatchingRgReturned() {
        LactationEntity legacyLac = lactationRepository.save(LactationEntity.builder()
                .farmId(1L)
                .goatTechnicalId(null)
                .goatId("RG-LEGACY")
                .status(LactationStatus.CLOSED)
                .startDate(LocalDate.of(2023, 1, 1))
                .build());

        List<LactationEntity> result = lactationRepository.findByGoatTechnicalIdOrRg(10L, "RG-LEGACY");
        assertThat(result).extracting(LactationEntity::getId).containsExactly(legacyLac.getId());
    }

    @Test
    @DisplayName("Lactation query: Case C - row with DIFFERENT non-null goatTechnicalId and matching RG is NOT returned")
    void lactation_caseC_differentNonNullTechnicalIdWithMatchingRgNotReturned() {
        lactationRepository.save(LactationEntity.builder()
                .farmId(1L)
                .goatTechnicalId(99L)
                .goatId("RG-SHARED")
                .status(LactationStatus.ACTIVE)
                .startDate(LocalDate.of(2025, 1, 1))
                .build());

        List<LactationEntity> result = lactationRepository.findByGoatTechnicalIdOrRg(10L, "RG-SHARED");
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("MilkProduction query: Case A - row with matching goatTechnicalId is returned")
    void milkProduction_caseA_matchingTechnicalIdReturned() {
        LactationEntity lac = lactationRepository.save(LactationEntity.builder()
                .farmId(1L)
                .goatTechnicalId(10L)
                .goatId("RG-10")
                .status(LactationStatus.ACTIVE)
                .startDate(LocalDate.of(2025, 1, 1))
                .build());

        MilkProductionEntity prodA = milkProductionRepository.save(MilkProductionEntity.builder()
                .farmId(1L)
                .goatTechnicalId(10L)
                .goatId("RG-10")
                .lactation(lac)
                .date(LocalDate.of(2025, 2, 1))
                .shift(MilkingShift.MORNING)
                .volumeLiters(new BigDecimal("3.00"))
                .status(MilkProductionStatus.ACTIVE)
                .build());

        List<MilkProductionEntity> result = milkProductionRepository.findHistoricalForDossier(1L, 10L, "RG-OTHER");
        assertThat(result).extracting(MilkProductionEntity::getId).containsExactly(prodA.getId());
    }

    @Test
    @DisplayName("MilkProduction query: Case B - legacy row with goatTechnicalId == null and matching RG is returned")
    void milkProduction_caseB_legacyRowWithNullTechnicalIdAndMatchingRgReturned() {
        LactationEntity lac = lactationRepository.save(LactationEntity.builder()
                .farmId(1L)
                .goatTechnicalId(null)
                .goatId("RG-LEGACY")
                .status(LactationStatus.CLOSED)
                .startDate(LocalDate.of(2023, 1, 1))
                .build());

        MilkProductionEntity legacyProd = milkProductionRepository.save(MilkProductionEntity.builder()
                .farmId(1L)
                .goatTechnicalId(null)
                .goatId("RG-LEGACY")
                .lactation(lac)
                .date(LocalDate.of(2023, 2, 1))
                .shift(MilkingShift.MORNING)
                .volumeLiters(new BigDecimal("2.50"))
                .status(MilkProductionStatus.ACTIVE)
                .build());

        List<MilkProductionEntity> result = milkProductionRepository.findHistoricalForDossier(1L, 10L, "RG-LEGACY");
        assertThat(result).extracting(MilkProductionEntity::getId).containsExactly(legacyProd.getId());
    }

    @Test
    @DisplayName("MilkProduction query: Case C - row with DIFFERENT non-null goatTechnicalId and matching RG is NOT returned")
    void milkProduction_caseC_differentNonNullTechnicalIdWithMatchingRgNotReturned() {
        LactationEntity lac = lactationRepository.save(LactationEntity.builder()
                .farmId(1L)
                .goatTechnicalId(99L)
                .goatId("RG-SHARED")
                .status(LactationStatus.ACTIVE)
                .startDate(LocalDate.of(2025, 1, 1))
                .build());

        milkProductionRepository.save(MilkProductionEntity.builder()
                .farmId(1L)
                .goatTechnicalId(99L)
                .goatId("RG-SHARED")
                .lactation(lac)
                .date(LocalDate.of(2025, 2, 1))
                .shift(MilkingShift.MORNING)
                .volumeLiters(new BigDecimal("3.20"))
                .status(MilkProductionStatus.ACTIVE)
                .build());

        List<MilkProductionEntity> result = milkProductionRepository.findHistoricalForDossier(1L, 10L, "RG-SHARED");
        assertThat(result).isEmpty();
    }
}
