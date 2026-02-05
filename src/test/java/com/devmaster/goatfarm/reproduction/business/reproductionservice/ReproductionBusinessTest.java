package com.devmaster.goatfarm.reproduction.business.reproductionservice;

import com.devmaster.goatfarm.reproduction.application.ports.out.PregnancyPersistencePort;
import com.devmaster.goatfarm.reproduction.application.ports.out.ReproductiveEventPersistencePort;
import com.devmaster.goatfarm.application.core.business.validation.GoatGenderValidator;
import com.devmaster.goatfarm.config.exceptions.custom.BusinessRuleException;
import com.devmaster.goatfarm.config.exceptions.DuplicateEntityException;
import com.devmaster.goatfarm.config.exceptions.custom.InvalidArgumentException;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.goat.persistence.entity.Goat;
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
import com.devmaster.goatfarm.reproduction.api.mapper.ReproductionMapper;
import com.devmaster.goatfarm.reproduction.persistence.entity.Pregnancy;
import com.devmaster.goatfarm.reproduction.persistence.entity.ReproductiveEvent;
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
    private GoatGenderValidator goatGenderValidator;

    @Mock
    private ReproductionMapper reproductionMapper;

    @InjectMocks
    private ReproductionBusiness reproductionBusiness;

    private static final Long FARM_ID = 1L;
    private static final String GOAT_ID = "1643218012";

    @BeforeEach
    void setUp() {
        // Maintained to follow Milk module test style.
        org.mockito.Mockito.lenient().when(goatGenderValidator.requireFemale(any(Long.class), any(String.class)))
                .thenReturn(new Goat());
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
        assertThrows(InvalidArgumentException.class,
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
        assertThrows(InvalidArgumentException.class,
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
        assertThrows(InvalidArgumentException.class,
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
    void confirmPregnancy_shouldRejectNegativeWithoutPersistence() {
        // Arrange
        PregnancyConfirmRequestVO requestVO = validConfirmRequestVONegative();
        ReproductiveEvent coverageEvent = coverageEventEntity();

        when(reproductiveEventPersistencePort.findLatestCoverageByFarmIdAndGoatIdOnOrBefore(
                FARM_ID, GOAT_ID, requestVO.getCheckDate()))
                .thenReturn(Optional.of(coverageEvent));

        // Act & Assert
        InvalidArgumentException exception = assertThrows(InvalidArgumentException.class,
                () -> reproductionBusiness.confirmPregnancy(FARM_ID, GOAT_ID, requestVO));

        // Validate exception content
        assertThat(exception.getFieldName()).isEqualTo("checkResult");
        assertThat(exception.getMessage()).contains("Resultado NEGATIVE não é permitido");

        verify(reproductiveEventPersistencePort, never()).save(any(ReproductiveEvent.class));
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
        assertThrows(InvalidArgumentException.class,
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
        assertThrows(InvalidArgumentException.class,
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
        assertThrows(InvalidArgumentException.class,
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
        assertThrows(InvalidArgumentException.class,
                () -> reproductionBusiness.confirmPregnancy(FARM_ID, GOAT_ID, requestVO));
        verifyNoInteractions(reproductiveEventPersistencePort, pregnancyPersistencePort, reproductionMapper);
    }

    @Test
    void confirmPregnancy_shouldThrowValidation_whenMultipleActivePregnanciesExist_beforeWritingAnything() {
        // Arrange
        PregnancyConfirmRequestVO requestVO = validConfirmRequestVOPositive();

        when(pregnancyPersistencePort.findAllActiveByFarmIdAndGoatIdOrdered(FARM_ID, GOAT_ID))
                .thenReturn(java.util.List.of(activePregnancyEntity(), activePregnancyEntity()));

        // Act
        DuplicateEntityException exception = assertThrows(DuplicateEntityException.class,
                () -> reproductionBusiness.confirmPregnancy(FARM_ID, GOAT_ID, requestVO));

        assertThat(exception.getMessage()).contains("Foram encontradas múltiplas gestações ativas");

        verifyNoInteractions(reproductiveEventPersistencePort, reproductionMapper);
        verify(pregnancyPersistencePort, never()).save(any(Pregnancy.class));
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
    void getActivePregnancy_shouldThrowValidation_whenMultipleActivePregnanciesExist() {
        // Arrange
        DuplicateEntityException duplicateException = new DuplicateEntityException("Foram encontradas múltiplas gestações ativas");

        when(pregnancyPersistencePort.findActiveByFarmIdAndGoatId(FARM_ID, GOAT_ID))
                .thenThrow(duplicateException);

        // Act & Assert
        DuplicateEntityException thrown = assertThrows(DuplicateEntityException.class,
                () -> reproductionBusiness.getActivePregnancy(FARM_ID, GOAT_ID));

        assertThat(thrown.getMessage()).contains("Foram encontradas múltiplas gestações ativas");
        verifyNoInteractions(reproductionMapper);
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
    // GET PREGNANCY BY ID
    // ==================================================================================

    @Test
    void getPregnancyById_shouldReturnPregnancy_whenExists() {
        // Arrange
        Long pregnancyId = 1L;
        Pregnancy pregnancy = activePregnancyEntity();
        PregnancyResponseVO responseVO = pregnancyResponseVO();

        when(pregnancyPersistencePort.findByIdAndFarmIdAndGoatId(pregnancyId, FARM_ID, GOAT_ID))
                .thenReturn(Optional.of(pregnancy));
        when(reproductionMapper.toPregnancyResponseVO(pregnancy))
                .thenReturn(responseVO);

        // Act
        PregnancyResponseVO result = reproductionBusiness.getPregnancyById(FARM_ID, GOAT_ID, pregnancyId);

        // Assert
        assertThat(result).isSameAs(responseVO);
    }

    @Test
    void getPregnancyById_shouldThrowNotFound_whenNotExists() {
        // Arrange
        Long pregnancyId = 999L;
        when(pregnancyPersistencePort.findByIdAndFarmIdAndGoatId(pregnancyId, FARM_ID, GOAT_ID))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> reproductionBusiness.getPregnancyById(FARM_ID, GOAT_ID, pregnancyId));
        verifyNoInteractions(reproductionMapper);
    }

    @Test
    void getPregnancyById_shouldThrowInvalidArgument_whenIdIsInvalid() {
        // Act & Assert
        assertThrows(com.devmaster.goatfarm.config.exceptions.custom.InvalidArgumentException.class,
                () -> reproductionBusiness.getPregnancyById(FARM_ID, GOAT_ID, 0L));
        assertThrows(com.devmaster.goatfarm.config.exceptions.custom.InvalidArgumentException.class,
                () -> reproductionBusiness.getPregnancyById(FARM_ID, GOAT_ID, null));
        verifyNoInteractions(pregnancyPersistencePort, reproductionMapper);
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
        assertThrows(InvalidArgumentException.class,
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
        InvalidArgumentException ex = assertThrows(InvalidArgumentException.class,
                () -> reproductionBusiness.closePregnancy(FARM_ID, GOAT_ID, pregnancyId, requestVO));
        
        assertThat(ex.getFieldName()).isEqualTo("closeDate");

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
        InvalidArgumentException ex = assertThrows(InvalidArgumentException.class,
                () -> reproductionBusiness.closePregnancy(FARM_ID, GOAT_ID, pregnancyId, requestVO));

        assertThat(ex.getFieldName()).isEqualTo("closeDate");

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
        InvalidArgumentException ex = assertThrows(InvalidArgumentException.class,
                () -> reproductionBusiness.closePregnancy(FARM_ID, GOAT_ID, pregnancyId, requestVO));

        assertThat(ex.getFieldName()).isEqualTo("closeReason");

        verify(pregnancyPersistencePort, never()).save(any(Pregnancy.class));
        verify(reproductiveEventPersistencePort, never()).save(any(ReproductiveEvent.class));
        verifyNoInteractions(reproductionMapper);
    }

    // ==================================================================================
    // HELPERS (Milk module style)
    // ==================================================================================

    private BreedingRequestVO validBreedingRequestVO() {
        return BreedingRequestVO.builder()
                .eventDate(LocalDate.now().minusDays(60))
                .breedingType(BreedingType.NATURAL)
                .breederRef("MALE_01")
                .notes("Natural breeding request")
                .build();
    }

    private PregnancyConfirmRequestVO validConfirmRequestVOPositive() {
        return PregnancyConfirmRequestVO.builder()
                .checkDate(LocalDate.now())
                .checkResult(PregnancyCheckResult.POSITIVE)
                .notes("Pregnancy confirmed positive")
                .build();
    }

    private PregnancyConfirmRequestVO validConfirmRequestVONegative() {
        return PregnancyConfirmRequestVO.builder()
                .checkDate(LocalDate.now())
                .checkResult(PregnancyCheckResult.NEGATIVE)
                .notes("Pregnancy confirmed negative")
                .build();
    }

    private PregnancyCloseRequestVO validCloseRequestVO() {
        return PregnancyCloseRequestVO.builder()
                .closeDate(LocalDate.now().plusDays(5))
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
                .breedingDate(LocalDate.now().minusDays(60))
                .confirmDate(LocalDate.now())
                .expectedDueDate(LocalDate.now().minusDays(60).plusDays(150))
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
                .breedingDate(LocalDate.now().minusDays(60))
                .confirmDate(LocalDate.now())
                .expectedDueDate(LocalDate.now().minusDays(60).plusDays(150))
                .closedAt(LocalDate.now().plusDays(5))
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
                .eventDate(LocalDate.now().minusDays(60))
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
                .eventDate(LocalDate.now())
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
                .eventDate(LocalDate.now().plusDays(5))
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
