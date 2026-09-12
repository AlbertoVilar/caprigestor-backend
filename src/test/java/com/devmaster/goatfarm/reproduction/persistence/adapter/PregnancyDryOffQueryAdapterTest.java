package com.devmaster.goatfarm.reproduction.persistence.adapter;

import com.devmaster.goatfarm.reproduction.application.model.PregnancyDryOffSnapshot;
import com.devmaster.goatfarm.reproduction.application.ports.in.PregnancyDryOffQueryUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Import(PregnancyDryOffQueryAdapter.class)
class PregnancyDryOffQueryAdapterTest {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private PregnancyDryOffQueryUseCase queryUseCase;

    @BeforeEach
    void setUp() {
        jdbcTemplate.getJdbcTemplate().execute("DROP TABLE IF EXISTS pregnancy");
        jdbcTemplate.getJdbcTemplate().execute("""
                CREATE TABLE pregnancy (
                    id BIGINT PRIMARY KEY,
                    farm_id BIGINT NOT NULL,
                    goat_technical_id BIGINT,
                    goat_id VARCHAR(50) NOT NULL,
                    status VARCHAR(20) NOT NULL,
                    breeding_date DATE,
                    confirm_date DATE,
                    closed_at DATE
                )
                """);
    }

    @Test
    void shouldSelectLatestStartDateAndUseIdAsTieBreaker() {
        insert(1L, 10L, 101L, "RG-1", "ACTIVE", LocalDate.of(2026, 1, 1), null, null);
        insert(2L, 10L, 101L, "RG-1", "ACTIVE", LocalDate.of(2026, 2, 1), null, null);
        insert(3L, 10L, 101L, "RG-1", "ACTIVE", LocalDate.of(2026, 2, 1), null, null);

        List<PregnancyDryOffSnapshot> result = queryUseCase.findLatestRelevantByFarmId(10L, LocalDate.of(2026, 3, 1));

        assertThat(result).singleElement().satisfies(snapshot -> assertThat(snapshot.id()).isEqualTo(3L));
    }

    @Test
    void shouldExcludePregnancyStartingAfterReferenceDate() {
        insert(1L, 10L, 101L, "RG-1", "ACTIVE", LocalDate.of(2026, 4, 1), null, null);

        assertThat(queryUseCase.findLatestRelevantByFarmId(10L, LocalDate.of(2026, 3, 1))).isEmpty();
    }

    @Test
    void shouldKeepLatestClosedPregnancyAndNeverResurrectOlderActiveOne() {
        insert(1L, 10L, 101L, "RG-1", "ACTIVE", LocalDate.of(2026, 1, 1), null, null);
        insert(2L, 10L, 101L, "RG-1", "CLOSED", LocalDate.of(2026, 2, 1), null, LocalDate.of(2026, 2, 10));

        PregnancyDryOffSnapshot result = queryUseCase.findLatestRelevantByFarmId(10L, LocalDate.of(2026, 3, 1))
                .get(0);

        assertThat(result.id()).isEqualTo(2L);
        assertThat(result.activeAsOf(LocalDate.of(2026, 3, 1))).isFalse();
    }

    @Test
    void shouldTreatActivePregnancyClosedAfterReferenceAsActive() {
        insert(1L, 10L, 101L, "RG-1", "ACTIVE", LocalDate.of(2026, 1, 1), null, LocalDate.of(2026, 3, 15));

        PregnancyDryOffSnapshot result = queryUseCase.findLatestRelevantByFarmId(10L, LocalDate.of(2026, 3, 1))
                .get(0);

        assertThat(result.activeAsOf(LocalDate.of(2026, 3, 1))).isTrue();
        assertThat(result.activeAsOf(LocalDate.of(2026, 3, 15))).isFalse();
    }

    @Test
    void shouldUseTechnicalGoatIdWhenPresentAndRgWhenAbsent() {
        insert(1L, 10L, 101L, "RG-TECH", "ACTIVE", LocalDate.of(2026, 1, 1), null, null);
        insert(2L, 10L, null, "RG-ONLY", "ACTIVE", LocalDate.of(2026, 1, 1), null, null);

        List<PregnancyDryOffSnapshot> result = queryUseCase.findLatestRelevantByFarmId(10L, LocalDate.of(2026, 3, 1));

        assertThat(result).extracting(PregnancyDryOffSnapshot::goatKey)
                .containsExactlyInAnyOrder("101", "RG-ONLY");
    }

    @Test
    void shouldPreserveFarmScoping() {
        insert(1L, 10L, 101L, "RG-1", "ACTIVE", LocalDate.of(2026, 1, 1), null, null);
        insert(2L, 11L, 202L, "RG-2", "ACTIVE", LocalDate.of(2026, 1, 1), null, null);

        List<PregnancyDryOffSnapshot> result = queryUseCase.findLatestRelevantByFarmId(10L, LocalDate.of(2026, 3, 1));

        assertThat(result).singleElement().extracting(PregnancyDryOffSnapshot::farmId).isEqualTo(10L);
    }

    private void insert(Long id, Long farmId, Long goatTechnicalId, String goatId, String status,
                         LocalDate breedingDate, LocalDate confirmDate, LocalDate closedAt) {
        jdbcTemplate.update("""
                INSERT INTO pregnancy (id, farm_id, goat_technical_id, goat_id, status, breeding_date, confirm_date, closed_at)
                VALUES (:id, :farmId, :goatTechnicalId, :goatId, :status, :breedingDate, :confirmDate, :closedAt)
                """, new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("farmId", farmId)
                .addValue("goatTechnicalId", goatTechnicalId)
                .addValue("goatId", goatId)
                .addValue("status", status)
                .addValue("breedingDate", breedingDate)
                .addValue("confirmDate", confirmDate)
                .addValue("closedAt", closedAt));
    }
}
