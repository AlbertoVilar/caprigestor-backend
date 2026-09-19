package com.devmaster.goatfarm.health.persistence.repository;

import com.devmaster.goatfarm.health.domain.enums.HealthEventStatus;
import com.devmaster.goatfarm.health.domain.enums.HealthEventType;
import com.devmaster.goatfarm.health.persistence.entity.HealthEvent;
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
class HistoricalHealthRepositoryIdentityTest {

    @Autowired
    private HealthEventRepository healthEventRepository;

    @AfterEach
    void tearDown() {
        healthEventRepository.deleteAll();
    }

    private HealthEvent createHealthEvent(Long goatTechnicalId, String goatId, Long farmId, String title, LocalDate scheduledDate, HealthEventStatus status) {
        HealthEvent e = new HealthEvent();
        e.setGoatTechnicalId(goatTechnicalId);
        e.setGoatId(goatId != null ? goatId : "RG-" + goatTechnicalId);
        e.setFarmId(farmId);
        e.setType(HealthEventType.VACINA);
        e.setStatus(status);
        e.setTitle(title);
        e.setScheduledDate(scheduledDate);
        return healthEventRepository.save(e);
    }

    private HealthEvent createHealthEvent(Long goatTechnicalId, Long farmId, String title, LocalDate scheduledDate, HealthEventStatus status) {
        return createHealthEvent(goatTechnicalId, "RG-" + goatTechnicalId, farmId, title, scheduledDate, status);
    }

    @Test
    @DisplayName("Health query: returns events matching exact goatTechnicalId and farmId")
    void returnsEventsMatchingGoatTechnicalIdAndFarmId() {
        HealthEvent e1 = createHealthEvent(10L, 1L, "Vacina A1", LocalDate.of(2026, 3, 1), HealthEventStatus.REALIZADO);
        HealthEvent e2 = createHealthEvent(10L, 1L, "Vacina A2", LocalDate.of(2026, 3, 15), HealthEventStatus.AGENDADO);

        List<HealthEvent> result = healthEventRepository.findHistoricalHealthEvents(10L, 1L);

        assertThat(result).extracting(HealthEvent::getId).containsExactly(e2.getId(), e1.getId());
    }

    @Test
    @DisplayName("Health query: excludes events with same goat but different farmId")
    void excludesSameGoatDifferentFarmId() {
        HealthEvent eFarmA = createHealthEvent(10L, 1L, "Vacina Farm A", LocalDate.of(2026, 1, 1), HealthEventStatus.REALIZADO);
        createHealthEvent(10L, 2L, "Vacina Farm B", LocalDate.of(2026, 4, 1), HealthEventStatus.REALIZADO);

        List<HealthEvent> resultFarmA = healthEventRepository.findHistoricalHealthEvents(10L, 1L);
        assertThat(resultFarmA).extracting(HealthEvent::getId).containsExactly(eFarmA.getId());

        List<HealthEvent> resultFarmB = healthEventRepository.findHistoricalHealthEvents(10L, 2L);
        assertThat(resultFarmB).extracting(HealthEvent::getTitle).containsExactly("Vacina Farm B");
    }

    @Test
    @DisplayName("Health query: excludes events with same farmId but different goatTechnicalId")
    void excludesDifferentGoatSameFarmId() {
        HealthEvent eGoat10 = createHealthEvent(10L, 1L, "Vacina Goat 10", LocalDate.of(2026, 1, 1), HealthEventStatus.REALIZADO);
        createHealthEvent(20L, 1L, "Vacina Goat 20", LocalDate.of(2026, 1, 1), HealthEventStatus.REALIZADO);

        List<HealthEvent> result = healthEventRepository.findHistoricalHealthEvents(10L, 1L);

        assertThat(result).extracting(HealthEvent::getId).containsExactly(eGoat10.getId());
    }

    @Test
    @DisplayName("Health query: does NOT match different goatTechnicalId merely because RG snapshot matches")
    void excludesDifferentGoatTechnicalIdWithSameRgSnapshot() {
        HealthEvent eGoat10 = createHealthEvent(10L, "RG-SHARED", 1L, "Vacina Goat 10", LocalDate.of(2026, 1, 1), HealthEventStatus.REALIZADO);
        createHealthEvent(20L, "RG-SHARED", 1L, "Vacina Goat 20", LocalDate.of(2026, 1, 1), HealthEventStatus.REALIZADO);

        List<HealthEvent> result = healthEventRepository.findHistoricalHealthEvents(10L, 1L);

        assertThat(result).extracting(HealthEvent::getId).containsExactly(eGoat10.getId());
    }

    @Test
    @DisplayName("Health query: sorts deterministically by scheduledDate DESC, then id DESC")
    void sortsDeterministicallyByScheduledDateDescThenIdDesc() {
        LocalDate sameDate = LocalDate.of(2026, 2, 10);
        HealthEvent eOld = createHealthEvent(10L, 1L, "Old", LocalDate.of(2026, 1, 1), HealthEventStatus.REALIZADO);
        HealthEvent eSame1 = createHealthEvent(10L, 1L, "Same 1", sameDate, HealthEventStatus.REALIZADO);
        HealthEvent eSame2 = createHealthEvent(10L, 1L, "Same 2", sameDate, HealthEventStatus.REALIZADO);
        HealthEvent eNew = createHealthEvent(10L, 1L, "New", LocalDate.of(2026, 5, 1), HealthEventStatus.AGENDADO);

        List<HealthEvent> result = healthEventRepository.findHistoricalHealthEvents(10L, 1L);

        // eNew is highest date; between eSame2 and eSame1, eSame2 has higher id; eOld is lowest date
        assertThat(result).extracting(HealthEvent::getId)
                .containsExactly(eNew.getId(), eSame2.getId(), eSame1.getId(), eOld.getId());
    }

    @Test
    @DisplayName("Health query: returns empty list when no records match")
    void returnsEmptyListWhenNoRecordsMatch() {
        List<HealthEvent> result = healthEventRepository.findHistoricalHealthEvents(999L, 1L);
        assertThat(result).isEmpty();
    }
}
