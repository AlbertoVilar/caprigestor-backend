package com.devmaster.goatfarm.milk.business.milkproductionservice;

import com.devmaster.goatfarm.application.ports.out.LactationPersistencePort;
import com.devmaster.goatfarm.application.ports.out.MilkProductionPersistencePort;
import com.devmaster.goatfarm.application.core.business.validation.GoatGenderValidator;
import com.devmaster.goatfarm.config.exceptions.DuplicateMilkProductionException;
import com.devmaster.goatfarm.config.exceptions.NoActiveLactationException;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.config.exceptions.custom.ValidationError;
import com.devmaster.goatfarm.config.exceptions.custom.ValidationException;
import com.devmaster.goatfarm.milk.business.bo.MilkProductionRequestVO;
import com.devmaster.goatfarm.milk.business.bo.MilkProductionResponseVO;
import com.devmaster.goatfarm.milk.business.bo.MilkProductionUpdateRequestVO;
import com.devmaster.goatfarm.milk.mapper.MilkProductionMapper;
import com.devmaster.goatfarm.milk.model.entity.Lactation;
import com.devmaster.goatfarm.milk.model.entity.MilkProduction;
import com.devmaster.goatfarm.goat.persistence.entity.Goat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import com.devmaster.goatfarm.milk.enums.MilkingShift;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
class MilkProductionBusinessTest {

    @Mock
    private MilkProductionPersistencePort milkProductionPersistencePort;

    @Mock
    private LactationPersistencePort lactationPersistencePort;

    @Mock
    private GoatGenderValidator goatGenderValidator;

    @Mock
    private MilkProductionMapper milkProductionMapper;

    @InjectMocks
    private MilkProductionBusiness milkProductionBusiness;

    @BeforeEach
    void setUp() {
        // Método executado antes de cada teste.
        // Útil para resetar mocks ou configurar comportamento padrão se necessário.
        lenient().when(goatGenderValidator.requireFemale(anyLong(), anyString())).thenReturn(new Goat());
    }

    // ==================================================================================
    // CREATE
    // ==================================================================================

    @Test
    void shouldCreateMilkProduction_whenValidRequest() {
        // Arrange
        Long farmId = 1L;
        String goatId = "1643218012";

        MilkProductionRequestVO request = validCreateVO();

        MilkProduction entity = validEntity();
        MilkProductionResponseVO responseVO = validResponseVO();
        // Se tem lactação ativa
        Lactation lactation = new Lactation(); 
        lactation.setId(10L);

        when(milkProductionPersistencePort.existsByFarmIdAndGoatIdAndDateAndShift(
                eq(farmId), eq(goatId), eq(request.getDate()), eq(request.getShift())))
                .thenReturn(false);

        when(lactationPersistencePort.findActiveByFarmIdAndGoatId(farmId, goatId))
                .thenReturn(Optional.of(lactation));

        MilkProduction savedEntity = validEntity();

        when(milkProductionMapper.toEntity(request)).thenReturn(entity);
        when(milkProductionPersistencePort.save(entity)).thenReturn(savedEntity);
        when(milkProductionMapper.toResponseVO(savedEntity)).thenReturn(responseVO);

        // Act
        MilkProductionResponseVO result = milkProductionBusiness.createMilkProduction(farmId, goatId, request);

        // Assert
        assertNotNull(result);
        assertEquals(responseVO.getId(), result.getId());
        assertEquals(responseVO.getVolumeLiters(), result.getVolumeLiters());
        assertEquals(responseVO.getDate(), result.getDate());
        assertEquals(responseVO.getShift(), result.getShift());

        // Verify
        verify(milkProductionPersistencePort).existsByFarmIdAndGoatIdAndDateAndShift(farmId, goatId, request.getDate(), request.getShift());
        verify(lactationPersistencePort).findActiveByFarmIdAndGoatId(farmId, goatId);
        verify(milkProductionPersistencePort).save(entity);
    }

    @Test
    void shouldThrowValidationException_whenDateIsFuture() {
        // Arrange
        Long farmId = 1L;
        String goatId = "1643218012";
        MilkProductionRequestVO request = createVOWithFutureDate();

        // Act & Assert
        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> milkProductionBusiness.createMilkProduction(farmId, goatId, request)
        );

        ValidationError validationError = ex.getValidationError();

