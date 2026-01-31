package com.devmaster.goatfarm.health.business.healthservice;

import com.devmaster.goatfarm.config.exceptions.custom.BusinessRuleException;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.goat.application.ports.out.GoatPersistencePort;
import com.devmaster.goatfarm.health.application.ports.in.HealthEventCommandUseCase;
import com.devmaster.goatfarm.health.application.ports.in.HealthEventQueryUseCase;
import com.devmaster.goatfarm.health.application.ports.out.HealthEventPersistencePort;
import com.devmaster.goatfarm.health.business.bo.*;
import com.devmaster.goatfarm.health.business.mapper.HealthEventBusinessMapper;
import com.devmaster.goatfarm.health.domain.enums.HealthEventStatus;
import com.devmaster.goatfarm.health.domain.enums.HealthEventType;
import com.devmaster.goatfarm.health.persistence.entity.HealthEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class HealthEventBusiness implements HealthEventCommandUseCase, HealthEventQueryUseCase {

    private final HealthEventPersistencePort persistencePort;
    private final HealthEventBusinessMapper mapper;
    private final GoatPersistencePort goatPersistencePort;

    @Override
    @Transactional
    public HealthEventResponseVO create(Long farmId, String goatId, HealthEventCreateRequestVO request) {

        goatPersistencePort.findByIdAndFarmId(goatId, farmId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Cabra não encontrada no capril informado. goatId=" + goatId + ", farmId=" + farmId
                        )
                );

        var entity = mapper.toEntity(request);
        entity.setFarmId(farmId);
        entity.setGoatId(goatId);

        var entitySaved = persistencePort.save(entity);
        return mapper.toResponseVO(entitySaved);
    }

    @Override
    @Transactional
    public HealthEventResponseVO update(Long farmId, String goatId, Long eventId, HealthEventUpdateRequestVO request) {

        var healthEvent = persistencePort.findByIdAndFarmIdAndGoatId(eventId, farmId, goatId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Evento de saúde não encontrado. eventId=" + eventId + ", goatId=" + goatId + ", farmId=" + farmId
                        )
                );

        // Regra de negócio: só pode editar enquanto estiver AGENDADO
        if (healthEvent.getStatus() != HealthEventStatus.AGENDADO) {
            throw new BusinessRuleException(
                    "Não é permitido alterar um evento de saúde já realizado ou cancelado."
            );
        }

        mapper.updateEntity(healthEvent, request);

        var saved = persistencePort.save(healthEvent);
        return mapper.toResponseVO(saved);
    }


    @Override
    @Transactional
    public HealthEventResponseVO markAsDone(Long farmId, String goatId, Long eventId, HealthEventDoneRequestVO request) {
        // TODO: Implement logic
        return null;
    }

    @Override
    @Transactional
    public HealthEventResponseVO cancel(Long farmId, String goatId, Long eventId, HealthEventCancelRequestVO request) {
        // TODO: Implement logic
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public HealthEventResponseVO getById(Long farmId, String goatId, Long eventId) {
        // TODO: Implement logic
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<HealthEventResponseVO> listByGoat(Long farmId, String goatId, LocalDate from, LocalDate to, HealthEventType type, HealthEventStatus status, Pageable pageable) {
        // TODO: Implement logic
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<HealthEventResponseVO> listCalendar(Long farmId, LocalDate from, LocalDate to, HealthEventType type, HealthEventStatus status, Pageable pageable) {
        // TODO: Implement logic
        return null;
    }
}
