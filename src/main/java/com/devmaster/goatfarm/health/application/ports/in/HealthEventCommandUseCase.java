package com.devmaster.goatfarm.health.application.ports.in;

import com.devmaster.goatfarm.health.business.bo.*;

public interface HealthEventCommandUseCase {

    HealthEventResponseVO create(Long farmId, String goatId, HealthEventCreateRequestVO request);

    HealthEventResponseVO update(Long farmId, String goatId, Long eventId, HealthEventUpdateRequestVO request);

    HealthEventResponseVO markAsDone(Long farmId, String goatId, Long eventId, HealthEventDoneRequestVO request);

    HealthEventResponseVO cancel(Long farmId, String goatId, Long eventId, HealthEventCancelRequestVO request);

}
