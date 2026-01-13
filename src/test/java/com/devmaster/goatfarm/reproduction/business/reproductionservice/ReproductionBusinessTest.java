package com.devmaster.goatfarm.reproduction.business.reproductionservice;

import com.devmaster.goatfarm.application.ports.out.PregnancyPersistencePort;
import com.devmaster.goatfarm.application.ports.out.ReproductiveEventPersistencePort;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.config.exceptions.custom.ValidationException;
import com.devmaster.goatfarm.reproduction.business.bo.BreedingRequestVO;
import com.devmaster.goatfarm.reproduction.business.bo.PregnancyCloseRequestVO;
import com.devmaster.goatfarm.reproduction.business.bo.PregnancyConfirmRequestVO;
import com.devmaster.goatfarm.reproduction.business.bo.PregnancyResponseVO;
import com.devmaster.goatfarm.reproduction.business.bo.ReproductiveEventResponseVO;
import com.devmaster.goatfarm.reproduction.enums.BreedingType;
import com.devmaster.goatfarm.reproduction.enums.PregnancyCheckResult;
import com.devmaster.goatfarm.reproduction.enums.PregnancyCloseReason;
import com.devmaster.goatfarm.reproduction.enums.PregnancyStatus;
import com.devmaster.goatfarm.reproduction.enums.ReproductiveEventType;
import com.devmaster.goatfarm.reproduction.mapper.ReproductionMapper;
import com.devmaster.goatfarm.reproduction.model.entity.Pregnancy;
import com.devmaster.goatfarm.reproduction.model.entity.ReproductiveEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReproductionBusinessTest {

    @Mock
    private PregnancyPersistencePort pregnancyPersistencePort;

    @Mock
    private ReproductiveEventPersistencePort reproductiveEventPersistencePort;

    @Mock
    private ReproductionMapper reproductionMapper;

    @InjectMocks
    private ReproductionBusiness reproductionBusiness;

    private static final Long FARM_ID = 1L;
    private static final String GOAT_ID = "1643218012";

    @BeforeEach
    void setUp() {
        // Maintained to follow Milk module test style.
    }

    // ==================================================================================
    // REGISTER BREEDING
    // ==================================================================================

    @Test
    void registerBreeding_shouldCreateCoverageEvent_whenValidRequest() {
        // Arrange
        BreedingRequestVO requestVO = validBreedingRequestVO();

        ReproductiveEvent savedEvent = coverageEventEntity();
        when(reproductiveEventPersistencePort.save(any(ReproductiveEvent.class))).thenReturn(savedEvent);

        ReproductiveEventResponseVO responseVO = reproductiveEventResponseVO();
        when(reproductionMapper.toReproductiveEventResponseVO(savedEvent)).thenReturn(responseVO);

        // Act
        ReproductiveEventResponseVO result = reproductionBusiness.registerBreeding(FARM_ID, GOAT_ID, requestVO);

        // Assert
        ArgumentCaptor<ReproductiveEvent> eventCaptor = ArgumentCaptor.forClass(ReproductiveEvent.class);
        verify(reproductiveEventPersistencePort).save(eventCaptor.capture());

        ReproductiveEvent capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.getFarmId()).isEqualTo(FARM_ID);
        assertThat(capturedEvent.getGoatId()).isEqualTo(GOAT_ID);
        assertThat(capturedEvent.getEventType()).isEqualTo(ReproductiveEventType.COVERAGE);
        assertThat(capturedEvent.getEventDate()).isEqualTo(requestVO.getEventDate());
        assertThat(capturedEvent.getBreedingType()).isEqualTo(requestVO.getBreedingType());

        verifyNoInteractions(pregnancyPersistencePort);
        assertThat(result).isSameAs(responseVO);
    }

    @Test
    void registerBreeding_shouldThrowValidation_whenEventDateIsNull() {
        // Arrange
        BreedingRequestVO requestVO = BreedingRequestVO.builder()
                .eventDate(null)
                .breedingType(BreedingType.NATURAL)
                .build();

        // Act & Assert
        assertThrows(ValidationException.class,
                () -> reproductionBusiness.registerBreeding(FARM_ID, GOAT_ID, requestVO));
        verifyNoInteractions(reproductiveEventPersistencePort, reproductionMapper, pregnancyPersistencePort);
    }

    @Test
    void registerBreeding_shouldThrowValidation_whenBreedingTypeIsNull() {
        // Arrange
        BreedingRequestVO requestVO = BreedingRequestVO.builder()
                .eventDate(LocalDate.of(2026, 1, 1))
                .breedingType(null)
                .build();

        // Act & Assert
        assertThrows(ValidationException.class,
                () -> reproductionBusiness.registerBreeding(FARM_ID, GOAT_ID, requestVO));
        verifyNoInteractions(reproductiveEventPersistencePort, reproductionMapper, pregnancyPersistencePort);
    }

    @Test
    void registerBreeding_shouldThrowValidation_whenEventDateIsInFuture() {
        // Arrange
        BreedingRequestVO requestVO = BreedingRequestVO.builder()
                .eventDate(LocalDate.now().plusDays(1))
                .breedingType(BreedingType.NATURAL)
                .build();

        // Act & Assert
        assertThrows(ValidationException.class,
                () -> reproductionBusiness.registerBreeding(FARM_ID, GOAT_ID, requestVO));
        verifyNoInteractions(reproductiveEventPersistencePort, reproductionMapper, pregnancyPersistencePort);
    }

    // NOTE: checkScheduledDate not present on BreedingRequestVO, so the related test was removed.

    // ==================================================================================
    // CONFIRM PREGNANCY
    // ==================================================================================

    @Test
    void confirmPregnancy_shouldCreateCheckEventAndActivatePregnancy_whenResultIsPositiveAndNoActivePregnancy() {
        // Arrange
        PregnancyConfirmRequestVO requestVO = validConfirmRequestVOPositive();
        ReproductiveEvent coverageEvent = coverageEventEntity();

        when(reproductiveEventPersistencePort.findLatestCoverageByFarmIdAndGoatIdOnOrBefore(
                FARM_ID, GOAT_ID, requestVO.getCheckDate()))
                .thenReturn(Optional.of(coverageEvent));

        when(pregnancyPersistencePort.findActiveByFarmIdAndGoatId(FARM_ID, GOAT_ID))
                .thenReturn(Optional.empty());

        when(reproductiveEventPersistencePort.save(any(ReproductiveEvent.class)))
                .thenReturn(checkEventEntity());

        when(pregnancyPersistencePort.save(any(Pregnancy.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PregnancyResponseVO responseVO = pregnancyResponseVO();
        when(reproductionMapper.toPregnancyResponseVO(any(Pregnancy.class)))
                .thenReturn(responseVO);

        // Act
        PregnancyResponseVO result = reproductionBusiness.confirmPregnancy(FARM_ID, GOAT_ID, requestVO);

        // Assert
        ArgumentCaptor<ReproductiveEvent> eventCaptor = ArgumentCaptor.forClass(ReproductiveEvent.class);
        verify(reproductiveEventPersistencePort).save(eventCaptor.capture());

        ReproductiveEvent capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.getFarmId()).isEqualTo(FARM_ID);
        assertThat(capturedEvent.getGoatId()).isEqualTo(GOAT_ID);
        assertThat(capturedEvent.getEventType()).isEqualTo(ReproductiveEventType.PREGNANCY_CHECK);
        assertThat(capturedEvent.getEventDate()).isEqualTo(requestVO.getCheckDate());
        assertThat(capturedEvent.getCheckResult()).isEqualTo(PregnancyCheckResult.POSITIVE);

        ArgumentCaptor<Pregnancy> pregnancyCaptor = ArgumentCaptor.forClass(Pregnancy.class);
        verify(pregnancyPersistencePort).save(pregnancyCaptor.capture());

        Pregnancy capturedPregnancy = pregnancyCaptor.getValue();
        assertThat(capturedPregnancy.getStatus()).isEqualTo(PregnancyStatus.ACTIVE);
        assertThat(capturedPregnancy.getBreedingDate()).isEqualTo(coverageEvent.getEventDate());
        assertThat(capturedPregnancy.getConfirmDate()).isEqualTo(requestVO.getCheckDate());
        assertThat(capturedPregnancy.getExpectedDueDate()).isEqualTo(coverageEvent.getEventDate().plusDays(150));
        assertThat(capturedPregnancy.getClosedAt()).isNull();
        assertThat(capturedPregnancy.getCloseReason()).isNull();

        assertThat(result).isSameAs(responseVO);
    }

    @Test
    void confirmPregnancy_shouldThrowValidation_whenResultIsNegative() {
        // Arrange
        PregnancyConfirmRequestVO requestVO = validConfirmRequestVONegative();
        ReproductiveEvent coverageEvent = coverageEventEntity();

        when(reproductiveEventPersistencePort.findLatestCoverageByFarmIdAndGoatIdOnOrBefore(
                FARM_ID, GOAT_ID, requestVO.getCheckDate()))
                .thenReturn(Optional.of(coverageEvent));

        // Act & Assert
        assertThrows(ValidationException.class,
                () -> reproductionBusiness.confirmPregnancy(FARM_ID, GOAT_ID, requestVO));

        ArgumentCaptor<ReproductiveEvent> eventCaptor = ArgumentCaptor.forClass(ReproductiveEvent.class);
        verify(reproductiveEventPersistencePort).save(eventCaptor.capture());

        ReproductiveEvent capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.getEventType()).isEqualTo(ReproductiveEventType.PREGNANCY_CHECK);
        assertThat(capturedEvent.getCheckResult()).isEqualTo(PregnancyCheckResult.NEGATIVE);

        verify(pregnancyPersistencePort, never()).save(any(Pregnancy.class));
        verifyNoInteractions(reproductionMapper);
    }

    @Test
    void confirmPregnancy_shouldThrowValidation_whenNoCoverageExistsBeforeCheckDate() {
        // Arrange
        PregnancyConfirmRequestVO requestVO = validConfirmRequestVOPositive();

        when(reproductiveEventPersistencePort.findLatestCoverageByFarmIdAndGoatIdOnOrBefore(
                FARM_ID, GOAT_ID, requestVO.getCheckDate()))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ValidationException.class,
                () -> reproductionBusiness.confirmPregnancy(FARM_ID, GOAT_ID, requestVO));

        verify(reproductiveEventPersistencePort, never()).save(any(ReproductiveEvent.class));
        verify(pregnancyPersistencePort, never()).save(any(Pregnancy.class));
        verifyNoInteractions(reproductionMapper);
    }

    @Test
    void confirmPregnancy_shouldThrowValidation_whenActivePregnancyAlreadyExists_andResultIsPositive() {
        // Arrange
        PregnancyConfirmRequestVO requestVO = validConfirmRequestVOPositive();
        ReproductiveEvent coverageEvent = coverageEventEntity();
        Pregnancy activePregnancy = activePregnancyEntity();

        when(reproductiveEventPersistencePort.findLatestCoverageByFarmIdAndGoatIdOnOrBefore(
                FARM_ID, GOAT_ID, requestVO.getCheckDate()))
                .thenReturn(Optional.of(coverageEvent));

        when(pregnancyPersistencePort.findActiveByFarmIdAndGoatId(FARM_ID, GOAT_ID))
                .thenReturn(Optional.of(activePregnancy));

        // Act & Assert
        assertThrows(ValidationException.class,
                () -> reproductionBusiness.confirmPregnancy(FARM_ID, GOAT_ID, requestVO));

        verify(reproductiveEventPersistencePort, never()).save(any(ReproductiveEvent.class));
        verify(pregnancyPersistencePort, never()).save(any(Pregnancy.class));
        verifyNoInteractions(reproductionMapper);
    }

    @Test
    void confirmPregnancy_shouldThrowValidation_whenCheckDateIsNull() {
        // Arrange
        PregnancyConfirmRequestVO requestVO = PregnancyConfirmRequestVO.builder()
                .checkDate(null)
                .checkResult(PregnancyCheckResult.POSITIVE)
                .build();

        // Act & Assert
        assertThrows(ValidationException.class,
                () -> reproductionBusiness.confirmPregnancy(FARM_ID, GOAT_ID, requestVO));
        verifyNoInteractions(reproductiveEventPersistencePort, pregnancyPersistencePort, reproductionMapper);
    }

    @Test
    void confirmPregnancy_shouldThrowValidation_whenCheckDateIsInFuture() {
        // Arrange
        PregnancyConfirmRequestVO requestVO = PregnancyConfirmRequestVO.builder()
                .checkDate(LocalDate.now().plusDays(1))
                .checkResult(PregnancyCheckResult.POSITIVE)
                .build();

        // Act & Assert
        assertThrows(ValidationException.class,
                () -> reproductionBusiness.confirmPregnancy(FARM_ID, GOAT_ID, requestVO));
        verifyNoInteractions(reproductiveEventPersistencePort, pregnancyPersistencePort, reproductionMapper);
    }

    // ==================================================================================
    // GET ACTIVE PREGNANCY
    // ==================================================================================

    @Test
    void getActivePregnancy_shouldReturnActivePregnancy_whenExists() {
        // Arrange
        Pregnancy activePregnancy = activePregnancyEntity();
        PregnancyResponseVO responseVO = pregnancyResponseVO();

        when(pregnancyPersistencePort.findActiveByFarmIdAndGoatId(FARM_ID, GOAT_ID))
                .thenReturn(Optional.of(activePregnancy));
        when(reproductionMapper.toPregnancyResponseVO(activePregnancy))
                .thenReturn(responseVO);

        // Act
        PregnancyResponseVO result = reproductionBusiness.getActivePregnancy(FARM_ID, GOAT_ID);

        // Assert
        assertThat(result).isSameAs(responseVO);
    }

    @Test
    void getActivePregnancy_shouldThrowNotFound_whenNotExists() {
        // Arrange
        when(pregnancyPersistencePort.findActiveByFarmIdAndGoatId(FARM_ID, GOAT_ID))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> reproductionBusiness.getActivePregnancy(FARM_ID, GOAT_ID));
        verifyNoInteractions(reproductionMapper);
    }

    // ==================================================================================
    // CLOSE PREGNANCY
    // ==================================================================================

    @Test
    void closePregnancy_shouldClosePregnancyAndCreateCloseEvent_whenRequestIsValid() {
        // Arrange
        Long pregnancyId = 10L;
        Pregnancy activePregnancy = activePregnancyEntity();
        PregnancyCloseRequestVO requestVO = validCloseRequestVO();

        when(pregnancyPersistencePort.findByIdAndFarmIdAndGoatId(pregnancyId, FARM_ID, GOAT_ID))
                .thenReturn(Optional.of(activePregnancy));

        when(pregnancyPersistencePort.save(any(Pregnancy.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(reproductiveEventPersistencePort.save(any(ReproductiveEvent.class)))
                .thenReturn(closeEventEntity(pregnancyId));

        PregnancyResponseVO responseVO = pregnancyResponseVO();
        when(reproductionMapper.toPregnancyResponseVO(any(Pregnancy.class)))
                .thenReturn(responseVO);

        // Act
        PregnancyResponseVO result = reproductionBusiness.closePregnancy(FARM_ID, GOAT_ID, pregnancyId, requestVO);

        // Assert
        ArgumentCaptor<Pregnancy> pregnancyCaptor = ArgumentCaptor.forClass(Pregnancy.class);
        verify(pregnancyPersistencePort).save(pregnancyCaptor.capture());

        Pregnancy capturedPregnancy = pregnancyCaptor.getValue();
        assertThat(capturedPregnancy.getStatus()).isEqualTo(PregnancyStatus.CLOSED);
        assertThat(capturedPregnancy.getClosedAt()).isEqualTo(requestVO.getCloseDate());
        assertThat(capturedPregnancy.getCloseReason()).isEqualTo(requestVO.getCloseReason());

        ArgumentCaptor<ReproductiveEvent> eventCaptor = ArgumentCaptor.forClass(ReproductiveEvent.class);
        verify(reproductiveEventPersistencePort).save(eventCaptor.capture());

        ReproductiveEvent capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.getEventType()).isEqualTo(ReproductiveEventType.PREGNANCY_CLOSE);
        assertThat(capturedEvent.getEventDate()).isEqualTo(requestVO.getCloseDate());
        assertThat(capturedEvent.getPregnancyId()).isEqualTo(pregnancyId);
        assertThat(capturedEvent.getFarmId()).isEqualTo(FARM_ID);
        assertThat(capturedEvent.getGoatId()).isEqualTo(GOAT_ID);

        assertThat(result).isSameAs(responseVO);
    }

    @Test
    void closePregnancy_shouldThrowNotFound_whenPregnancyDoesNotExist() {
        // Arrange
        Long pregnancyId = 999L;
        PregnancyCloseRequestVO requestVO = validCloseRequestVO();

        when(pregnancyPersistencePort.findByIdAndFarmIdAndGoatId(pregnancyId, FARM_ID, GOAT_ID))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> reproductionBusiness.closePregnancy(FARM_ID, GOAT_ID, pregnancyId, requestVO));

        verify(pregnancyPersistencePort, never()).save(any(Pregnancy.class));
        verify(reproductiveEventPersistencePort, never()).save(any(ReproductiveEvent.class));
        verifyNoInteractions(reproductionMapper);
    }

    @Test
    void closePregnancy_shouldThrowValidation_whenPregnancyIsNotActive() {
        // Arrange
        Long pregnancyId = 11L;
        Pregnancy closedPregnancy = closedPregnancyEntity();
        PregnancyCloseRequestVO requestVO = validCloseRequestVO();

        when(pregnancyPersistencePort.findByIdAndFarmIdAndGoatId(pregnancyId, FARM_ID, GOAT_ID))
                .thenReturn(Optional.of(closedPregnancy));

        // Act & Assert
        assertThrows(ValidationException.class,
                () -> reproductionBusiness.closePregnancy(FARM_ID, GOAT_ID, pregnancyId, requestVO));

        verify(pregnancyPersistencePort, never()).save(any(Pregnancy.class));
        verify(reproductiveEventPersistencePort, never()).save(any(ReproductiveEvent.class));
        verifyNoInteractions(reproductionMapper);
    }

    @Test
    void closePregnancy_shouldThrowValidation_whenCloseDateIsNull() {
        // Arrange
        Long pregnancyId = 10L;
        Pregnancy activePregnancy = activePregnancyEntity();
        PregnancyCloseRequestVO requestVO = PregnancyCloseRequestVO.builder()
                .closeDate(null)
                .closeReason(PregnancyCloseReason.BIRTH)
                .build();

        when(pregnancyPersistencePort.findByIdAndFarmIdAndGoatId(pregnancyId, FARM_ID, GOAT_ID))
                .thenReturn(Optional.of(activePregnancy));

        // Act & Assert
        assertThrows(ValidationException.class,
                () -> reproductionBusiness.closePregnancy(FARM_ID, GOAT_ID, pregnancyId, requestVO));

        verify(pregnancyPersistencePort, never()).save(any(Pregnancy.class));
        verify(reproductiveEventPersistencePort, never()).save(any(ReproductiveEvent.class));
        verifyNoInteractions(reproductionMapper);
    }

    @Test
    void closePregnancy_shouldThrowValidation_whenCloseDateIsBeforeBreedingDate() {
        // Arrange
        Long pregnancyId = 10L;
        Pregnancy activePregnancy = activePregnancyEntity();
        PregnancyCloseRequestVO requestVO = PregnancyCloseRequestVO.builder()
                .closeDate(activePregnancy.getBreedingDate().minusDays(1))
                .closeReason(PregnancyCloseReason.BIRTH)
                .build();

        when(pregnancyPersistencePort.findByIdAndFarmIdAndGoatId(pregnancyId, FARM_ID, GOAT_ID))
                .thenReturn(Optional.of(activePregnancy));

        // Act & Assert
        assertThrows(ValidationException.class,
                () -> reproductionBusiness.closePregnancy(FARM_ID, GOAT_ID, pregnancyId, requestVO));

        verify(pregnancyPersistencePort, never()).save(any(Pregnancy.class));
        verify(reproductiveEventPersistencePort, never()).save(any(ReproductiveEvent.class));
        verifyNoInteractions(reproductionMapper);
    }

    @Test
    void closePregnancy_shouldThrowValidation_whenCloseReasonIsNull() {
        // Arrange
        Long pregnancyId = 10L;
        Pregnancy activePregnancy = activePregnancyEntity();
        PregnancyCloseRequestVO requestVO = PregnancyCloseRequestVO.builder()
                .closeDate(LocalDate.of(2026, 1, 12))
                .closeReason(null)
                .build();

        when(pregnancyPersistencePort.findByIdAndFarmIdAndGoatId(pregnancyId, FARM_ID, GOAT_ID))
                .thenReturn(Optional.of(activePregnancy));

        // Act & Assert
        assertThrows(ValidationException.class,
                () -> reproductionBusiness.closePregnancy(FARM_ID, GOAT_ID, pregnancyId, requestVO));

        verify(pregnancyPersistencePort, never()).save(any(Pregnancy.class));
        verify(reproductiveEventPersistencePort, never()).save(any(ReproductiveEvent.class));
        verifyNoInteractions(reproductionMapper);
    }

    // ==================================================================================
    // HELPERS (Milk module style)
    // ==================================================================================

    private BreedingRequestVO validBreedingRequestVO() {
        return BreedingRequestVO.builder()
                .eventDate(LocalDate.of(2026, 1, 1))
                .breedingType(BreedingType.NATURAL)
                .breederRef("MALE_01")
                .notes("Natural breeding request")
                .build();
    }

    private PregnancyConfirmRequestVO validConfirmRequestVOPositive() {
        return PregnancyConfirmRequestVO.builder()
                .checkDate(LocalDate.of(2026, 1, 10))
                .checkResult(PregnancyCheckResult.POSITIVE)
                .notes("Pregnancy confirmed positive")
                .build();
    }

    private PregnancyConfirmRequestVO validConfirmRequestVONegative() {
        return PregnancyConfirmRequestVO.builder()
                .checkDate(LocalDate.of(2026, 1, 10))
                .checkResult(PregnancyCheckResult.NEGATIVE)
                .notes("Pregnancy confirmed negative")
                .build();
    }

    private PregnancyCloseRequestVO validCloseRequestVO() {
        return PregnancyCloseRequestVO.builder()
                .closeDate(LocalDate.of(2026, 1, 12))
                .status(PregnancyStatus.CLOSED)
                .closeReason(PregnancyCloseReason.BIRTH)
                .notes("Pregnancy closed successfully")
                .build();
    }

    private Pregnancy activePregnancyEntity() {
        return Pregnancy.builder()
                .id(10L)
                .farmId(FARM_ID)
                .goatId(GOAT_ID)
                .status(PregnancyStatus.ACTIVE)
                .breedingDate(LocalDate.of(2026, 1, 1))
                .confirmDate(LocalDate.of(2026, 1, 10))
                .expectedDueDate(LocalDate.of(2026, 5, 31))
                .closedAt(null)
                .closeReason(null)
                .notes("Active pregnancy fixture")
                .build();
    }

    private Pregnancy closedPregnancyEntity() {
        return Pregnancy.builder()
                .id(11L)
                .farmId(FARM_ID)
                .goatId(GOAT_ID)
                .status(PregnancyStatus.CLOSED)
                .breedingDate(LocalDate.of(2026, 1, 1))
                .confirmDate(LocalDate.of(2026, 1, 10))
                .expectedDueDate(LocalDate.of(2026, 5, 31))
                .closedAt(LocalDate.of(2026, 1, 12))
                .closeReason(PregnancyCloseReason.BIRTH)
                .notes("Closed pregnancy fixture")
                .build();
    }

    private ReproductiveEvent coverageEventEntity() {
        return ReproductiveEvent.builder()
                .id(100L)
                .farmId(FARM_ID)
                .goatId(GOAT_ID)
                .eventType(ReproductiveEventType.COVERAGE)
                .eventDate(LocalDate.of(2026, 1, 1))
                .breedingType(BreedingType.NATURAL)
                .breederRef("MALE_01")
                .notes("Coverage fixture")
                .build();
    }

    private ReproductiveEvent checkEventEntity() {
        return ReproductiveEvent.builder()
                .id(101L)
                .farmId(FARM_ID)
                .goatId(GOAT_ID)
                .eventType(ReproductiveEventType.PREGNANCY_CHECK)
                .eventDate(LocalDate.of(2026, 1, 10))
                .checkResult(PregnancyCheckResult.POSITIVE)
                .notes("Check fixture")
                .build();
    }

    private ReproductiveEvent closeEventEntity(Long pregnancyId) {
        return ReproductiveEvent.builder()
                .id(102L)
                .farmId(FARM_ID)
                .goatId(GOAT_ID)
                .pregnancyId(pregnancyId)
                .eventType(ReproductiveEventType.PREGNANCY_CLOSE)
                .eventDate(LocalDate.of(2026, 1, 12))
                .notes("Close fixture")
                .build();
    }

    @SuppressWarnings("unused")
    private ReproductiveEventResponseVO reproductiveEventResponseVO() {
        return ReproductiveEventResponseVO.builder().build();
    }

    @SuppressWarnings("unused")
    private PregnancyResponseVO pregnancyResponseVO() {
        return PregnancyResponseVO.builder().build();
    }
}
