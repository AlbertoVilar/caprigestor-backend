package com.devmaster.goatfarm.milk.persistence.mapper;

import com.devmaster.goatfarm.milk.domain.FarmMilkProduction;
import com.devmaster.goatfarm.milk.persistence.entity.FarmMilkProductionEntity;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class FarmMilkProductionPersistenceMapperTest {
    @Test
    void mapsEveryFieldAndTimestamps() {
        var mapper = new FarmMilkProductionPersistenceMapper();
        LocalDateTime now = LocalDateTime.now();
        FarmMilkProduction d = new FarmMilkProduction(1L, 2L, LocalDate.now(), BigDecimal.TEN,
                BigDecimal.ONE, new BigDecimal("9.00"), "n", now, now);
        FarmMilkProductionEntity e = mapper.toEntity(d);
        assertEquals(d.totalProduced(), e.getTotalProduced());
        assertEquals(d.updatedAt(), e.getUpdatedAt());
        assertEquals(d, mapper.toDomain(e));
    }
}
