package com.devmaster.goatfarm.events.application.ports.out;

import com.devmaster.goatfarm.events.domain.OperationalEvent;
import com.devmaster.goatfarm.events.enums.EventType;
import com.devmaster.goatfarm.goat.domain.GoatId;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Persistence boundary owned by the Events application core.
 *
 * <p>It intentionally transports application/domain types only. JPA entities,
 * Spring pages and repository projections remain inside the persistence adapter.</p>
 */
public interface EventPersistencePort {

    OperationalEvent save(OperationalEvent event);

    Optional<OperationalEvent> findByIdAndGoatIdAndFarmId(Long eventId, GoatId goatId, Long farmId);

    EventPage<OperationalEvent> findByGoatIdWithFilters(
            GoatId goatId,
            Long farmId,
            EventType eventType,
            LocalDate startDate,
            LocalDate endDate,
            EventPageQuery pageQuery
    );

    void deleteById(Long id);

    void deleteEventsFromOtherUsers(Long adminId);
}
