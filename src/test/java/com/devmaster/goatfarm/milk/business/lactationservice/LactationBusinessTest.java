package com.devmaster.goatfarm.milk.business.lactationservice;

import com.devmaster.goatfarm.application.core.business.validation.GoatGenderValidator;
import com.devmaster.goatfarm.config.exceptions.custom.BusinessRuleException;
import com.devmaster.goatfarm.config.exceptions.custom.InvalidArgumentException;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.milk.application.ports.out.LactationPersistencePort;
import com.devmaster.goatfarm.milk.application.ports.out.MilkProductionSummaryQueryPort;
import com.devmaster.goatfarm.reproduction.application.ports.in.PregnancySnapshotQueryUseCase;
import com.devmaster.goatfarm.reproduction.application.ports.in.PregnancyDryOffQueryUseCase;
import com.devmaster.goatfarm.reproduction.application.model.PregnancyDryOffSnapshot;
import com.devmaster.goatfarm.milk.business.bo.LactationDryOffAlertVO;
import com.devmaster.goatfarm.milk.business.bo.LactationDryRequestVO;
import com.devmaster.goatfarm.milk.business.bo.LactationRequestVO;
import com.devmaster.goatfarm.milk.business.bo.LactationResponseVO;
import com.devmaster.goatfarm.milk.business.bo.LactationSummaryResponseVO;
import com.devmaster.goatfarm.milk.business.mapper.LactationBusinessMapper;
import com.devmaster.goatfarm.milk.enums.LactationStatus;
import com.devmaster.goatfarm.milk.domain.Lactation;
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
    private MilkProductionSummaryQueryPort milkProductionSummaryQueryPort;

    @Mock
    private PregnancySnapshotQueryUseCase pregnancySnapshotQueryPort;

    @Mock
    private PregnancyDryOffQueryUseCase pregnancyDryOffQueryUseCase;

    @Mock
    private GoatGenderValidator goatGenderValidator;

    @Mock
    private LactationBusinessMapper lactationMapper;

    @InjectMocks
    private LactationBusiness lactationBusiness;

    @BeforeEach
    void setUp() {
        lenient().doNothing().when(goatGenderValidator).requireFemale(anyLong(), anyString());
        lenient().doNothing().when(goatGenderValidator).requireFemaleAndActive(anyLong(), anyString());
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

        Lactation dryLactation = Lactation.rehydrate(20L, farmId, goatId, null, LactationStatus.DRY,
                LocalDate.of(2025, 11, 1), LocalDate.of(2026, 3, 28), null,
                LocalDate.of(2026, 3, 28), 90, 60, null, null);

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

        Lactation existingLactation = Lactation.rehydrate(lactationId, farmId, goatId, null,
                LactationStatus.ACTIVE, startDate, null, null, null, 90, 60, null, null);

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

        Lactation closedLactation = Lactation.rehydrate(10L, farmId, goatId, null, LactationStatus.CLOSED,
                LocalDate.of(2026, 1, 1), null, null, null, 90, 60, null, null);

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

        Lactation activeLactation = Lactation.rehydrate(10L, farmId, goatId, null, LactationStatus.ACTIVE,
                startDate, null, null, null, 90, 60, null, null);

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

        Lactation dryLactation = Lactation.rehydrate(lactationId, farmId, goatId, null, LactationStatus.DRY,
                LocalDate.of(2025, 11, 15), LocalDate.of(2026, 3, 28), null,
                LocalDate.of(2026, 3, 28), 90, 60, null, null);

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

        Lactation dryLactation = Lactation.rehydrate(lactationId, farmId, goatId, null, LactationStatus.DRY,
                LocalDate.of(2026, 1, 1), null, null, null, 90, 60, null, null);

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

        Lactation dryLactation = Lactation.rehydrate(lactationId, farmId, goatId, null, LactationStatus.DRY,
                LocalDate.of(2026, 1, 1), null, null, null, 90, 60, null, null);

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

        Lactation lactation = Lactation.rehydrate(lactationId, farmId, goatId, null, LactationStatus.ACTIVE,
                LocalDate.of(2026, 1, 1), null, null, null, 90, 60, null, null);

        when(lactationPersistencePort.findByIdAndFarmIdAndGoatId(lactationId, farmId, goatId))
                .thenReturn(Optional.of(lactation));
        when(milkProductionSummaryQueryPort.findSummaryByFarmIdAndGoatIdAndDateBetween(
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

        Lactation lactation = Lactation.rehydrate(lactationId, farmId, goatId, null, LactationStatus.ACTIVE,
                LocalDate.of(2026, 1, 1), null, null, null, 90, 60, null, null);

        when(lactationPersistencePort.findByIdAndFarmIdAndGoatId(lactationId, farmId, goatId))
                .thenReturn(Optional.of(lactation));
        when(milkProductionSummaryQueryPort.findSummaryByFarmIdAndGoatIdAndDateBetween(
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

        when(lactationPersistencePort.findAllActiveByFarmId(farmId)).thenReturn(List.of(
                Lactation.rehydrate(11L, farmId, "GOAT-001", null, LactationStatus.ACTIVE,
                        LocalDate.of(2025, 10, 1), null, null, null, 90, 60, null, null)));
        when(pregnancyDryOffQueryUseCase.findLatestRelevantByFarmId(farmId, referenceDate)).thenReturn(List.of(
                new PregnancyDryOffSnapshot(1L, farmId, null, "GOAT-001", "ACTIVE",
                        LocalDate.of(2025, 10, 20), LocalDate.of(2025, 12, 20),
                        LocalDate.of(2025, 10, 20), null)));

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

        when(lactationPersistencePort.findAllActiveByFarmId(farmId)).thenReturn(List.of(
                Lactation.rehydrate(22L, farmId, "GOAT-002", null, LactationStatus.ACTIVE,
                        LocalDate.of(2025, 11, 1), null, null, null, 90, 60, null, null)));
        when(pregnancyDryOffQueryUseCase.findLatestRelevantByFarmId(farmId, referenceDate)).thenReturn(List.of(
                new PregnancyDryOffSnapshot(2L, farmId, null, "GOAT-002", "ACTIVE",
                        LocalDate.of(2025, 12, 1), LocalDate.of(2026, 1, 1),
                        LocalDate.of(2025, 12, 1), null)));

        Page<LactationDryOffAlertVO> result = lactationBusiness.getDryOffAlerts(farmId, referenceDate, pageable);

        assertEquals(0, result.getTotalElements());
    }

    private LactationRequestVO validRequestVO() {
        return LactationRequestVO.builder()
                .startDate(LocalDate.of(2026, 1, 1))
                .build();
    }

    private Lactation activeLactationEntity() {
        return Lactation.rehydrate(10L, 1L, "123", null, LactationStatus.ACTIVE,
                LocalDate.of(2026, 1, 1), null, null, null, 90, 60, null, null);
    }

    private Lactation savedLactationEntity() {
        return Lactation.rehydrate(11L, 1L, "123", null, LactationStatus.ACTIVE,
                LocalDate.of(2026, 1, 1), null, null, null, 90, 60, null, null);
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