        assertNotNull(validationError);
        assertNotNull(validationError.getErrors());

        assertTrue(
                validationError.getErrors().stream()
                        .anyMatch(err -> "date".equals(err.getFieldName())),
                "Expected validation error to contain fieldName='date'"
        );

        // Verify
        verifyNoInteractions(milkProductionPersistencePort);
        verifyNoInteractions(lactationPersistencePort);
        verifyNoInteractions(milkProductionMapper);
    }

    @Test
    void shouldThrowDuplicateException_whenDuplicateDateAndShift() {
        // Arrange
        Long farmId = 1L;
        String goatId = "1643218012";
        MilkProductionRequestVO request = validCreateVO();

        when(milkProductionPersistencePort.existsByFarmIdAndGoatIdAndDateAndShift(
                farmId, goatId, request.getDate(), request.getShift()
        )).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateMilkProductionException.class,
                () -> milkProductionBusiness.createMilkProduction(farmId, goatId, request));

        // Verify
        verify(milkProductionPersistencePort).existsByFarmIdAndGoatIdAndDateAndShift(
                farmId, goatId, request.getDate(), request.getShift()
        );
        verifyNoInteractions(lactationPersistencePort);
        verifyNoInteractions(milkProductionMapper);
        verify(milkProductionPersistencePort, never()).save(any());
    }

    @Test
    void shouldThrowNoActiveLactationException_whenNoActiveLactation() {
        // Arrange
        Long farmId = 1L;
        String goatId = "1643218012";
        MilkProductionRequestVO request = validCreateVO();

        when(milkProductionPersistencePort.existsByFarmIdAndGoatIdAndDateAndShift(
                farmId, goatId, request.getDate(), request.getShift()
        )).thenReturn(false);

        when(lactationPersistencePort.findActiveByFarmIdAndGoatId(farmId, goatId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoActiveLactationException.class,
                () -> milkProductionBusiness.createMilkProduction(farmId, goatId, request));

        // Verify
        verify(milkProductionPersistencePort).existsByFarmIdAndGoatIdAndDateAndShift(
                farmId, goatId, request.getDate(), request.getShift()
        );
        verify(lactationPersistencePort).findActiveByFarmIdAndGoatId(farmId, goatId);
        verifyNoInteractions(milkProductionMapper);
        verify(milkProductionPersistencePort, never()).save(any());
    }

    // ==================================================================================
    // FIND BY ID
    // ==================================================================================

    @Test
    void shouldReturnMilkProduction_whenExistsInScope() {
        // Arrange
        Long farmId = 1L;
        String goatId = "1643218012";
        Long id = 1L;

        MilkProduction entity = validEntity();
        MilkProductionResponseVO expected = validResponseVO();

        when(milkProductionPersistencePort.findById(farmId, goatId, id))
                .thenReturn(Optional.of(entity));

        when(milkProductionMapper.toResponseVO(entity)).thenReturn(expected);

        // Act
        MilkProductionResponseVO result = milkProductionBusiness.findById(farmId, goatId, id);

        // Assert
        assertNotNull(result);
        assertEquals(expected.getId(), result.getId());

        // Verify
        verify(milkProductionPersistencePort).findById(farmId, goatId, id);
        verify(milkProductionMapper).toResponseVO(entity);
        verifyNoMoreInteractions(milkProductionPersistencePort, milkProductionMapper);
    }

    @Test
    void shouldThrowResourceNotFound_whenNotExistsInScope() {
        // Arrange
        Long farmId = 1L;
        String goatId = "1643218012";
        Long id = 99L;

        when(milkProductionPersistencePort.findById(farmId, goatId, id))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> milkProductionBusiness.findById(farmId, goatId, id));

        // Verify
        verify(milkProductionPersistencePort).findById(farmId, goatId, id);
        verifyNoInteractions(milkProductionMapper);
    }

    // ==================================================================================
    // UPDATE (PATCH)
    // ==================================================================================

    @Test
    void shouldUpdateOnlyVolumeLiters_whenOnlyVolumeProvided() {
        // Arrange
        Long farmId = 1L;
        String goatId = "1643218012";
        Long id = 5L;

        MilkProduction entityBefore = validEntity(); 
        BigDecimal newVolume = new BigDecimal("3.00");

        MilkProductionUpdateRequestVO updateVO = MilkProductionUpdateRequestVO.builder()
                .volumeLiters(newVolume)
                .notes(null)
                .build();

        MilkProduction savedEntity = validEntity();
        savedEntity.setVolumeLiters(newVolume);
        
        MilkProductionResponseVO expected = validResponseVO();
        expected.setVolumeLiters(newVolume);

        when(milkProductionPersistencePort.findById(farmId, goatId, id))
                .thenReturn(Optional.of(entityBefore));

        when(milkProductionPersistencePort.save(entityBefore))
                .thenReturn(savedEntity);

        when(milkProductionMapper.toResponseVO(savedEntity))
                .thenReturn(expected);

        // Act
        MilkProductionResponseVO result =
                milkProductionBusiness.update(farmId, goatId, id, updateVO);

        // Assert
        assertEquals(newVolume, entityBefore.getVolumeLiters());
        assertEquals("Ordenha da manhã", entityBefore.getNotes());

        assertNotNull(result);
        assertEquals(newVolume, result.getVolumeLiters());

        // Verify
        verify(milkProductionPersistencePort).findById(farmId, goatId, id);
        verify(milkProductionPersistencePort).save(entityBefore);
        verify(milkProductionMapper).toResponseVO(savedEntity);
        verifyNoMoreInteractions(milkProductionPersistencePort, milkProductionMapper);
    }

    @Test
    void shouldUpdateOnlyNotes_whenOnlyNotesProvided() {
        // Arrange
        Long farmId = 1L;
        String goatId = "1643218012";
        Long id = 5L;

        MilkProduction entityBefore = validEntity();
        BigDecimal oldVolume = entityBefore.getVolumeLiters();
        String newNotes = "Atualização de observação";

        MilkProductionUpdateRequestVO updateVO = MilkProductionUpdateRequestVO.builder()
                .volumeLiters(null)
                .notes(newNotes)
                .build();

        MilkProduction savedEntity = validEntity();
        savedEntity.setNotes(newNotes);
        savedEntity.setVolumeLiters(oldVolume);

        MilkProductionResponseVO expected = validResponseVO();
        expected.setNotes(newNotes);

        when(milkProductionPersistencePort.findById(farmId, goatId, id))
                .thenReturn(Optional.of(entityBefore));

        when(milkProductionPersistencePort.save(entityBefore))
                .thenReturn(savedEntity);

        when(milkProductionMapper.toResponseVO(savedEntity))
                .thenReturn(expected);

        // Act
        MilkProductionResponseVO result =
                milkProductionBusiness.update(farmId, goatId, id, updateVO);

        // Assert
        assertEquals(newNotes, entityBefore.getNotes());
        assertEquals(oldVolume, entityBefore.getVolumeLiters());

        assertNotNull(result);
        assertEquals(newNotes, result.getNotes());

        // Verify
        verify(milkProductionPersistencePort).findById(farmId, goatId, id);
        verify(milkProductionPersistencePort).save(entityBefore);
        verify(milkProductionMapper).toResponseVO(savedEntity);
        verifyNoMoreInteractions(milkProductionPersistencePort, milkProductionMapper);
    }

    @Test
    void shouldUpdateVolumeAndNotes_whenBothProvided() {
        // Arrange
        Long farmId = 1L;
        String goatId = "1643218012";
        Long id = 5L;

        MilkProduction entityBefore = validEntity();

        BigDecimal newVolume = new BigDecimal("3.20");
        String newNotes = "Atualização de volume e observação";

        MilkProductionUpdateRequestVO updateVO = MilkProductionUpdateRequestVO.builder()
                .volumeLiters(newVolume)
                .notes(newNotes)
                .build();

        when(milkProductionPersistencePort.findById(farmId, goatId, id))
                .thenReturn(Optional.of(entityBefore));

        MilkProduction savedEntity = validEntity();
        savedEntity.setVolumeLiters(newVolume);
        savedEntity.setNotes(newNotes);

        when(milkProductionPersistencePort.save(entityBefore))
                .thenReturn(savedEntity);

        MilkProductionResponseVO expected = validResponseVO();
        expected.setVolumeLiters(newVolume);
        expected.setNotes(newNotes);

        when(milkProductionMapper.toResponseVO(savedEntity))
                .thenReturn(expected);

        // Act
        MilkProductionResponseVO result =
                milkProductionBusiness.update(farmId, goatId, id, updateVO);

        // Assert
        assertEquals(newVolume, entityBefore.getVolumeLiters());
        assertEquals(newNotes, entityBefore.getNotes());

        assertNotNull(result);
        assertEquals(newVolume, result.getVolumeLiters());
        assertEquals(newNotes, result.getNotes());

        // Verify
        verify(milkProductionPersistencePort).findById(farmId, goatId, id);
        verify(milkProductionPersistencePort).save(entityBefore);
        verify(milkProductionMapper).toResponseVO(savedEntity);
        verifyNoMoreInteractions(milkProductionPersistencePort, milkProductionMapper);
    }

    @Test
    void shouldThrowResourceNotFound_whenUpdatingNotExists() {
        // Arrange
        Long farmId = 1L;
        String goatId = "1643218012";
        Long id = 999L;

        MilkProductionUpdateRequestVO updateVO = MilkProductionUpdateRequestVO.builder()
                .volumeLiters(new BigDecimal("3.00"))
                .notes("qualquer")
                .build();

        when(milkProductionPersistencePort.findById(farmId, goatId, id))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> milkProductionBusiness.update(farmId, goatId, id, updateVO));

        // Verify
        verify(milkProductionPersistencePort).findById(farmId, goatId, id);
        verify(milkProductionPersistencePort, never()).save(any());
        verifyNoInteractions(milkProductionMapper);
    }


    // ==================================================================================
    // DELETE
    // ==================================================================================

    @Test
    void shouldDeleteMilkProduction_whenExistsInScope() {
        // Arrange
        Long farmId = 1L;
        String goatId = "1643218012";
        Long id = 5L;

        MilkProduction entity = validEntity();

        when(milkProductionPersistencePort.findById(farmId, goatId, id))
                .thenReturn(Optional.of(entity));

        // Act
        milkProductionBusiness.delete(farmId, goatId, id);

        // Verify
        verify(milkProductionPersistencePort).findById(farmId, goatId, id);
        verify(milkProductionPersistencePort).delete(entity);
        verifyNoMoreInteractions(milkProductionPersistencePort);
        verifyNoInteractions(milkProductionMapper, lactationPersistencePort);
    }

    @Test
    void shouldThrowResourceNotFound_whenDeletingNotExists() {
        // Arrange
        Long farmId = 1L;
        String goatId = "1643218012";
        Long id = 999L;

        when(milkProductionPersistencePort.findById(farmId, goatId, id))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> milkProductionBusiness.delete(farmId, goatId, id));

        // Verify
        verify(milkProductionPersistencePort).findById(farmId, goatId, id);
        verify(milkProductionPersistencePort, never()).delete(any());
        verifyNoInteractions(milkProductionMapper, lactationPersistencePort);
    }


    // ==================================================================================
    // HELPERS
    // ==================================================================================

    private MilkProduction validEntity() {
        MilkProduction entity = new MilkProduction();
        entity.setId(5L);
        entity.setDate(LocalDate.of(2026, 1, 1));
        entity.setShift(MilkingShift.MORNING);
        entity.setVolumeLiters(new BigDecimal("2.50"));
        entity.setNotes("Ordenha da manhã");
        return entity;
    }

    private MilkProductionRequestVO validCreateVO() {
        return MilkProductionRequestVO.builder()
                .date(LocalDate.of(2026, 1, 1))
                .shift(MilkingShift.MORNING)
                .volumeLiters(new BigDecimal("2.50"))
                .notes("Ordenha da manhã")
                .build();
    }

    private MilkProductionResponseVO validResponseVO() {
        return MilkProductionResponseVO.builder()
                .id(5L)
                .date(LocalDate.of(2026, 1, 1))
                .shift(MilkingShift.MORNING)
                .volumeLiters(new BigDecimal("2.50"))
                .notes("Ordenha da manhã")
                .build();
    }

    // Testar data futura
    private MilkProductionRequestVO createVOWithFutureDate() {
        return MilkProductionRequestVO.builder()
                .date(LocalDate.now().plusDays(1))
                .shift(MilkingShift.MORNING)
                .volumeLiters(new BigDecimal("2.50"))
                .notes("Ordenha da manhã")
                .build();
    }


    private MilkProductionUpdateRequestVO validUpdateVO() {
        // Retorna um VO de atualização válido
        return null;
    }
}
