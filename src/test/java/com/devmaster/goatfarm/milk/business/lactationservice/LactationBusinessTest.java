package com.devmaster.goatfarm.milk.business.lactationservice;

import com.devmaster.goatfarm.milk.application.ports.out.LactationPersistencePort;
import com.devmaster.goatfarm.milk.application.ports.out.MilkProductionPersistencePort;
import com.devmaster.goatfarm.reproduction.application.ports.out.PregnancyPersistencePort;
import com.devmaster.goatfarm.application.core.business.validation.GoatGenderValidator;
import com.devmaster.goatfarm.config.exceptions.custom.BusinessRuleException;
import com.devmaster.goatfarm.config.exceptions.custom.InvalidArgumentException;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.milk.business.bo.LactationDryRequestVO;
import com.devmaster.goatfarm.milk.business.bo.LactationRequestVO;
import com.devmaster.goatfarm.milk.business.bo.LactationResponseVO;
import com.devmaster.goatfarm.milk.enums.LactationStatus;
import com.devmaster.goatfarm.milk.business.mapper.LactationBusinessMapper;
import com.devmaster.goatfarm.milk.persistence.entity.Lactation;
import com.devmaster.goatfarm.goat.persistence.entity.Goat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LactationBusinessTest {

    @Mock
    private LactationPersistencePort lactationPersistencePort;

    @Mock
    private MilkProductionPersistencePort milkProductionPersistencePort;

    @Mock
    private PregnancyPersistencePort pregnancyPersistencePort;

    @Mock
    private GoatGenderValidator goatGenderValidator;

    @Mock
    private LactationBusinessMapper lactationMapper;

    @InjectMocks
    private LactationBusiness lactationBusiness;

    @BeforeEach
    void setUp() {
        // Método executado antes de cada teste.
        // Útil para resetar mocks ou configurar comportamento padrão se necessário.
        lenient().when(goatGenderValidator.requireFemale(anyLong(), anyString())).thenReturn(new Goat());
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
        // then: lança BusinessRuleException e não chama save

        // Arrange
        Long farmId = 1L;
        String goatId = "1643218012";

        LactationRequestVO requestVO = validRequestVO();
        Lactation activeEntity = activeLactationEntity();

        when(lactationPersistencePort.findActiveByFarmIdAndGoatId(farmId, goatId))
                .thenReturn(Optional.of(activeEntity));

        // Act & Assert
        assertThrows(BusinessRuleException.class,
                () -> lactationBusiness.openLactation(farmId, goatId, requestVO));

        // Verify
        verify(lactationPersistencePort).findActiveByFarmIdAndGoatId(farmId, goatId);
        verify(lactationPersistencePort, never()).save(any(Lactation.class));
        verifyNoInteractions(lactationMapper);
    }

    @Test
    void openLactation_shouldThrowValidationException_whenStartDateIsInFuture() {
        // given: request com data futura
        // then: lança InvalidArgumentException e não chama banco

        // Arrange
        Long farmId = 1L;
        String goatId = "123";

        // Cria request inline para garantir data futura
        LactationRequestVO futureRequest = new LactationRequestVO();
        futureRequest.setStartDate(LocalDate.now().plusDays(1));

        // Act & Assert
        InvalidArgumentException ex = assertThrows(InvalidArgumentException.class,
                () -> lactationBusiness.openLactation(farmId, goatId, futureRequest));
        
        assertEquals("startDate", ex.getFieldName());

        // Verify
        verifyNoInteractions(lactationPersistencePort);
        verifyNoInteractions(lactationMapper);
    }

    // ==================================================================================
    // UPDATE (DRY/CLOSE LACTATION)
    // ==================================================================================

    @Test
    void dryLactation_shouldCloseLactation_whenValidRequest() {
        // given: lactação ACTIVE existe, request com data válida
        // when: dryLactation é chamado
        // then: atualiza status para CLOSED, set endDate e dryStartDate, salva e retorna ResponseVO

        // Arrange
        Long farmId = 1L;
        String goatId = "123";
        Long lactationId = 10L;
        LocalDate startDate = LocalDate.of(2026, 1, 1);
        LocalDate endDate = LocalDate.of(2026, 10, 1);

        LactationDryRequestVO dryRequestVO = new LactationDryRequestVO();
        dryRequestVO.setEndDate(endDate);

        Lactation existingLactation = new Lactation();
        existingLactation.setId(lactationId);
        existingLactation.setFarmId(farmId);
        existingLactation.setGoatId(goatId);
        existingLactation.setStatus(LactationStatus.ACTIVE);
        existingLactation.setStartDate(startDate);

        when(lactationPersistencePort.findByIdAndFarmIdAndGoatId(lactationId, farmId, goatId))
                .thenReturn(Optional.of(existingLactation));

        when(lactationPersistencePort.save(any(Lactation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0)); // retorna o próprio objeto salvo

        LactationResponseVO expectedVO = responseVO();
        expectedVO.setStatus(LactationStatus.CLOSED);
        expectedVO.setEndDate(endDate);

        when(lactationMapper.toResponseVO(any(Lactation.class)))
                .thenReturn(expectedVO);

        // Act
        LactationResponseVO result = lactationBusiness.dryLactation(farmId, goatId, lactationId, dryRequestVO);

        // Assert
        assertNotNull(result);
        assertEquals(LactationStatus.CLOSED, result.getStatus());
        assertEquals(endDate, result.getEndDate());

        ArgumentCaptor<Lactation> captor = ArgumentCaptor.forClass(Lactation.class);
        verify(lactationPersistencePort).save(captor.capture());

        Lactation savedEntity = captor.getValue();
        assertEquals(LactationStatus.CLOSED, savedEntity.getStatus());
        assertEquals(endDate, savedEntity.getEndDate());
        assertEquals(endDate, savedEntity.getDryStartDate()); // Regra: dryStartDate = endDate
    }

    @Test
    void dryLactation_shouldThrowResourceNotFound_whenLactationNotFound() {
        // Arrange
        Long farmId = 1L;
        String goatId = "123";
        Long lactationId = 999L;
        LactationDryRequestVO dryRequestVO = new LactationDryRequestVO();

        when(lactationPersistencePort.findByIdAndFarmIdAndGoatId(lactationId, farmId, goatId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> lactationBusiness.dryLactation(farmId, goatId, lactationId, dryRequestVO));
        
        verify(lactationPersistencePort, never()).save(any());
    }

    @Test
    void dryLactation_shouldThrowValidationException_whenLactationNotActive() {
        // Arrange
        Long farmId = 1L;
        String goatId = "123";
        Long lactationId = 10L;
        
        Lactation closedLactation = new Lactation();
        closedLactation.setStatus(LactationStatus.CLOSED);

        when(lactationPersistencePort.findByIdAndFarmIdAndGoatId(lactationId, farmId, goatId))
                .thenReturn(Optional.of(closedLactation));

        LactationDryRequestVO dryRequestVO = new LactationDryRequestVO();

        // Act & Assert
        assertThrows(BusinessRuleException.class,
                () -> lactationBusiness.dryLactation(farmId, goatId, lactationId, dryRequestVO));
    }

    @Test
    void dryLactation_shouldThrowValidationException_whenEndDateBeforeStartDate() {
        // Arrange
        Long farmId = 1L;
        String goatId = "123";
        Long lactationId = 10L;
        LocalDate startDate = LocalDate.of(2026, 5, 1);
        
        Lactation activeLactation = new Lactation();
        activeLactation.setStatus(LactationStatus.ACTIVE);
        activeLactation.setStartDate(startDate);

        when(lactationPersistencePort.findByIdAndFarmIdAndGoatId(lactationId, farmId, goatId))
                .thenReturn(Optional.of(activeLactation));

        LactationDryRequestVO dryRequestVO = new LactationDryRequestVO();
        dryRequestVO.setEndDate(startDate.minusDays(1)); // Data anterior ao início

        // Act & Assert
        assertThrows(BusinessRuleException.class,
                () -> lactationBusiness.dryLactation(farmId, goatId, lactationId, dryRequestVO));
    }

    // ==================================================================================
    // READ (GET ACTIVE, GET BY ID, LIST)
    // ==================================================================================

    @Test
    void getActiveLactation_shouldReturnLactation_whenExists() {
        // Arrange
        Long farmId = 1L;
        String goatId = "123";
        Lactation activeLactation = activeLactationEntity();
        LactationResponseVO expectedVO = responseVO();

        when(lactationPersistencePort.findActiveByFarmIdAndGoatId(farmId, goatId))
                .thenReturn(Optional.of(activeLactation));
        when(lactationMapper.toResponseVO(activeLactation)).thenReturn(expectedVO);

        // Act
        LactationResponseVO result = lactationBusiness.getActiveLactation(farmId, goatId);

        // Assert
        assertNotNull(result);
        assertEquals(expectedVO.getId(), result.getId());
    }

    @Test
    void getActiveLactation_shouldThrowResourceNotFound_whenNotExists() {
        // Arrange
        Long farmId = 1L;
        String goatId = "123";
        when(lactationPersistencePort.findActiveByFarmIdAndGoatId(farmId, goatId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> lactationBusiness.getActiveLactation(farmId, goatId));
    }

    @Test
    void getLactationById_shouldReturnLactation_whenExists() {
        // Arrange
        Long farmId = 1L;
        String goatId = "123";
        Long lactationId = 10L;
        Lactation lactation = activeLactationEntity();
        LactationResponseVO expectedVO = responseVO();

        when(lactationPersistencePort.findByIdAndFarmIdAndGoatId(lactationId, farmId, goatId))
                .thenReturn(Optional.of(lactation));
        when(lactationMapper.toResponseVO(lactation)).thenReturn(expectedVO);

        // Act
        LactationResponseVO result = lactationBusiness.getLactationById(farmId, goatId, lactationId);

        // Assert
        assertNotNull(result);
        assertEquals(expectedVO.getId(), result.getId());
    }

    @Test
    void getAllLactations_shouldReturnPageOfLactations() {
        // Arrange
        Long farmId = 1L;
        String goatId = "123";
        Pageable pageable = PageRequest.of(0, 10);
        
        List<Lactation> lactationList = List.of(activeLactationEntity());
        Page<Lactation> lactationPage = new PageImpl<>(lactationList);
        
        LactationResponseVO responseVO = responseVO();

        when(lactationPersistencePort.findAllByFarmIdAndGoatId(farmId, goatId, pageable))
                .thenReturn(lactationPage);
        when(lactationMapper.toResponseVO(any(Lactation.class))).thenReturn(responseVO);

        // Act
        Page<LactationResponseVO> result = lactationBusiness.getAllLactations(farmId, goatId, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(responseVO.getId(), result.getContent().get(0).getId());
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
                .farmId(1L)
                .goatId("123")
                .status(LactationStatus.ACTIVE)
                .startDate(LocalDate.of(2026, 1, 1))
                .build();
    }
}
