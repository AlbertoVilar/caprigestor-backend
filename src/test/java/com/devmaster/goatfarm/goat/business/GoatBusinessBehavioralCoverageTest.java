package com.devmaster.goatfarm.goat.business;

import com.devmaster.goatfarm.application.core.business.common.EntityFinder;
import com.devmaster.goatfarm.audit.application.ports.in.OperationalAuditUseCase;
import com.devmaster.goatfarm.authority.business.bo.AuthenticatedPrincipal;
import com.devmaster.goatfarm.config.exceptions.custom.BusinessRuleException;
import com.devmaster.goatfarm.config.exceptions.custom.InvalidArgumentException;
import com.devmaster.goatfarm.config.security.OwnershipService;
import com.devmaster.goatfarm.farm.application.ports.out.GoatFarmPersistencePort;
import com.devmaster.goatfarm.farm.persistence.entity.GoatFarm;
import com.devmaster.goatfarm.goat.application.ports.out.GoatBreedCount;
import com.devmaster.goatfarm.goat.application.ports.out.GoatHerdSnapshot;
import com.devmaster.goatfarm.goat.application.pagination.GoatPage;
import com.devmaster.goatfarm.goat.application.pagination.GoatPageQuery;
import com.devmaster.goatfarm.goat.application.ports.out.GoatParentagePort;
import com.devmaster.goatfarm.goat.application.ports.out.GoatPersistencePort;
import com.devmaster.goatfarm.goat.business.bo.GoatExitRequestVO;
import com.devmaster.goatfarm.goat.business.bo.GoatExitResponseVO;
import com.devmaster.goatfarm.goat.business.bo.GoatHerdSummaryVO;
import com.devmaster.goatfarm.goat.business.bo.GoatRequestVO;
import com.devmaster.goatfarm.goat.business.bo.GoatResponseVO;
import com.devmaster.goatfarm.goat.domain.Goat;
import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goat.domain.RegistrationIdentity;
import com.devmaster.goatfarm.goat.enums.Category;
import com.devmaster.goatfarm.goat.enums.Gender;
import com.devmaster.goatfarm.goat.enums.GoatBreed;
import com.devmaster.goatfarm.goat.enums.GoatExitType;
import com.devmaster.goatfarm.goat.enums.GoatStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Behavioral regression coverage retained while the Goat application boundary
 * moves from JPA entities to domain objects.
 */
@ExtendWith(MockitoExtension.class)
class GoatBusinessBehavioralCoverageTest {

    @Mock private GoatPersistencePort goatPort;
    @Mock private GoatFarmPersistencePort goatFarmPort;
    @Mock private OwnershipService ownershipService;
    @Mock private EntityFinder entityFinder;
    @Mock private OperationalAuditUseCase audit;
    @Mock private GoatParentagePort parentage;

    private GoatBusiness business;
    private Goat goat;

    @BeforeEach
    void setUp() {
        business = new GoatBusiness(goatPort, goatFarmPort, ownershipService, entityFinder, audit, parentage);
        goat = goat(77L, "1643222002", "Xeque", Gender.MACHO, GoatBreed.ALPINA, GoatStatus.ATIVO,
                null, null, null);
        lenient().when(parentage.resolve(any(), any(), any(), any()))
                .thenReturn(new GoatParentagePort.ResolvedParentage(null, null));
        lenient().when(entityFinder.findOrThrow(any(), anyString()))
                .thenAnswer(invocation -> ((java.util.function.Supplier<?>) invocation.getArgument(0)).get());
    }

