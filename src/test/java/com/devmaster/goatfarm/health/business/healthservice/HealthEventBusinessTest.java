package com.devmaster.goatfarm.health.business.healthservice;

import com.devmaster.goatfarm.application.core.business.common.EntityFinder;
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

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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
        EntityFinder entityFinder = new EntityFinder();
        healthEventBusiness = new HealthEventBusiness(persistencePort, goatPersistencePort, mapper, entityFinder);

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
        HealthEventCreateRequestVO request = HealthEventCreateRequestVO.builder().build();

        when(goatPersistencePort.findByIdAndFarmId(goatId, farmId)).thenReturn(Optional.of(goat));
        when(mapper.toEntity(request)).thenReturn(healthEvent);
        when(persistencePort.save(any(HealthEvent.class))).thenReturn(healthEvent);
        when(mapper.toResponseVO(healthEvent)).thenReturn(HealthEventResponseVO.builder().build());

        HealthEventResponseVO response = healthEventBusiness.create(farmId, goatId, request);

        assertNotNull(response);
        verify(goatPersistencePort).findByIdAndFarmId(goatId, farmId);
        verify(persistencePort).save(healthEvent);
    }

    @Test
    @DisplayName("Should fail to create when goat not found")
    void create_fail_goatNotFound() {
        HealthEventCreateRequestVO request = HealthEventCreateRequestVO.builder().build();

        when(goatPersistencePort.findByIdAndFarmId(goatId, farmId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
            healthEventBusiness.create(farmId, goatId, request)
        );

        verify(persistencePort, never()).save(any());
    }

    @Test
    @DisplayName("Should update health event successfully")
    void update_success() {
        HealthEventUpdateRequestVO request = HealthEventUpdateRequestVO.builder().build();

        when(persistencePort.findByIdAndFarmIdAndGoatId(eventId, farmId, goatId)).thenReturn(Optional.of(healthEvent));
        when(persistencePort.save(any(HealthEvent.class))).thenReturn(healthEvent);
        when(mapper.toResponseVO(healthEvent)).thenReturn(HealthEventResponseVO.builder().build());

        HealthEventResponseVO response = healthEventBusiness.update(farmId, goatId, eventId, request);

        assertNotNull(response);
        verify(mapper).updateEntity(healthEvent, request);
        verify(persistencePort).save(healthEvent);
    }

    @Test
    @DisplayName("Should fail to update when event status is not AGENDADO")
    void update_fail_wrongStatus() {
        healthEvent.setStatus(HealthEventStatus.REALIZADO);
        HealthEventUpdateRequestVO request = HealthEventUpdateRequestVO.builder().build();

        when(persistencePort.findByIdAndFarmIdAndGoatId(eventId, farmId, goatId)).thenReturn(Optional.of(healthEvent));

        assertThrows(BusinessRuleException.class, () ->
            healthEventBusiness.update(farmId, goatId, eventId, request)
        );

        verify(persistencePort, never()).save(any());
    }

    @Test
    @DisplayName("Should fail to update when event not found")
    void update_fail_notFound() {
        HealthEventUpdateRequestVO request = HealthEventUpdateRequestVO.builder().build();

        when(persistencePort.findByIdAndFarmIdAndGoatId(eventId, farmId, goatId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
            healthEventBusiness.update(farmId, goatId, eventId, request)
        );
    }

    @Test
    @DisplayName("Should mark health event as done successfully")
    void markAsDone_success() {
        HealthEventDoneRequestVO request = HealthEventDoneRequestVO.builder().build();
        
        when(persistencePort.findByIdAndFarmIdAndGoatId(eventId, farmId, goatId)).thenReturn(Optional.of(healthEvent));
        when(persistencePort.save(any(HealthEvent.class))).thenReturn(healthEvent);
        when(mapper.toResponseVO(healthEvent)).thenReturn(HealthEventResponseVO.builder().build());

        HealthEventResponseVO response = healthEventBusiness.markAsDone(farmId, goatId, eventId, request);

        assertNotNull(response);
        verify(persistencePort).save(healthEvent);
    }

    @Test
    @DisplayName("Should fail to mark as done when event status is not AGENDADO")
    void markAsDone_fail_wrongStatus() {
        healthEvent.setStatus(HealthEventStatus.CANCELADO);
        HealthEventDoneRequestVO request = HealthEventDoneRequestVO.builder().build();

        when(persistencePort.findByIdAndFarmIdAndGoatId(eventId, farmId, goatId)).thenReturn(Optional.of(healthEvent));

        assertThrows(BusinessRuleException.class, () ->
            healthEventBusiness.markAsDone(farmId, goatId, eventId, request)
        );

        verify(persistencePort, never()).save(any());
    }

    @Test
    @DisplayName("Should cancel health event successfully")
    void cancel_success() {
        HealthEventCancelRequestVO request = HealthEventCancelRequestVO.builder().build();
        
        when(persistencePort.findByIdAndFarmIdAndGoatId(eventId, farmId, goatId)).thenReturn(Optional.of(healthEvent));
        when(persistencePort.save(any(HealthEvent.class))).thenReturn(healthEvent);
        when(mapper.toResponseVO(healthEvent)).thenReturn(HealthEventResponseVO.builder().build());

        HealthEventResponseVO response = healthEventBusiness.cancel(farmId, goatId, eventId, request);

        assertNotNull(response);
        verify(persistencePort).save(healthEvent);
    }

    @Test
    @DisplayName("Should reopen health event marked as performed")
    void reopen_success_fromRealizado() {
        healthEvent.setStatus(HealthEventStatus.REALIZADO);
        healthEvent.setPerformedAt(LocalDateTime.now().minusDays(1));

        when(persistencePort.findByIdAndFarmIdAndGoatId(eventId, farmId, goatId)).thenReturn(Optional.of(healthEvent));
        when(persistencePort.save(any(HealthEvent.class))).thenReturn(healthEvent);
        when(mapper.toResponseVO(healthEvent)).thenReturn(HealthEventResponseVO.builder().build());

        HealthEventResponseVO response = healthEventBusiness.reopen(farmId, goatId, eventId);

        assertNotNull(response);
        assertEquals(HealthEventStatus.AGENDADO, healthEvent.getStatus());
        assertNull(healthEvent.getPerformedAt());
        verify(persistencePort).save(healthEvent);
    }

    @Test
    @DisplayName("Should reopen health event marked as cancelled")
    void reopen_success_fromCancelled() {
        healthEvent.setStatus(HealthEventStatus.CANCELADO);
        healthEvent.setPerformedAt(LocalDateTime.now().minusDays(2));

        when(persistencePort.findByIdAndFarmIdAndGoatId(eventId, farmId, goatId)).thenReturn(Optional.of(healthEvent));
        when(persistencePort.save(any(HealthEvent.class))).thenReturn(healthEvent);
        when(mapper.toResponseVO(healthEvent)).thenReturn(HealthEventResponseVO.builder().build());

        HealthEventResponseVO response = healthEventBusiness.reopen(farmId, goatId, eventId);

        assertNotNull(response);
        assertEquals(HealthEventStatus.AGENDADO, healthEvent.getStatus());
        assertNull(healthEvent.getPerformedAt());
        verify(persistencePort).save(healthEvent);
    }

    @Test
    @DisplayName("Should fail to reopen health event already scheduled")
    void reopen_fail_alreadyScheduled() {
        healthEvent.setStatus(HealthEventStatus.AGENDADO);

        when(persistencePort.findByIdAndFarmIdAndGoatId(eventId, farmId, goatId)).thenReturn(Optional.of(healthEvent));

        assertThrows(BusinessRuleException.class, () ->
            healthEventBusiness.reopen(farmId, goatId, eventId)
        );

        verify(persistencePort, never()).save(any());
    }
}
