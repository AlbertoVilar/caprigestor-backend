package com.devmaster.goatfarm.milk.business.lactationservice;

import com.devmaster.goatfarm.application.ports.out.LactationPersistencePort;
import com.devmaster.goatfarm.config.exceptions.custom.ValidationException;
import com.devmaster.goatfarm.milk.business.bo.LactationRequestVO;
import com.devmaster.goatfarm.milk.business.bo.LactationResponseVO;
import com.devmaster.goatfarm.milk.enums.LactationStatus;
import com.devmaster.goatfarm.milk.mapper.LactationMapper;
import com.devmaster.goatfarm.milk.model.entity.Lactation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.mockito.ArgumentCaptor;

@ExtendWith(MockitoExtension.class)
class LactationBusinessTest {

    @Mock
    private LactationPersistencePort lactationPersistencePort;

    @Mock
    private LactationMapper lactationMapper;

    @InjectMocks
    private LactationBusiness lactationBusiness;

    @BeforeEach
    void setUp() {
        // Método executado antes de cada teste.
        // Útil para resetar mocks ou configurar comportamento padrão se necessário.
    }

    // ==================================================================================
    // CREATE (OPEN LACTATION)
    // ==================================================================================

    @Test
    void openLactation_shouldCreateActiveLactation_whenNoActiveExists() {
        // given: não existe lactação ativa
        // when: openLactation é chamado
        // then: salva entity com status ACTIVE, endDate null e startDate do request; retorna ResponseVO

        // Arrange
        Long farmId = 1L;
        String goatId = "123";
        LactationRequestVO requestVO = validRequestVO();

        when(lactationPersistencePort.findActiveByFarmIdAndGoatId(farmId, goatId))
                .thenReturn(Optional.empty());

        Lactation savedEntity = savedLactationEntity();
        savedEntity.setFarmId(farmId);
        savedEntity.setGoatId(goatId);

        when(lactationPersistencePort.save(any(Lactation.class)))
                .thenReturn(savedEntity);

        LactationResponseVO expectedVO = responseVO();
        when(lactationMapper.toResponseVO(savedEntity))
                .thenReturn(expectedVO);

        ArgumentCaptor<Lactation> captor = ArgumentCaptor.forClass(Lactation.class);

        // Act
        LactationResponseVO result = lactationBusiness.openLactation(farmId, goatId, requestVO);

        // Assert
        assertNotNull(result);
        assertEquals(expectedVO.getId(), result.getId());
        assertEquals(expectedVO.getStatus(), result.getStatus());

        // Verify
        verify(lactationPersistencePort).findActiveByFarmIdAndGoatId(farmId, goatId);
        verify(lactationPersistencePort).save(captor.capture());

        Lactation capturedEntity = captor.getValue();
        assertEquals(farmId, capturedEntity.getFarmId());
        assertEquals(goatId, capturedEntity.getGoatId());
        assertEquals(requestVO.getStartDate(), capturedEntity.getStartDate());
        assertEquals(LactationStatus.ACTIVE, capturedEntity.getStatus());
        assertNull(capturedEntity.getEndDate());
    }

    @Test
    void openLactation_shouldThrowValidationException_whenActiveLactationAlreadyExists() {
        // given: findActive retorna lactação ACTIVE
        // then: lança ValidationException e não chama save

        // Arrange
        Long farmId = 1L;
        String goatId = "1643218012";

        LactationRequestVO requestVO = validRequestVO();
        Lactation activeEntity = activeLactationEntity();

        when(lactationPersistencePort.findActiveByFarmIdAndGoatId(farmId, goatId))
                .thenReturn(Optional.of(activeEntity));

        // Act & Assert
        assertThrows(ValidationException.class,
                () -> lactationBusiness.openLactation(farmId, goatId, requestVO));

        // Verify
        verify(lactationPersistencePort).findActiveByFarmIdAndGoatId(farmId, goatId);
        verify(lactationPersistencePort, never()).save(any(Lactation.class));
        verifyNoInteractions(lactationMapper);
    }

    @Test
    void openLactation_shouldThrowValidationException_whenStartDateIsInFuture() {
        // given: request com data futura
        // then: lança ValidationException e não chama banco

        // Arrange
        Long farmId = 1L;
        String goatId = "123";

        // Cria request inline para garantir data futura
        LactationRequestVO futureRequest = new LactationRequestVO();
        futureRequest.setStartDate(LocalDate.now().plusDays(1));

        // Act & Assert
        assertThrows(ValidationException.class,
                () -> lactationBusiness.openLactation(farmId, goatId, futureRequest));

        // Verify
        verifyNoInteractions(lactationPersistencePort);
        verifyNoInteractions(lactationMapper);
    }

    // ==================================================================================
    // HELPERS
    // ==================================================================================

    private LactationRequestVO validRequestVO() {
        return LactationRequestVO.builder()
                .startDate(LocalDate.of(2026, 1, 1))
                .build();
    }

    private Lactation activeLactationEntity() {
        Lactation entity = new Lactation();
        entity.setId(10L);
        entity.setStatus(LactationStatus.ACTIVE);
        entity.setStartDate(LocalDate.of(2026, 1, 1));
        return entity;
    }

    private Lactation savedLactationEntity() {
        Lactation entity = new Lactation();
        entity.setId(11L);
        entity.setStatus(LactationStatus.ACTIVE);
        entity.setStartDate(LocalDate.of(2026, 1, 1));
        return entity;
    }

    private LactationResponseVO responseVO() {
        return LactationResponseVO.builder()
                .id(11L)
                .status(LactationStatus.ACTIVE)
                .startDate(LocalDate.of(2026, 1, 1))
                .build();
    }
}
