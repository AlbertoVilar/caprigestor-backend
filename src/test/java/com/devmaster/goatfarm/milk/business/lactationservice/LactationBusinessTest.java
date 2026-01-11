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
    @Disabled("TDD skeleton — habilitar após implementação do LactationBusiness.openLactation")
    void openLactation_shouldCreateActiveLactation_whenNoActiveExists() {
        // given: não existe lactação ativa
        // when: openLactation é chamado
        // then: salva entity com status ACTIVE, endDate null e startDate do request; retorna ResponseVO

        // Arrange
        Long farmId = 1L;
        String goatId = "1643218012";

        LactationRequestVO requestVO = validRequestVO();
        Lactation savedEntity = savedLactationEntity(farmId, goatId, requestVO.getStartDate());
        LactationResponseVO expectedResponseVO = responseVO(savedEntity.getId(), requestVO.getStartDate(), LactationStatus.ACTIVE);

        when(lactationPersistencePort.findActiveByFarmIdAndGoatId(farmId, goatId))
                .thenReturn(Optional.empty());

        when(lactationPersistencePort.save(any(Lactation.class)))
                .thenReturn(savedEntity);

        when(lactationMapper.toResponseVO(savedEntity))
                .thenReturn(expectedResponseVO);

        ArgumentCaptor<Lactation> captor = ArgumentCaptor.forClass(Lactation.class);

        // Act
        LactationResponseVO result = lactationBusiness.openLactation(farmId, goatId, requestVO);

        // Assert
        verify(lactationPersistencePort).findActiveByFarmIdAndGoatId(farmId, goatId);
        verify(lactationPersistencePort).save(captor.capture());
        verify(lactationMapper).toResponseVO(savedEntity);

        Lactation toSave = captor.getValue();

        assertEquals(farmId, toSave.getFarmId());
        assertEquals(goatId, toSave.getGoatId());
        assertEquals(requestVO.getStartDate(), toSave.getStartDate());
        assertEquals(LactationStatus.ACTIVE, toSave.getStatus());
        assertNull(toSave.getEndDate());

        assertNotNull(result);
        assertEquals(expectedResponseVO.getId(), result.getId());
        assertEquals(expectedResponseVO.getStatus(), result.getStatus());
        assertEquals(expectedResponseVO.getStartDate(), result.getStartDate());
    }

    @Test
    @Disabled("TDD skeleton — habilitar após implementação do LactationBusiness.openLactation")
    void openLactation_shouldThrowValidationException_whenActiveLactationAlreadyExists() {
        // given: findActive retorna lactação ACTIVE
        // then: lança ValidationException e não chama save

        // Arrange
        Long farmId = 1L;
        String goatId = "1643218012";

        LactationRequestVO requestVO = validRequestVO();
        Lactation activeEntity = activeLactationEntity(farmId, goatId);

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
    @Disabled("TDD skeleton — habilitar após implementação do LactationBusiness.openLactation")
    void openLactation_shouldThrowValidationException_whenStartDateIsInFuture() {
        // given: startDate > hoje
        // then: lança ValidationException e não chama findActive/save (ideal validar antes de persistência)

        // Arrange
        Long farmId = 1L;
        String goatId = "1643218012";

        LactationRequestVO requestVO = futureRequestVO();

        // Act & Assert
        assertThrows(ValidationException.class,
                () -> lactationBusiness.openLactation(farmId, goatId, requestVO));

        // Verify
        verifyNoInteractions(lactationPersistencePort);
        verifyNoInteractions(lactationMapper);
    }

    // ==================================================================================
    // HELPERS
    // ==================================================================================

    /**
     * Request VO válido: startDate no passado (evita conflito com regra de "data futura").
     */
    private LactationRequestVO validRequestVO() {
        return LactationRequestVO.builder()
                .startDate(LocalDate.now().minusDays(1))
                .build();
    }

    /**
     * Request VO inválido: startDate no futuro (regra de domínio).
     */
    private LactationRequestVO futureRequestVO() {
        return LactationRequestVO.builder()
                .startDate(LocalDate.now().plusDays(1))
                .build();
    }

    private Lactation activeLactationEntity(Long farmId, String goatId) {
        Lactation entity = new Lactation();
        entity.setId(10L);
        entity.setFarmId(farmId);
        entity.setGoatId(goatId);
        entity.setStatus(LactationStatus.ACTIVE);
        entity.setStartDate(LocalDate.now().minusDays(10));
        entity.setEndDate(null);
        return entity;
    }

    private Lactation savedLactationEntity(Long farmId, String goatId, LocalDate startDate) {
        Lactation entity = new Lactation();
        entity.setId(11L);
        entity.setFarmId(farmId);
        entity.setGoatId(goatId);
        entity.setStatus(LactationStatus.ACTIVE);
        entity.setStartDate(startDate);
        entity.setEndDate(null);
        return entity;
    }

    private LactationResponseVO responseVO(Long id, LocalDate startDate, LactationStatus status) {
        return LactationResponseVO.builder()
                .id(id)
                .status(status)
                .startDate(startDate)
                .build();
    }
}