    @Test
    void createsGoatWithLocalParentsThroughParentagePort() {
        GoatFarm farm = new GoatFarm();
        farm.setId(1L);
        Goat.ParentReference father = Goat.ParentReference.local(new GoatId(11L), "164321001", "Reprodutor Alpha");
        Goat.ParentReference mother = Goat.ParentReference.local(new GoatId(12L), "164321002", "Matriz Beta");
        GoatRequestVO request = request("1643222002", "Xeque");
        request.setFatherRegistrationNumber("164321001");
        request.setMotherRegistrationNumber("164321002");

        doNothing().when(ownershipService).verifyFarmManagement(1L);
        when(goatFarmPort.findById(1L)).thenReturn(Optional.of(farm));
        when(ownershipService.getCurrentPrincipal()).thenReturn(
                new AuthenticatedPrincipal(1L, "test@example.com", "Test", Set.of()));
        when(goatPort.existsByRegistrationNumber("1643222002")).thenReturn(false);
        when(parentage.resolve(Category.PA, "1643222002", "164321001", "164321002"))
                .thenReturn(new GoatParentagePort.ResolvedParentage(father, mother));
        when(goatPort.save(any(Goat.class))).thenAnswer(invocation -> invocation.getArgument(0));

        GoatResponseVO result = business.createGoat(1L, request);

        assertThat(result.getRegistrationNumber()).isEqualTo("1643222002");
        ArgumentCaptor<Goat> saved = ArgumentCaptor.forClass(Goat.class);
        verify(goatPort).save(saved.capture());
        assertThat(saved.getValue().father()).isEqualTo(father);
        assertThat(saved.getValue().mother()).isEqualTo(mother);
    }

    @Test
    void filtersGoatsByBreedInFarmList() {
        GoatPageQuery query = new GoatPageQuery(0, 12, "");
        when(goatPort.findAllByFarmIdAndBreed(eq(1L), eq(GoatBreed.SAANEN),
                eq(query)))
                .thenReturn(new GoatPage<>(List.of(goat), 1, 0, 12));

        GoatPage<GoatResponseVO> result = business.findAllGoatsByFarm(1L, GoatBreed.SAANEN, query);

        assertThat(result.totalElements()).isEqualTo(1L);
        verify(goatPort).findAllByFarmIdAndBreed(1L, GoatBreed.SAANEN, query);
    }

    @Test
    void filtersGoatsByNameAndBreedInFarmSearch() {
        GoatPageQuery query = new GoatPageQuery(0, 12, "");
        when(goatPort.findByNameAndFarmIdAndBreed(eq(1L), eq("Xeque"), eq(GoatBreed.ALPINA),
                eq(query)))
                .thenReturn(new GoatPage<>(List.of(goat), 1, 0, 12));

        GoatPage<GoatResponseVO> result = business.findGoatsByNameAndFarm(1L, "Xeque", GoatBreed.ALPINA, query);

        assertThat(result.totalElements()).isEqualTo(1L);
        verify(goatPort).findByNameAndFarmIdAndBreed(1L, "Xeque", GoatBreed.ALPINA, query);
    }

