package com.devmaster.goatfarm.goat.business;

import com.devmaster.goatfarm.audit.application.ports.in.OperationalAuditUseCase;
import com.devmaster.goatfarm.authority.business.bo.AuthenticatedPrincipal;
import com.devmaster.goatfarm.authority.application.ports.in.CurrentPrincipalQueryUseCase;
import com.devmaster.goatfarm.authority.application.ports.in.FarmAuthorizationUseCase;
import com.devmaster.goatfarm.goat.application.ports.out.GoatReference;
import com.devmaster.goatfarm.goat.application.routing.GoatReferenceResolver;
import com.devmaster.goatfarm.goat.application.ports.out.GoatPersistencePort;
import com.devmaster.goatfarm.goat.application.ports.out.GoatRegistrationHistoryPersistencePort;
import com.devmaster.goatfarm.goat.business.bo.GoatRegistrationRectificationRequestVO;
import com.devmaster.goatfarm.goat.business.bo.GoatRegistrationRectificationResponseVO;
import com.devmaster.goatfarm.goat.domain.Goat;
import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goat.domain.GoatRegistrationHistory;
import com.devmaster.goatfarm.goat.domain.RegistrationIdentity;
import com.devmaster.goatfarm.goat.enums.Category;
import com.devmaster.goatfarm.goat.enums.Gender;
import com.devmaster.goatfarm.goat.enums.GoatBreed;
import com.devmaster.goatfarm.goat.enums.GoatStatus;
import com.devmaster.goatfarm.goat.enums.RegistrationRectificationSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoatRegistrationRectificationBusinessTest {

    @Mock private GoatPersistencePort goatPersistencePort;
    @Mock private GoatReferenceResolver goatReferenceResolver;
    @Mock private GoatRegistrationHistoryPersistencePort historyPersistencePort;
    @Mock private FarmAuthorizationUseCase ownershipService;
    @Mock private CurrentPrincipalQueryUseCase currentPrincipalQuery;
    @Mock private OperationalAuditUseCase operationalAuditUseCase;

    private GoatRegistrationRectificationBusiness business;
    private Goat goat;

    @BeforeEach
    void setUp() {
        business = new GoatRegistrationRectificationBusiness(
                goatPersistencePort, goatReferenceResolver, historyPersistencePort,
                ownershipService, operationalAuditUseCase, currentPrincipalQuery);
        goat = Goat.rehydrate(
                new GoatId(7L), RegistrationIdentity.fromTodAndToe("16432", "18012"),
                "Matriz", Gender.FEMEA, GoatBreed.SAANEN, "Branca", LocalDate.of(2024, 1, 1),
                GoatStatus.INATIVO, null, null, null, Category.PA, null, null,
                1L, 9L, "Capril", "Alberto");
        when(goatReferenceResolver.resolve("technical-7", 1L))
                .thenReturn(Optional.of(new GoatReference(new GoatId(7L), 1L, goat.registrationNumber(), goat.name(), goat.gender())));
        when(goatPersistencePort.findByIdAndFarmId(new GoatId(7L), 1L)).thenReturn(Optional.of(goat));
        lenient().when(goatPersistencePort.save(any(Goat.class))).thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(currentPrincipalQuery.requireCurrent()).thenReturn(
                new AuthenticatedPrincipal(9L, "alberto@example.com", "Alberto", Set.of()));
        lenient().when(historyPersistencePort.save(any(GoatRegistrationHistory.class))).thenAnswer(invocation -> {
            GoatRegistrationHistory value = invocation.getArgument(0);
            return new GoatRegistrationHistory(11L, value.goatId(), value.farmId(), value.oldIdentity(), value.newIdentity(),
                    value.source(), value.evidenceReference(), value.reason(), value.actorUserId(), value.createdAt());
        });
    }

    @Test
    void rectifiesSameAggregateAndPreservesTechnicalIdentity() {
        GoatRegistrationRectificationRequestVO request = request("20001", "20002", "Correção conferida na ABCC");

        GoatRegistrationRectificationResponseVO response = business.rectify(1L, "technical-7", request);

        assertThat(response.technicalGoatId()).isEqualTo(7L);
        assertThat(response.previousRegistrationNumber()).isEqualTo("1643218012");
        assertThat(response.currentRegistrationNumber()).isEqualTo("2000120002");
        assertThat(goat.id()).isEqualTo(new GoatId(7L));
        assertThat(goat.registrationNumber()).isEqualTo("2000120002");

        ArgumentCaptor<GoatRegistrationHistory> history = ArgumentCaptor.forClass(GoatRegistrationHistory.class);
        verify(historyPersistencePort).save(history.capture());
        assertThat(history.getValue().oldIdentity().registrationNumber()).isEqualTo("1643218012");
        assertThat(history.getValue().newIdentity().registrationNumber()).isEqualTo("2000120002");
        assertThat(history.getValue().reason()).isEqualTo("Correção conferida na ABCC");
        verify(operationalAuditUseCase).record(any());
    }

    @Test
    void rejectsDuplicateCurrentRegistrationBeforeMutation() {
        when(goatPersistencePort.existsByRegistrationNumber("2000120002")).thenReturn(true);

        assertThatThrownBy(() -> business.rectify(1L, "technical-7",
                request("20001", "20002", "Correção conferida na ABCC")))
                .isInstanceOf(com.devmaster.goatfarm.config.exceptions.DuplicateEntityException.class);

        verify(goatPersistencePort, never()).save(any(Goat.class));
        verify(historyPersistencePort, never()).save(any());
        verify(operationalAuditUseCase, never()).record(any());
    }

    @Test
    void rejectsAnUnchangedIdentityWithoutHistory() {
        assertThatThrownBy(() -> business.rectify(1L, "technical-7",
                request("16432", "18012", "Sem alteração")))
                .isInstanceOf(com.devmaster.goatfarm.config.exceptions.custom.BusinessRuleException.class);

        verify(goatPersistencePort, never()).save(any(Goat.class));
        verify(historyPersistencePort, never()).save(any());
    }

    private GoatRegistrationRectificationRequestVO request(String tod, String toe, String reason) {
        return new GoatRegistrationRectificationRequestVO(
                tod, toe, RegistrationRectificationSource.ABCC, "ABCC-2026-001", reason);
    }

}
