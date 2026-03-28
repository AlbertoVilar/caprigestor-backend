package com.devmaster.goatfarm.milk.business.lactationservice;

import com.devmaster.goatfarm.application.core.business.validation.GoatGenderValidator;
import com.devmaster.goatfarm.config.exceptions.custom.BusinessRuleException;
import com.devmaster.goatfarm.config.exceptions.custom.InvalidArgumentException;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.goat.persistence.entity.Goat;
import com.devmaster.goatfarm.milk.application.ports.out.LactationPersistencePort;
import com.devmaster.goatfarm.milk.application.ports.out.MilkProductionPersistencePort;
import com.devmaster.goatfarm.milk.application.ports.out.PregnancySnapshotQueryPort;
import com.devmaster.goatfarm.milk.business.bo.LactationDryOffAlertVO;
import com.devmaster.goatfarm.milk.business.bo.LactationDryRequestVO;
import com.devmaster.goatfarm.milk.business.bo.LactationRequestVO;
import com.devmaster.goatfarm.milk.business.bo.LactationResponseVO;
import com.devmaster.goatfarm.milk.business.bo.LactationSummaryResponseVO;
import com.devmaster.goatfarm.milk.business.mapper.LactationBusinessMapper;
import com.devmaster.goatfarm.milk.enums.LactationStatus;
import com.devmaster.goatfarm.milk.persistence.entity.Lactation;
import com.devmaster.goatfarm.milk.persistence.projection.LactationDryOffAlertProjection;
import com.devmaster.goatfarm.sharedkernel.pregnancy.PregnancySnapshot;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LactationBusinessTest {

    @Mock
    private LactationPersistencePort lactationPersistencePort;

    @Mock
    private MilkProductionPersistencePort milkProductionPersistencePort;

    @Mock
    private PregnancySnapshotQueryPort pregnancySnapshotQueryPort;

    @Mock
    private GoatGenderValidator goatGenderValidator;

    @Mock
    private LactationBusinessMapper lactationMapper;

    @InjectMocks
    private LactationBusiness lactationBusiness;

    @BeforeEach
    void setUp() {
        lenient().when(goatGenderValidator.requireFemale(anyLong(), anyString())).thenReturn(new Goat());
        lenient().when(goatGenderValidator.requireFemaleAndActive(anyLong(), anyString())).thenReturn(new Goat());
    }

    @Test
    void openLactation_shouldCreateActiveLactation_whenNoActiveExists() {
        Long farmId = 1L;
        String goatId = "123";
        LactationRequestVO requestVO = validRequestVO();

        when(lactationPersistencePort.findActiveByFarmIdAndGoatId(farmId, goatId))
                .thenReturn(Optional.empty());
        when(lactationPersistencePort.findAllByFarmIdAndGoatId(eq(farmId), eq(goatId), any(Pageable.class)))
                .thenReturn(Page.empty());
        when(pregnancySnapshotQueryPort.findLatestByFarmIdAndGoatId(farmId, goatId, requestVO.getStartDate()))
                .thenReturn(Optional.empty());

        Lactation savedEntity = savedLactationEntity();
        savedEntity.setFarmId(farmId);
        savedEntity.setGoatId(goatId);

        when(lactationPersistencePort.save(any(Lactation.class))).thenReturn(savedEntity);

        LactationResponseVO expectedVO = responseVO();
        when(lactationMapper.toResponseVO(savedEntity)).thenReturn(expectedVO);

        ArgumentCaptor<Lactation> captor = ArgumentCaptor.forClass(Lactation.class);

        LactationResponseVO result = lactationBusiness.openLactation(farmId, goatId, requestVO);

        assertNotNull(result);
        assertEquals(expectedVO.getId(), result.getId());
        assertEquals(expectedVO.getStatus(), result.getStatus());

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
        Long farmId = 1L;
        String goatId = "1643218012";

        LactationRequestVO requestVO = validRequestVO();
        Lactation activeEntity = activeLactationEntity();

        when(lactationPersistencePort.findActiveByFarmIdAndGoatId(farmId, goatId))
                .thenReturn(Optional.of(activeEntity));

        assertThrows(BusinessRuleException.class,
                () -> lactationBusiness.openLactation(farmId, goatId, requestVO));

        verify(lactationPersistencePort).findActiveByFarmIdAndGoatId(farmId, goatId);
        verify(lactationPersistencePort, never()).save(any(Lactation.class));
        verifyNoInteractions(lactationMapper);
    }

    @Test
    void openLactation_shouldThrowValidationException_whenLatestLactationIsDryAndPregnancyStillActive() {
        Long farmId = 1L;
        String goatId = "123";
        LactationRequestVO requestVO = validRequestVO();

        Lactation dryLactation = new Lactation();
        dryLactation.setId(20L);
        dryLactation.setFarmId(farmId);
        dryLactation.setGoatId(goatId);
        dryLactation.setStatus(LactationStatus.DRY);
        dryLactation.setStartDate(LocalDate.of(2025, 11, 1));
        dryLactation.setEndDate(LocalDate.of(2026, 3, 28));
        dryLactation.setDryStartDate(LocalDate.of(2026, 3, 28));

        when(lactationPersistencePort.findActiveByFarmIdAndGoatId(farmId, goatId))
                .thenReturn(Optional.empty());
        when(lactationPersistencePort.findAllByFarmIdAndGoatId(eq(farmId), eq(goatId), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(dryLactation)));
        when(pregnancySnapshotQueryPort.findLatestByFarmIdAndGoatId(farmId, goatId, requestVO.getStartDate()))
                .thenReturn(Optional.of(new PregnancySnapshot(
                        true,
                        LocalDate.of(2025, 12, 28),
                        LocalDate.of(2026, 3, 1),
                        null
                )));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> lactationBusiness.openLactation(farmId, goatId, requestVO));

        assertEquals("Nao e permitido abrir nova lactacao enquanto houver prenhez ativa apos secagem confirmada.", ex.getMessage());
        verify(lactationPersistencePort, never()).save(any(Lactation.class));
    }

    @Test
    void openLactation_shouldThrowValidationException_whenStartDateIsInFuture() {
        Long farmId = 1L;
        String goatId = "123";

        LactationRequestVO futureRequest = new LactationRequestVO();
        futureRequest.setStartDate(LocalDate.now().plusDays(1));

        InvalidArgumentException ex = assertThrows(InvalidArgumentException.class,
                () -> lactationBusiness.openLactation(farmId, goatId, futureRequest));

        assertEquals("startDate", ex.getFieldName());
        verifyNoInteractions(lactationPersistencePort);
        verifyNoInteractions(lactationMapper);
    }

    @Test
    void dryLactation_shouldMarkLactationAsDry_whenValidRequest() {
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
                .thenAnswer(invocation -> invocation.getArgument(0));

        LactationResponseVO expectedVO = responseVO();
        expectedVO.setStatus(LactationStatus.DRY);
        expectedVO.setEndDate(endDate);
        when(lactationMapper.toResponseVO(any(Lactation.class))).thenReturn(expectedVO);

        LactationResponseVO result = lactationBusiness.dryLactation(farmId, goatId, lactationId, dryRequestVO);

        assertNotNull(result);
        assertEquals(LactationStatus.DRY, result.getStatus());
        assertEquals(endDate, result.getEndDate());

        ArgumentCaptor<Lactation> captor = ArgumentCaptor.forClass(Lactation.class);
        verify(lactationPersistencePort).save(captor.capture());

        Lactation savedEntity = captor.getValue();
        assertEquals(LactationStatus.DRY, savedEntity.getStatus());
        assertEquals(endDate, savedEntity.getEndDate());
        assertEquals(endDate, savedEntity.getDryStartDate());
    }

    @Test
    void dryLactation_shouldThrowResourceNotFound_whenLactationNotFound() {
        Long farmId = 1L;
        String goatId = "123";
        Long lactationId = 999L;
        LactationDryRequestVO dryRequestVO = new LactationDryRequestVO();

        when(lactationPersistencePort.findByIdAndFarmIdAndGoatId(lactationId, farmId, goatId))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> lactationBusiness.dryLactation(farmId, goatId, lactationId, dryRequestVO));

        verify(lactationPersistencePort, never()).save(any());
    }

    @Test
    void dryLactation_shouldThrowValidationException_whenLactationNotActive() {
        Long farmId = 1L;
        String goatId = "123";
        Long lactationId = 10L;

        Lactation closedLactation = new Lactation();
        closedLactation.setStatus(LactationStatus.CLOSED);

        when(lactationPersistencePort.findByIdAndFarmIdAndGoatId(lactationId, farmId, goatId))
                .thenReturn(Optional.of(closedLactation));

        LactationDryRequestVO dryRequestVO = new LactationDryRequestVO();

        assertThrows(BusinessRuleException.class,
                () -> lactationBusiness.dryLactation(farmId, goatId, lactationId, dryRequestVO));
    }

    @Test
    void dryLactation_shouldThrowValidationException_whenEndDateBeforeStartDate() {
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
        dryRequestVO.setEndDate(startDate.minusDays(1));

        assertThrows(BusinessRuleException.class,
                () -> lactationBusiness.dryLactation(farmId, goatId, lactationId, dryRequestVO));
    }

    @Test
    void resumeLactation_shouldReactivateDryLactation_whenPregnancyIsClosedWithoutBirth() {
        Long farmId = 1L;
        String goatId = "123";
        Long lactationId = 10L;

        Lactation dryLactation = new Lactation();
        dryLactation.setId(lactationId);
        dryLactation.setFarmId(farmId);
        dryLactation.setGoatId(goatId);
        dryLactation.setStatus(LactationStatus.DRY);
        dryLactation.setStartDate(LocalDate.of(2025, 11, 15));
        dryLactation.setEndDate(LocalDate.of(2026, 3, 28));
        dryLactation.setDryStartDate(LocalDate.of(2026, 3, 28));

        when(lactationPersistencePort.findByIdAndFarmIdAndGoatId(lactationId, farmId, goatId))
                .thenReturn(Optional.of(dryLactation));
        when(lactationPersistencePort.findActiveByFarmIdAndGoatId(farmId, goatId))
                .thenReturn(Optional.empty());
        when(pregnancySnapshotQueryPort.findLatestByFarmIdAndGoatId(eq(farmId), eq(goatId), any(LocalDate.class)))
                .thenReturn(Optional.of(new PregnancySnapshot(
                        false,
                        LocalDate.of(2025, 12, 28),
                        LocalDate.of(2026, 3, 1),
                        "FALSE_POSITIVE"
                )));
        when(lactationPersistencePort.save(any(Lactation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(lactationMapper.toResponseVO(any(Lactation.class))).thenReturn(responseVO());

        LactationResponseVO result = lactationBusiness.resumeLactation(farmId, goatId, lactationId);

        assertNotNull(result);
        ArgumentCaptor<Lactation> captor = ArgumentCaptor.forClass(Lactation.class);
        verify(lactationPersistencePort).save(captor.capture());
        Lactation savedEntity = captor.getValue();
        assertEquals(LactationStatus.ACTIVE, savedEntity.getStatus());
        assertNull(savedEntity.getEndDate());
        assertNull(savedEntity.getDryStartDate());
    }

    @Test
    void resumeLactation_shouldThrowValidationException_whenPregnancyIsStillActive() {
        Long farmId = 1L;
        String goatId = "123";
        Long lactationId = 10L;

        Lactation dryLactation = new Lactation();
        dryLactation.setId(lactationId);
        dryLactation.setStatus(LactationStatus.DRY);

        when(lactationPersistencePort.findByIdAndFarmIdAndGoatId(lactationId, farmId, goatId))
                .thenReturn(Optional.of(dryLactation));
        when(lactationPersistencePort.findActiveByFarmIdAndGoatId(farmId, goatId))
                .thenReturn(Optional.empty());
        when(pregnancySnapshotQueryPort.findLatestByFarmIdAndGoatId(eq(farmId), eq(goatId), any(LocalDate.class)))
                .thenReturn(Optional.of(new PregnancySnapshot(
                        true,
                        LocalDate.of(2025, 12, 28),
                        LocalDate.of(2026, 3, 1),
                        null
                )));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> lactationBusiness.resumeLactation(farmId, goatId, lactationId));

        assertEquals("Nao e permitido retomar lactacao com prenhez ativa.", ex.getMessage());
        verify(lactationPersistencePort, never()).save(any(Lactation.class));
    }

    @Test
    void resumeLactation_shouldThrowValidationException_whenPregnancyClosedWithBirth() {
        Long farmId = 1L;
        String goatId = "123";
        Long lactationId = 10L;

        Lactation dryLactation = new Lactation();
        dryLactation.setId(lactationId);
        dryLactation.setStatus(LactationStatus.DRY);

        when(lactationPersistencePort.findByIdAndFarmIdAndGoatId(lactationId, farmId, goatId))
                .thenReturn(Optional.of(dryLactation));
        when(lactationPersistencePort.findActiveByFarmIdAndGoatId(farmId, goatId))
                .thenReturn(Optional.empty());
        when(pregnancySnapshotQueryPort.findLatestByFarmIdAndGoatId(eq(farmId), eq(goatId), any(LocalDate.class)))
                .thenReturn(Optional.of(new PregnancySnapshot(
                        false,
                        LocalDate.of(2025, 12, 28),
                        LocalDate.of(2026, 3, 1),
                        "BIRTH"
                )));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> lactationBusiness.resumeLactation(farmId, goatId, lactationId));

        assertEquals("Nao e permitido retomar lactacao apos parto. Inicie uma nova lactacao para o novo ciclo.", ex.getMessage());
        verify(lactationPersistencePort, never()).save(any(Lactation.class));
    }

    @Test
    void getActiveLactation_shouldReturnLactation_whenExists() {
        Long farmId = 1L;
        String goatId = "123";
        Lactation activeLactation = activeLactationEntity();
        LactationResponseVO expectedVO = responseVO();

        when(lactationPersistencePort.findActiveByFarmIdAndGoatId(farmId, goatId))
                .thenReturn(Optional.of(activeLactation));
        when(lactationMapper.toResponseVO(activeLactation)).thenReturn(expectedVO);

        LactationResponseVO result = lactationBusiness.getActiveLactation(farmId, goatId);

        assertNotNull(result);
        assertEquals(expectedVO.getId(), result.getId());
    }

    @Test
    void getActiveLactation_shouldThrowResourceNotFound_whenNotExists() {
        Long farmId = 1L;
        String goatId = "123";
        when(lactationPersistencePort.findActiveByFarmIdAndGoatId(farmId, goatId))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> lactationBusiness.getActiveLactation(farmId, goatId));
    }

    @Test
    void getLactationById_shouldReturnLactation_whenExists() {
        Long farmId = 1L;
        String goatId = "123";
        Long lactationId = 10L;
        Lactation lactation = activeLactationEntity();
        LactationResponseVO expectedVO = responseVO();

        when(lactationPersistencePort.findByIdAndFarmIdAndGoatId(lactationId, farmId, goatId))
                .thenReturn(Optional.of(lactation));
        when(lactationMapper.toResponseVO(lactation)).thenReturn(expectedVO);

        LactationResponseVO result = lactationBusiness.getLactationById(farmId, goatId, lactationId);

        assertNotNull(result);
        assertEquals(expectedVO.getId(), result.getId());
    }

    @Test
    void getAllLactations_shouldReturnPageOfLactations() {
        Long farmId = 1L;
        String goatId = "123";
        Pageable pageable = PageRequest.of(0, 10);

        List<Lactation> lactationList = List.of(activeLactationEntity());
        Page<Lactation> lactationPage = new PageImpl<>(lactationList);

        LactationResponseVO responseVO = responseVO();

        when(lactationPersistencePort.findAllByFarmIdAndGoatId(farmId, goatId, pageable))
                .thenReturn(lactationPage);
        when(lactationMapper.toResponseVO(any(Lactation.class))).thenReturn(responseVO);

        Page<LactationResponseVO> result = lactationBusiness.getAllLactations(farmId, goatId, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(responseVO.getId(), result.getContent().get(0).getId());
    }

    @Test
    void getLactationSummary_shouldRecommendDryOff_whenPregnancyIsActiveAndThresholdReached() {
        Long farmId = 1L;
        String goatId = "123";
        Long lactationId = 10L;
        LocalDate breedingDate = LocalDate.now().minusDays(100);

        Lactation lactation = activeLactationEntity();
        lactation.setId(lactationId);
        lactation.setFarmId(farmId);
        lactation.setGoatId(goatId);
        lactation.setDryAtPregnancyDays(90);

        when(lactationPersistencePort.findByIdAndFarmIdAndGoatId(lactationId, farmId, goatId))
                .thenReturn(Optional.of(lactation));
        when(milkProductionPersistencePort.findByFarmIdAndGoatIdAndDateBetween(
                eq(farmId), eq(goatId), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of());
        when(pregnancySnapshotQueryPort.findLatestByFarmIdAndGoatId(eq(farmId), eq(goatId), any(LocalDate.class)))
                .thenReturn(Optional.of(new PregnancySnapshot(true, breedingDate, breedingDate.plusDays(10))));

        LactationSummaryResponseVO result = lactationBusiness.getLactationSummary(farmId, goatId, lactationId);

        assertNotNull(result);
        assertNotNull(result.getPregnancy());
        assertTrue(Boolean.TRUE.equals(result.getPregnancy().getDryOffRecommendation()));
        assertEquals(breedingDate.plusDays(90), result.getPregnancy().getRecommendedDryOffDate());
    }

    @Test
    void getLactationSummary_shouldDropDryOffRecommendation_whenPregnancyChangesFromActiveToInactive() {
        Long farmId = 1L;
        String goatId = "123";
        Long lactationId = 10L;
        LocalDate breedingDate = LocalDate.now().minusDays(100);

        Lactation lactation = activeLactationEntity();
        lactation.setId(lactationId);
        lactation.setFarmId(farmId);
        lactation.setGoatId(goatId);
        lactation.setDryAtPregnancyDays(90);

        when(lactationPersistencePort.findByIdAndFarmIdAndGoatId(lactationId, farmId, goatId))
                .thenReturn(Optional.of(lactation));
        when(milkProductionPersistencePort.findByFarmIdAndGoatIdAndDateBetween(
                eq(farmId), eq(goatId), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of());
        when(pregnancySnapshotQueryPort.findLatestByFarmIdAndGoatId(eq(farmId), eq(goatId), any(LocalDate.class)))
                .thenReturn(
                        Optional.of(new PregnancySnapshot(true, breedingDate, breedingDate.plusDays(10))),
                        Optional.of(new PregnancySnapshot(false, breedingDate, breedingDate.plusDays(10)))
                );

        LactationSummaryResponseVO positiveSnapshot = lactationBusiness.getLactationSummary(farmId, goatId, lactationId);
        LactationSummaryResponseVO negativeSnapshot = lactationBusiness.getLactationSummary(farmId, goatId, lactationId);

        assertNotNull(positiveSnapshot.getPregnancy());
        assertTrue(Boolean.TRUE.equals(positiveSnapshot.getPregnancy().getDryOffRecommendation()));

        assertNotNull(negativeSnapshot.getPregnancy());
        assertFalse(Boolean.TRUE.equals(negativeSnapshot.getPregnancy().getDryOffRecommendation()));
        assertNull(negativeSnapshot.getPregnancy().getRecommendedDryOffDate());
        assertTrue(negativeSnapshot.getPregnancy().getMessage().contains("prenhez ativa"));
    }

    @Test
    void getDryOffAlerts_shouldMapProjectionAndCalculateOverdueDays() {
        Long farmId = 1L;
        LocalDate referenceDate = LocalDate.of(2026, 2, 1);
        Pageable pageable = PageRequest.of(0, 10);

        LactationDryOffAlertProjection projection = mock(LactationDryOffAlertProjection.class);
        when(projection.getLactationId()).thenReturn(11L);
        when(projection.getGoatId()).thenReturn("GOAT-001");
        when(projection.getStartDatePregnancy()).thenReturn(LocalDate.of(2025, 10, 20));
        when(projection.getBreedingDate()).thenReturn(LocalDate.of(2025, 10, 20));
        when(projection.getConfirmDate()).thenReturn(LocalDate.of(2025, 12, 20));
        when(projection.getDryOffDate()).thenReturn(LocalDate.of(2026, 1, 18));
        when(projection.getDryAtPregnancyDays()).thenReturn(90);

        when(lactationPersistencePort.findDryOffAlerts(farmId, referenceDate, 90, pageable))
                .thenReturn(new PageImpl<>(List.of(projection), pageable, 1));

        Page<LactationDryOffAlertVO> result = lactationBusiness.getDryOffAlerts(farmId, referenceDate, pageable);

        assertEquals(1, result.getTotalElements());
        LactationDryOffAlertVO alert = result.getContent().get(0);
        assertEquals("GOAT-001", alert.getGoatId());
        assertEquals(104, alert.getGestationDays());
        assertEquals(14, alert.getDaysOverdue());
        assertTrue(alert.isDryOffRecommendation());
        assertEquals(LocalDate.of(2026, 1, 18), alert.getDryOffDate());
    }

    @Test
    void getDryOffAlerts_shouldKeepOverdueAtZero_whenReferenceDateIsBeforeDryOffDate() {
        Long farmId = 1L;
        LocalDate referenceDate = LocalDate.of(2026, 1, 10);
        Pageable pageable = PageRequest.of(0, 10);

        LactationDryOffAlertProjection projection = mock(LactationDryOffAlertProjection.class);
        when(projection.getLactationId()).thenReturn(22L);
        when(projection.getGoatId()).thenReturn("GOAT-002");
        when(projection.getStartDatePregnancy()).thenReturn(LocalDate.of(2025, 12, 1));
        when(projection.getBreedingDate()).thenReturn(LocalDate.of(2025, 12, 1));
        when(projection.getConfirmDate()).thenReturn(LocalDate.of(2026, 1, 1));
        when(projection.getDryOffDate()).thenReturn(LocalDate.of(2026, 3, 1));
        when(projection.getDryAtPregnancyDays()).thenReturn(90);

        when(lactationPersistencePort.findDryOffAlerts(farmId, referenceDate, 90, pageable))
                .thenReturn(new PageImpl<>(List.of(projection), pageable, 1));

        Page<LactationDryOffAlertVO> result = lactationBusiness.getDryOffAlerts(farmId, referenceDate, pageable);

        assertEquals(1, result.getTotalElements());
        LactationDryOffAlertVO alert = result.getContent().get(0);
        assertEquals(40, alert.getGestationDays());
        assertEquals(0, alert.getDaysOverdue());
        assertFalse(alert.isDryOffRecommendation());
    }

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


