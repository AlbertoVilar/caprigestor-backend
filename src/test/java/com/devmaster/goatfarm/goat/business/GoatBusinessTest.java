package com.devmaster.goatfarm.goat.business;

import com.devmaster.goatfarm.application.core.business.common.EntityFinder;
import com.devmaster.goatfarm.application.exception.AuthorizationDeniedException;
import com.devmaster.goatfarm.audit.application.ports.in.OperationalAuditUseCase;
import com.devmaster.goatfarm.authority.business.bo.AuthenticatedPrincipal;
import com.devmaster.goatfarm.authority.application.ports.in.CurrentPrincipalQueryUseCase;
import com.devmaster.goatfarm.authority.application.ports.in.FarmAuthorizationUseCase;
import com.devmaster.goatfarm.farm.application.ports.out.GoatFarmPersistencePort;
import com.devmaster.goatfarm.farm.application.model.FarmRecord;
import com.devmaster.goatfarm.farm.persistence.entity.GoatFarm;
import com.devmaster.goatfarm.goat.application.pagination.GoatPage;
import com.devmaster.goatfarm.goat.application.pagination.GoatPageQuery;
import com.devmaster.goatfarm.goat.application.ports.out.GoatParentagePort;
import com.devmaster.goatfarm.goat.application.ports.out.GoatPersistencePort;
import com.devmaster.goatfarm.goatownership.application.ports.in.GoatOwnershipExitUseCase;
import com.devmaster.goatfarm.goatownership.application.ports.in.GoatOwnershipGuardUseCase;
import com.devmaster.goatfarm.goatownership.application.ports.in.GoatOwnershipInitializationUseCase;
import com.devmaster.goatfarm.goat.application.model.GoatCreationOrigin;
import com.devmaster.goatfarm.goat.business.bo.*;
import com.devmaster.goatfarm.goat.domain.Goat;
import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goat.domain.RegistrationIdentity;
import com.devmaster.goatfarm.goat.enums.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoatBusinessTest {
    @Mock private GoatPersistencePort goatPort;
    @Mock private GoatFarmPersistencePort goatFarmPort;
    @Mock private FarmAuthorizationUseCase ownershipService;
    @Mock private CurrentPrincipalQueryUseCase currentPrincipalQuery;
    @Mock private EntityFinder entityFinder;
    @Mock private OperationalAuditUseCase audit;
    @Mock private GoatParentagePort parentage;
    @Mock private GoatOwnershipExitUseCase goatOwnershipExitUseCase;
    @Mock private GoatOwnershipInitializationUseCase goatOwnershipInitializationUseCase;
    @Mock private GoatOwnershipGuardUseCase goatOwnershipGuard;

    private GoatBusiness business;
    private GoatRequestVO request;
    private Goat goat;

    @BeforeEach
    void setUp() {
        business = new GoatBusiness(goatPort, goatFarmPort, ownershipService, entityFinder, audit, parentage, currentPrincipalQuery, goatOwnershipExitUseCase, goatOwnershipInitializationUseCase, goatOwnershipGuard);
        request = new GoatRequestVO();
        request.setRegistrationNumber("1643222002"); request.setName("Xeque"); request.setGender(Gender.MACHO);
        request.setBreed(GoatBreed.ALPINA); request.setBirthDate(LocalDate.of(2025, 1, 1));
        request.setStatus(GoatStatus.ATIVO); request.setCategory(Category.PA); request.setTod("16432"); request.setToe("22002");
        request.setFarmId(1L); request.setUserId(1L);
        goat = Goat.rehydrate(new GoatId(77), RegistrationIdentity.of(request.getRegistrationNumber(), request.getTod(), request.getToe()),
                request.getName(), request.getGender(), request.getBreed(), request.getColor(), request.getBirthDate(), request.getStatus(),
                null, null, null, request.getCategory(), null, null, 1L, 1L, "Capril", "Alberto");
        lenient().when(parentage.resolve(any(), any(), any(), any())).thenReturn(new GoatParentagePort.ResolvedParentage(null, null));
        lenient().doNothing().when(goatOwnershipGuard).requireCurrentFarm(any(), anyLong());
        lenient().when(entityFinder.findOrThrow(any(), anyString())).thenAnswer(inv -> ((java.util.function.Supplier<?>) inv.getArgument(0)).get());
    }

    @Test
    void createsUsingDomainPort() {
        GoatFarm farm = new GoatFarm(); farm.setId(1L);
        doNothing().when(ownershipService).verifyFarmManagement(1L);
        when(goatFarmPort.findById(1L)).thenReturn(Optional.of(farmRecord()));
        when(currentPrincipalQuery.requireCurrent()).thenReturn(principal(1L));
        when(goatPort.existsByRegistrationNumber("1643222002")).thenReturn(false);
        when(goatPort.save(any(Goat.class))).thenReturn(goat);

        GoatResponseVO result = business.createGoat(1L, request, GoatCreationOrigin.MANUAL);

        assertThat(result.getRegistrationNumber()).isEqualTo("1643222002");
        verify(goatPort).save(any(Goat.class));
        verify(goatOwnershipInitializationUseCase).initialize(argThat(command ->
                command.goatId().equals(new GoatId(77L))
                        && command.farmId() == 1L
                        && command.origin() == GoatCreationOrigin.MANUAL));
    }

    @Test
    void rejectsContradictoryRegistrationInsteadOfSilentlyCorrectingIt() {
        doNothing().when(ownershipService).verifyFarmManagement(1L);
        request.setRegistrationNumber("1643299999");

        assertThatThrownBy(() -> business.createGoat(1L, request, GoatCreationOrigin.MANUAL))
                .isInstanceOf(com.devmaster.goatfarm.config.exceptions.custom.BusinessRuleException.class)
                .hasMessageContaining("composição de TOD + TOE");
        verify(goatPort, never()).existsByRegistrationNumber(anyString());
        verify(goatPort, never()).save(any(Goat.class));
    }

    @Test
    void derivesRegistrationFromTodAndToeOnCreation() {
        doNothing().when(ownershipService).verifyFarmManagement(1L);
        GoatFarm farm = new GoatFarm(); farm.setId(1L);
        when(goatFarmPort.findById(1L)).thenReturn(Optional.of(farmRecord()));
        when(currentPrincipalQuery.requireCurrent()).thenReturn(principal(1L));
        when(goatPort.existsByRegistrationNumber("1643222002")).thenReturn(false);
        when(goatPort.save(any(Goat.class))).thenReturn(goat);

        request.setRegistrationNumber(" 16432 22002 ");
        GoatResponseVO result = business.createGoat(1L, request, GoatCreationOrigin.MANUAL);

        assertThat(result.getRegistrationNumber()).isEqualTo("1643222002");
        verify(goatPort).existsByRegistrationNumber("1643222002");
    }

    @Test
    void deleteIsExplicitlyProhibitedForCanonicalGoat() {
        doNothing().when(ownershipService).verifyFarmOwnership(1L);

        assertThatThrownBy(() -> business.deleteGoat(1L, "1643222002"))
                .isInstanceOf(com.devmaster.goatfarm.config.exceptions.custom.BusinessRuleException.class)
                .hasMessageContaining("exclusão física");
        verify(ownershipService).verifyFarmOwnership(1L);
    }

    private AuthenticatedPrincipal principal(Long id) {
        return new AuthenticatedPrincipal(id, "test@example.com", "Test", Set.of());
    }

    private FarmRecord farmRecord() {
        return new FarmRecord(1L, "Capril", null, null, null, null, List.of(), null, null, null);
    }

    @Test
    void updatesByRegistrationNumberWithoutReinterpretingANumericRgAsTechnicalId() {
        doNothing().when(ownershipService).verifyFarmOwnership(1L);
        request.setRegistrationNumber("77");
        request.setTod("7"); request.setToe("7");
        goat = Goat.rehydrate(new GoatId(99), RegistrationIdentity.of("77", "7", "7"),
                request.getName(), request.getGender(), request.getBreed(), request.getColor(), request.getBirthDate(), request.getStatus(),
                null, null, null, request.getCategory(), null, null, 1L, 1L, "Capril", "Alberto");
        when(goatPort.findDomainByRegistrationNumber("77")).thenReturn(Optional.of(goat));
        when(goatPort.save(any(Goat.class))).thenAnswer(inv -> inv.getArgument(0));

        request.setName("Xeque atualizado");
        GoatResponseVO result = business.updateGoat(1L, "77", request);

        assertThat(result.getName()).isEqualTo("Xeque atualizado");
        verify(goatPort).findDomainByRegistrationNumber("77");
        verify(goatPort, never()).findByRegistrationNumberAndFarmId(anyString(), anyLong());
    }

    @Test
    void rejectsStatusChangeDuringCommonUpdate() {
        when(goatPort.findDomainByRegistrationNumber("1643222002")).thenReturn(Optional.of(goat));
        request.setStatus(GoatStatus.INATIVO);

        assertThatThrownBy(() -> business.updateGoat(1L, "1643222002", request))
                .isInstanceOf(com.devmaster.goatfarm.config.exceptions.custom.BusinessRuleException.class)
                .hasMessageContaining("status do animal não pode ser alterado");
        verify(goatPort, never()).save(any(Goat.class));
    }

    @Test
    void rejectsCommonUpdateWhenCanonicalOwnershipIsClosed() {
        when(goatPort.findDomainByRegistrationNumber("1643222002")).thenReturn(Optional.of(goat));
        doThrow(new AuthorizationDeniedException("ownership canônico aberto"))
                .when(goatOwnershipGuard).requireCurrentFarm(new GoatId(77L), 1L);

        assertThatThrownBy(() -> business.updateGoat(1L, "1643222002", request))
                .isInstanceOf(AuthorizationDeniedException.class)
                .hasMessageContaining("ownership canônico aberto");
        verify(goatPort, never()).save(any(Goat.class));
    }

    @Test
    void rejectsCommonUpdateWhenOwnershipProjectionDriftsFromCanonicalFarm() {
        when(goatPort.findDomainByRegistrationNumber("1643222002")).thenReturn(Optional.of(goat));

        assertThatThrownBy(() -> business.updateGoat(2L, "1643222002", request))
                .isInstanceOf(com.devmaster.goatfarm.config.exceptions.custom.BusinessRuleException.class)
                .hasMessageContaining("diverge");

        verify(goatOwnershipGuard).requireCurrentFarm(new GoatId(77L), 2L);
        verify(goatPort, never()).save(any(Goat.class));
    }

    @Test
    void resolvesExplicitTechnicalRouteTokenWithoutRegistrationCollision() {
        when(goatPort.findByIdAndFarmId(new GoatId(77L), 1L)).thenReturn(Optional.of(goat));

        GoatResponseVO result = business.findGoatById(1L, "technical-77");

        assertThat(result.getTechnicalId()).isEqualTo(77L);
        verify(goatPort).findByIdAndFarmId(new GoatId(77L), 1L);
        verify(goatPort, never()).findByRegistrationNumberAndFarmId(anyString(), anyLong());
    }

    @Test
    void rejectsExitForInactiveGoat() {
        Goat inactive = Goat.rehydrate(new GoatId(77), goat.registrationIdentity(), goat.name(), goat.gender(), goat.breed(), goat.color(),
                goat.birthDate(), GoatStatus.INATIVO, null, null, null, goat.category(), null, null, 1L, 1L, "Capril", "Alberto");
        when(goatPort.findByRegistrationNumberAndFarmId("77", 1L)).thenReturn(Optional.of(inactive));
        GoatExitRequestVO exit = GoatExitRequestVO.builder().exitType(GoatExitType.VENDA).exitDate(LocalDate.now()).build();

        assertThatThrownBy(() -> business.exitGoat(1L, "77", exit)).isInstanceOf(com.devmaster.goatfarm.config.exceptions.custom.BusinessRuleException.class);
        verify(goatPort, never()).save(any(Goat.class));
    }

    @Test
    void returnsCleanPageAndSummary() {
        GoatPageQuery query = new GoatPageQuery(0, 12, "");
        when(goatPort.findAllByFarmId(1L, query))
                .thenReturn(new GoatPage<>(List.of(goat), 1, 0, 12));
        GoatPage<GoatResponseVO> page = business.findAllGoatsByFarm(1L, query);
        assertThat(page.totalElements()).isEqualTo(1);

        when(goatPort.getHerdSummary(1L)).thenReturn(new com.devmaster.goatfarm.goat.application.ports.out.GoatHerdSnapshot(1, 1, 0, 1, 0, 0, 0, List.of(), 1));
        assertThat(business.getGoatHerdSummary(1L).getTotal()).isEqualTo(1);
    }
}