    @Test
    void listsOffspringByParentRegistrationWithinFarm() {
        Goat kid = goat(88L, "164322900", "Cria Teste", Gender.FEMEA, GoatBreed.ALPINA, GoatStatus.ATIVO,
                null, null, null);
        when(goatPort.findByRegistrationNumberAndFarmId("1643222002", 1L)).thenReturn(Optional.of(goat));
        when(goatPort.findOffspringByParentId(1L, new GoatId(77L))).thenReturn(List.of(kid));

        List<GoatResponseVO> result = business.listOffspring(1L, "1643222002");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRegistrationNumber()).isEqualTo("164322900");
        verify(goatPort).findByRegistrationNumberAndFarmId("1643222002", 1L);
        verify(goatPort).findOffspringByParentId(1L, new GoatId(77L));
    }

    @Test
    void buildsHerdSummaryWithSexStatusBreedAndUnknownBreedCounts() {
        when(goatPort.getHerdSummary(1L)).thenReturn(new GoatHerdSnapshot(
                20, 4, 16, 17, 1, 1, 1,
                List.of(new GoatBreedCount(GoatBreed.SAANEN, 8), new GoatBreedCount(GoatBreed.BOER, 5)), 2));

        GoatHerdSummaryVO summary = business.getGoatHerdSummary(1L);

        assertThat(summary.getTotal()).isEqualTo(20L);
        assertThat(summary.getMales()).isEqualTo(4L);
        assertThat(summary.getFemales()).isEqualTo(16L);
        assertThat(summary.getActive()).isEqualTo(17L);
        assertThat(summary.getInactive()).isEqualTo(1L);
        assertThat(summary.getSold()).isEqualTo(1L);
        assertThat(summary.getDeceased()).isEqualTo(1L);
        assertThat(summary.getBreeds()).hasSize(3);
        assertThat(summary.getBreeds().get(0).getLabel()).isEqualTo("Saanen");
        assertThat(summary.getBreeds().get(0).getCount()).isEqualTo(8L);
        assertThat(summary.getBreeds().get(2).getLabel()).isEqualTo("Não informada");
        assertThat(summary.getBreeds().get(2).getCount()).isEqualTo(2L);
    }

    @Test
    void exitsActiveGoatAsSoldAndAuditsTheOperation() {
        GoatExitRequestVO request = GoatExitRequestVO.builder()
                .exitType(GoatExitType.VENDA).exitDate(LocalDate.now().minusDays(1))
                .notes("Venda confirmada").build();
        when(goatPort.findByRegistrationNumberAndFarmId("1643222002", 1L)).thenReturn(Optional.of(goat));
        when(goatPort.save(any(Goat.class))).thenAnswer(invocation -> invocation.getArgument(0));

        GoatExitResponseVO result = business.exitGoat(1L, "1643222002", request);

        assertThat(result.getGoatId()).isEqualTo("1643222002");
        assertThat(result.getExitType()).isEqualTo(GoatExitType.VENDA);
        assertThat(result.getCurrentStatus()).isEqualTo(GoatStatus.VENDIDO);
        assertThat(result.getPreviousStatus()).isEqualTo(GoatStatus.ATIVO);
        verify(ownershipService).verifyFarmOwnership(1L);
        verify(goatPort).save(any(Goat.class));
        verify(audit).record(any());
    }

    @Test
    void rejectsGoatExitWithFutureDate() {
        GoatExitRequestVO request = GoatExitRequestVO.builder()
                .exitType(GoatExitType.DESCARTE).exitDate(LocalDate.now().plusDays(1)).build();
        when(goatPort.findByRegistrationNumberAndFarmId("1643222002", 1L)).thenReturn(Optional.of(goat));

        assertThatThrownBy(() -> business.exitGoat(1L, "1643222002", request))
                .isInstanceOf(InvalidArgumentException.class)
                .hasMessageContaining("Data de saída não pode ser futura");
        verify(goatPort, never()).save(any(Goat.class));
    }

    @Test
    void rejectsDuplicatedGoatExit() {
        Goat exited = goat(77L, "1643222002", "Xeque", Gender.MACHO, GoatBreed.ALPINA, GoatStatus.ATIVO,
                GoatExitType.VENDA, LocalDate.now().minusDays(10), null);
        GoatExitRequestVO request = GoatExitRequestVO.builder()
                .exitType(GoatExitType.TRANSFERENCIA).exitDate(LocalDate.now().minusDays(1)).build();
        when(goatPort.findByRegistrationNumberAndFarmId("1643222002", 1L)).thenReturn(Optional.of(exited));

        assertThatThrownBy(() -> business.exitGoat(1L, "1643222002", request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Já existe saída registrada para este animal");
        verify(goatPort, never()).save(any(Goat.class));
    }

    private GoatRequestVO request(String registration, String name) {
        GoatRequestVO request = new GoatRequestVO();
        request.setRegistrationNumber(registration);
        request.setName(name);
        request.setGender(Gender.MACHO);
        request.setBreed(GoatBreed.ALPINA);
        request.setBirthDate(LocalDate.of(2025, 1, 1));
        request.setStatus(GoatStatus.ATIVO);
        request.setCategory(Category.PA);
        request.setTod("16432");
        request.setToe("22002");
        request.setFarmId(1L);
        request.setUserId(1L);
        return request;
    }

    private Goat goat(Long id, String registration, String name, Gender gender, GoatBreed breed, GoatStatus status,
                      GoatExitType exitType, LocalDate exitDate, String exitNotes) {
        String tod = "1643222002".equals(registration) ? "16432" : null;
        String toe = "1643222002".equals(registration) ? "22002" : null;
        return Goat.rehydrate(new GoatId(id), RegistrationIdentity.of(registration, tod, toe),
                name, gender, breed, "Marrom", LocalDate.of(2025, 1, 1), status,
                exitType, exitDate, exitNotes, Category.PA, null, null, 1L, 1L, "Capril", "Alberto");
    }
}
