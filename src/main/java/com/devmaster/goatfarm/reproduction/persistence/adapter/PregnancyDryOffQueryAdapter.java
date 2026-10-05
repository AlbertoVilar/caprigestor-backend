package com.devmaster.goatfarm.reproduction.persistence.adapter;

import com.devmaster.goatfarm.reproduction.application.model.PregnancyDryOffSnapshot;
import com.devmaster.goatfarm.reproduction.application.ports.in.PregnancyDryOffQueryUseCase;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Reproduction-owned SQL adapter for farm-wide pregnancy dry-off selection. */
@Component
public class PregnancyDryOffQueryAdapter implements PregnancyDryOffQueryUseCase {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    public PregnancyDryOffQueryAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<PregnancyDryOffSnapshot> findLatestRelevantByFarmId(Long farmId, LocalDate referenceDate) {
        String sql = """
                select p.id, p.farm_id, p.goat_technical_id, p.goat_id, p.status,
                       p.breeding_date, p.confirm_date, p.closed_at,
                       coalesce(p.breeding_date, p.confirm_date) as start_date
                  from pregnancy p
                 where p.farm_id = :farmId
                   and coalesce(p.breeding_date, p.confirm_date) is not null
                   and coalesce(p.breeding_date, p.confirm_date) <= :referenceDate
                """;
        List<PregnancyDryOffSnapshot> candidates = jdbcTemplate.query(sql,
                new MapSqlParameterSource().addValue("farmId", farmId).addValue("referenceDate", referenceDate),
                (rs, rowNum) -> new PregnancyDryOffSnapshot(
                        rs.getLong("id"), rs.getLong("farm_id"),
                        (Long) rs.getObject("goat_technical_id"), rs.getString("goat_id"),
                        rs.getString("status"), toLocalDate(rs.getDate("breeding_date")),
                        toLocalDate(rs.getDate("confirm_date")), toLocalDate(rs.getDate("start_date")),
                        toLocalDate(rs.getDate("closed_at"))));
        Map<String, PregnancyDryOffSnapshot> latest = new LinkedHashMap<>();
        candidates.stream().sorted(Comparator.comparing(PregnancyDryOffSnapshot::startDate)
                        .thenComparing(PregnancyDryOffSnapshot::id))
                .forEach(p -> latest.put(p.goatKey(), p));
        return List.copyOf(latest.values());
    }

    private LocalDate toLocalDate(Date value) { return value == null ? null : value.toLocalDate(); }
}
