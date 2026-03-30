package com.devmaster.goatfarm.milk.persistence.repository;

import com.devmaster.goatfarm.milk.persistence.entity.FarmMilkProduction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface FarmMilkProductionRepository extends JpaRepository<FarmMilkProduction, Long> {

    @Modifying
    @Query(
            value = """
                    INSERT INTO farm_milk_production (
                        farm_id,
                        production_date,
                        total_produced,
                        withdrawal_produced,
                        marketable_produced,
                        notes,
                        created_at,
                        updated_at
                    ) VALUES (
                        :farmId,
                        :productionDate,
                        :totalProduced,
                        :withdrawalProduced,
                        :marketableProduced,
                        :notes,
                        CURRENT_TIMESTAMP,
                        CURRENT_TIMESTAMP
                    )
                    ON CONFLICT (farm_id, production_date)
                    DO UPDATE SET
                        total_produced = EXCLUDED.total_produced,
                        withdrawal_produced = EXCLUDED.withdrawal_produced,
                        marketable_produced = EXCLUDED.marketable_produced,
                        notes = EXCLUDED.notes,
                        updated_at = CURRENT_TIMESTAMP
                    """,
            nativeQuery = true
    )
    void upsertDaily(
            @Param("farmId") Long farmId,
            @Param("productionDate") LocalDate productionDate,
            @Param("totalProduced") java.math.BigDecimal totalProduced,
            @Param("withdrawalProduced") java.math.BigDecimal withdrawalProduced,
            @Param("marketableProduced") java.math.BigDecimal marketableProduced,
            @Param("notes") String notes
    );

    Optional<FarmMilkProduction> findByFarmIdAndProductionDate(Long farmId, LocalDate productionDate);

    List<FarmMilkProduction> findByFarmIdAndProductionDateBetweenOrderByProductionDateAsc(
            Long farmId,
            LocalDate from,
            LocalDate to
    );
}
