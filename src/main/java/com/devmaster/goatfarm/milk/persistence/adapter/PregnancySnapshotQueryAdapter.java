package com.devmaster.goatfarm.milk.persistence.adapter;

import com.devmaster.goatfarm.milk.application.ports.out.PregnancySnapshotQueryPort;
import com.devmaster.goatfarm.sharedkernel.pregnancy.PregnancySnapshot;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
public class PregnancySnapshotQueryAdapter implements PregnancySnapshotQueryPort {

    private static final String SQL_FIND_LATEST_BY_FARM_AND_GOAT = """
            SELECT p.status,
                   p.breeding_date,
                   p.confirm_date,
                   p.closed_at,
                   COALESCE(p.breeding_date, p.confirm_date) AS start_date
              FROM pregnancy p
             WHERE p.farm_id = :farmId
               AND p.goat_id = :goatId
               AND (
                    :referenceDate IS NULL
                    OR COALESCE(p.breeding_date, p.confirm_date) <= :referenceDate
               )
             ORDER BY
                   CASE WHEN COALESCE(p.breeding_date, p.confirm_date) IS NULL THEN 1 ELSE 0 END,
                   COALESCE(p.breeding_date, p.confirm_date) DESC,
                   p.id DESC
             LIMIT 1
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public PregnancySnapshotQueryAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<PregnancySnapshot> findLatestByFarmIdAndGoatId(Long farmId, String goatId, LocalDate referenceDate) {
        LocalDate asOfReferenceDate = referenceDate != null ? referenceDate : LocalDate.now();

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("farmId", farmId)
                .addValue("goatId", goatId)
                .addValue("referenceDate", referenceDate);

        List<PregnancySnapshot> snapshots = jdbcTemplate.query(SQL_FIND_LATEST_BY_FARM_AND_GOAT, params, (rs, rowNum) -> {
            LocalDate breedingDate = toLocalDate(rs.getDate("breeding_date"));
            LocalDate confirmDate = toLocalDate(rs.getDate("confirm_date"));
            LocalDate startDate = toLocalDate(rs.getDate("start_date"));
            LocalDate closedAt = toLocalDate(rs.getDate("closed_at"));

            return new PregnancySnapshot(
                    isActiveAsOf(rs.getString("status"), startDate, closedAt, asOfReferenceDate),
                    breedingDate,
                    confirmDate
            );
        });

        return snapshots.stream().findFirst();
    }

    private boolean isActiveAsOf(String status,
                                 LocalDate startDate,
                                 LocalDate closedAt,
                                 LocalDate referenceDate) {
        if (!"ACTIVE".equalsIgnoreCase(status)) {
            return false;
        }
        if (startDate != null && startDate.isAfter(referenceDate)) {
            return false;
        }
        if (closedAt != null && !closedAt.isAfter(referenceDate)) {
            return false;
        }
        return true;
    }

    private LocalDate toLocalDate(Date value) {
        return value != null ? value.toLocalDate() : null;
    }
}
