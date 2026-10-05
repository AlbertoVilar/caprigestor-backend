package com.devmaster.goatfarm.goatownership.business;

import com.devmaster.goatfarm.genealogy.application.model.GenealogyTreeSnapshot;
import com.devmaster.goatfarm.genealogy.application.ports.in.GenealogyTreeProjectionUseCase;
import com.devmaster.goatfarm.goat.application.model.GoatGenealogySnapshot;
import com.devmaster.goatfarm.goat.application.ports.in.GoatGenealogyReadUseCase;
import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goat.enums.GoatStatus;
import com.devmaster.goatfarm.goatownership.application.model.FarmGoatRegistryDisposition;
import com.devmaster.goatfarm.goatownership.application.model.FarmGoatRegistryItem;
import com.devmaster.goatfarm.goatownership.application.model.FarmGoatRegistryRole;
import com.devmaster.goatfarm.goatownership.application.ports.in.FarmGoatRegistryQueryUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FarmGoatHistoricalGenealogyQueryBusinessTest {

    @Mock
    private FarmGoatRegistryQueryUseCase registryQueryUseCase;
    @Mock
    private GoatGenealogyReadUseCase goatGenealogyReadUseCase;
    @Mock
    private GenealogyTreeProjectionUseCase genealogyTreeProjectionUseCase;

    private FarmGoatHistoricalGenealogyQueryBusiness business;

    @BeforeEach
    void setUp() {
        business = new FarmGoatHistoricalGenealogyQueryBusiness(
                registryQueryUseCase,
                goatGenealogyReadUseCase,
                genealogyTreeProjectionUseCase
        );
    }

    @Test
    void currentOwner_success() {
        GoatId goatId = GoatId.of(42L);
        FarmGoatRegistryItem registryItem = registryItem(goatId, Set.of(FarmGoatRegistryRole.CURRENT_OWNER), 1L);
        GoatGenealogySnapshot snapshot = rootSnapshot(goatId);
        GenealogyTreeSnapshot tree = mock(GenealogyTreeSnapshot.class);

        when(registryQueryUseCase.findForFarmAndGoat(1L, goatId)).thenReturn(Optional.of(registryItem));
        when(goatGenealogyReadUseCase.findGenealogyByGoatId(goatId)).thenReturn(Optional.of(snapshot));
        when(genealogyTreeProjectionUseCase.projectLocal(snapshot)).thenReturn(tree);

        var result = business.findHistoricalGenealogy(1L, goatId, false);

        assertThat(result).contains(tree);
        verify(genealogyTreeProjectionUseCase).projectLocal(snapshot);
        verify(genealogyTreeProjectionUseCase, never()).complementWithAbcc(any());
    }

    @Test
    void formerOwner_success() {
        GoatId goatId = GoatId.of(42L);
        FarmGoatRegistryItem registryItem = registryItem(goatId, Set.of(FarmGoatRegistryRole.FORMER_OWNER), 2L);
        GoatGenealogySnapshot snapshot = rootSnapshot(goatId);
        GenealogyTreeSnapshot tree = mock(GenealogyTreeSnapshot.class);

        when(registryQueryUseCase.findForFarmAndGoat(1L, goatId)).thenReturn(Optional.of(registryItem));
        when(goatGenealogyReadUseCase.findGenealogyByGoatId(goatId)).thenReturn(Optional.of(snapshot));
        when(genealogyTreeProjectionUseCase.projectLocal(snapshot)).thenReturn(tree);

        var result = business.findHistoricalGenealogy(1L, goatId, false);

        assertThat(result).contains(tree);
    }

    @Test
    void creatorOnly_success() {
        GoatId goatId = GoatId.of(42L);
        FarmGoatRegistryItem registryItem = registryItem(goatId, Set.of(FarmGoatRegistryRole.CREATOR), null);
        GoatGenealogySnapshot snapshot = rootSnapshot(goatId);
        GenealogyTreeSnapshot tree = mock(GenealogyTreeSnapshot.class);

        when(registryQueryUseCase.findForFarmAndGoat(1L, goatId)).thenReturn(Optional.of(registryItem));
        when(goatGenealogyReadUseCase.findGenealogyByGoatId(goatId)).thenReturn(Optional.of(snapshot));
        when(genealogyTreeProjectionUseCase.projectLocal(snapshot)).thenReturn(tree);

        var result = business.findHistoricalGenealogy(1L, goatId, false);

        assertThat(result).contains(tree);
    }

    @Test
    void unrelatedFarm_empty() {
        GoatId goatId = GoatId.of(42L);
        when(registryQueryUseCase.findForFarmAndGoat(3L, goatId)).thenReturn(Optional.empty());

        var result = business.findHistoricalGenealogy(3L, goatId, false);

        assertThat(result).isEmpty();
        verify(goatGenealogyReadUseCase, never()).findGenealogyByGoatId(any());
    }

    @Test
    void terminalAfterTransfer_formerOwnerReadable() {
        GoatId goatId = GoatId.of(42L);
        // currentOwnerFarmId == null due to terminal exit
        FarmGoatRegistryItem registryItem = registryItem(goatId, Set.of(FarmGoatRegistryRole.FORMER_OWNER), null);
        GoatGenealogySnapshot snapshot = rootSnapshot(goatId);
        GenealogyTreeSnapshot tree = mock(GenealogyTreeSnapshot.class);

        when(registryQueryUseCase.findForFarmAndGoat(1L, goatId)).thenReturn(Optional.of(registryItem));
        when(goatGenealogyReadUseCase.findGenealogyByGoatId(goatId)).thenReturn(Optional.of(snapshot));
        when(genealogyTreeProjectionUseCase.projectLocal(snapshot)).thenReturn(tree);

        var result = business.findHistoricalGenealogy(1L, goatId, false);

        assertThat(result).contains(tree);
    }

    @Test
    void complementaryMode_usesComplementWithAbcc() {
        GoatId goatId = GoatId.of(42L);
        FarmGoatRegistryItem registryItem = registryItem(goatId, Set.of(FarmGoatRegistryRole.CURRENT_OWNER), 1L);
        GoatGenealogySnapshot snapshot = rootSnapshot(goatId);
        GenealogyTreeSnapshot tree = mock(GenealogyTreeSnapshot.class);

        when(registryQueryUseCase.findForFarmAndGoat(1L, goatId)).thenReturn(Optional.of(registryItem));
        when(goatGenealogyReadUseCase.findGenealogyByGoatId(goatId)).thenReturn(Optional.of(snapshot));
        when(genealogyTreeProjectionUseCase.complementWithAbcc(snapshot)).thenReturn(tree);

        var result = business.findHistoricalGenealogy(1L, goatId, true);

        assertThat(result).contains(tree);
        verify(genealogyTreeProjectionUseCase).complementWithAbcc(snapshot);
        verify(genealogyTreeProjectionUseCase, never()).projectLocal(any());
    }

    @Test
    void historicalBusiness_neverTalksDirectlyToAbccOutputPort() {
        Field[] fields = FarmGoatHistoricalGenealogyQueryBusiness.class.getDeclaredFields();
        for (Field field : fields) {
            assertThat(field.getType().getName()).doesNotContain("GenealogyAbccQueryPort");
        }
    }

    private FarmGoatRegistryItem registryItem(GoatId goatId, Set<FarmGoatRegistryRole> roles, Long currentOwnerFarmId) {
        return new FarmGoatRegistryItem(
                goatId, "RG-42", "Cabra 42", GoatStatus.ATIVO, 1L, "Criador Vilar",
                roles, FarmGoatRegistryDisposition.CURRENT, currentOwnerFarmId
        );
    }

    private GoatGenealogySnapshot rootSnapshot(GoatId goatId) {
        return new GoatGenealogySnapshot(goatId, "RG-42", "Cabra 42", null, null, null, null, null, null, null, null, null, null, null, null);
    }
}
