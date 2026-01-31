package com.devmaster.goatfarm.health.business.healthservice;

import com.devmaster.goatfarm.config.exceptions.custom.BusinessRuleException;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.goat.application.ports.out.GoatPersistencePort;
import com.devmaster.goatfarm.health.application.ports.out.HealthEventPersistencePort;
import com.devmaster.goatfarm.health.business.bo.HealthEventCancelRequestVO;
import com.devmaster.goatfarm.health.business.bo.HealthEventCreateRequestVO;
import com.devmaster.goatfarm.health.business.bo.HealthEventDoneRequestVO;
import com.devmaster.goatfarm.health.business.bo.HealthEventResponseVO;
import com.devmaster.goatfarm.health.business.bo.HealthEventUpdateRequestVO;
import com.devmaster.goatfarm.health.business.mapper.HealthEventBusinessMapper;
import com.devmaster.goatfarm.health.domain.enums.HealthEventStatus;
import com.devmaster.goatfarm.health.persistence.entity.HealthEvent;
import com.devmaster.goatfarm.goat.persistence.entity.Goat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HealthEventBusinessTest {

    @Mock
    private HealthEventPersistencePort persistencePort;
    @Mock
    private GoatPersistencePort goatPersistencePort;
    @Mock
    private HealthEventBusinessMapper mapper;

    private HealthEventBusiness healthEventBusiness;

    private Long farmId = 1L;
    private String goatId = "goat-123";
    private Long eventId = 100L;
    private HealthEvent healthEvent;
    private Goat goat;

    @BeforeEach
    void setUp() {
        healthEventBusiness = new HealthEventBusiness(persistencePort, goatPersistencePort, mapper);

        healthEvent = new HealthEvent();
        healthEvent.setId(eventId);
        healthEvent.setFarmId(farmId);
        healthEvent.setGoatId(goatId);
        healthEvent.setStatus(HealthEventStatus.AGENDADO);

        goat = new Goat();
        goat.setRegistrationNumber(goatId);
        goat.setFarm(new com.devmaster.goatfarm.farm.persistence.entity.GoatFarm());
        goat.getFarm().setId(farmId);
    }

    @Test
    @DisplayName("Should create health event successfully")
    void create_success() {
        HealthEventCreateRequestVO request = new HealthEventCreateRequestVO();

        when(goatPersistencePort.findByIdAndFarmId(goatId, farmId)).thenReturn(Optional.of(goat));
        when(mapper.toEntity(request)).thenReturn(healthEvent);
        when(persistencePort.save(any(HealthEvent.class))).thenReturn(healthEvent);
        when(mapper.toResponseVO(healthEvent)).thenReturn(new HealthEventResponseVO());

        HealthEventResponseVO response = healthEventBusiness.create(farmId, goatId, request);

        assertNotNull(response);
        verify(goatPersistencePort).findByIdAndFarmId(goatId, farmId);
        verify(persistencePort).save(healthEvent);
    }

    @Test
    @DisplayName("Should fail to create when goat not found")
    void create_fail_goatNotFound() {
        HealthEventCreateRequestVO request = new HealthEventCreateRequestVO();

        when(goatPersistencePort.findByIdAndFarmId(goatId, farmId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
            healthEventBusiness.create(farmId, goatId, request)
        );

        verify(persistencePort, never()).save(any());
    }

    @Test
    @DisplayName("Should update health event successfully")
    void update_success() {
        HealthEventUpdateRequestVO request = new HealthEventUpdateRequestVO();

        when(persistencePort.findByIdAndFarmIdAndGoatId(eventId, farmId, goatId)).thenReturn(Optional.of(healthEvent));
        when(persistencePort.save(any(HealthEvent.class))).thenReturn(healthEvent);
        when(mapper.toResponseVO(healthEvent)).thenReturn(new HealthEventResponseVO());

        HealthEventResponseVO response = healthEventBusiness.update(farmId, goatId, eventId, request);

        assertNotNull(response);
        verify(mapper).updateEntity(healthEvent, request);
        verify(persistencePort).save(healthEvent);
    }

    @Test
    @DisplayName("Should fail to update when event status is not AGENDADO")
    void update_fail_wrongStatus() {
        healthEvent.setStatus(HealthEventStatus.REALIZADO);
        HealthEventUpdateRequestVO request = new HealthEventUpdateRequestVO();

        when(persistencePort.findByIdAndFarmIdAndGoatId(eventId, farmId, goatId)).thenReturn(Optional.of(healthEvent));

        assertThrows(BusinessRuleException.class, () ->
            healthEventBusiness.update(farmId, goatId, eventId, request)
        );

        verify(persistencePort, never()).save(any());
    }

    @Test
    @DisplayName("Should fail to update when event not found")
    void update_fail_notFound() {
        HealthEventUpdateRequestVO request = new HealthEventUpdateRequestVO();

        when(persistencePort.findByIdAndFarmIdAndGoatId(eventId, farmId, goatId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
            healthEventBusiness.update(farmId, goatId, eventId, request)
        );
    }

    @Test
    @DisplayName("Should mark health event as done successfully")
    void markAsDone_success() {
        HealthEventDoneRequestVO request = new HealthEventDoneRequestVO();
        
        when(persistencePort.findByIdAndFarmIdAndGoatId(eventId, farmId, goatId)).thenReturn(Optional.of(healthEvent));
        when(persistencePort.save(any(HealthEvent.class))).thenReturn(healthEvent);
        when(mapper.toResponseVO(healthEvent)).thenReturn(new HealthEventResponseVO());

        HealthEventResponseVO response = healthEventBusiness.markAsDone(farmId, goatId, eventId, request);

        assertNotNull(response);
        verify(persistencePort).save(healthEvent);
    }

    @Test
    @DisplayName("Should fail to mark as done when event status is not AGENDADO")
    void markAsDone_fail_wrongStatus() {
        healthEvent.setStatus(HealthEventStatus.CANCELADO);
        HealthEventDoneRequestVO request = new HealthEventDoneRequestVO();

        when(persistencePort.findByIdAndFarmIdAndGoatId(eventId, farmId, goatId)).thenReturn(Optional.of(healthEvent));

        assertThrows(BusinessRuleException.class, () ->
            healthEventBusiness.markAsDone(farmId, goatId, eventId, request)
        );

        verify(persistencePort, never()).save(any());
    }

    @Test
    @DisplayName("Should cancel health event successfully")
    void cancel_success() {
        HealthEventCancelRequestVO request = new HealthEventCancelRequestVO();
        
        when(persistencePort.findByIdAndFarmIdAndGoatId(eventId, farmId, goatId)).thenReturn(Optional.of(healthEvent));
        when(persistencePort.save(any(HealthEvent.class))).thenReturn(healthEvent);
        when(mapper.toResponseVO(healthEvent)).thenReturn(new HealthEventResponseVO());

        HealthEventResponseVO response = healthEventBusiness.cancel(farmId, goatId, eventId, request);

        assertNotNull(response);
        verify(persistencePort).save(healthEvent);
    }
}
