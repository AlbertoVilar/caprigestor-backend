package com.devmaster.goatfarm.goatownership.api.mapper;

import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goat.enums.GoatStatus;
import com.devmaster.goatfarm.goatownership.api.dto.FarmGoatRegistryResponseDTO;
import com.devmaster.goatfarm.goatownership.application.model.FarmGoatRegistryDisposition;
import com.devmaster.goatfarm.goatownership.application.model.FarmGoatRegistryItem;
import com.devmaster.goatfarm.goatownership.application.model.FarmGoatRegistryRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class FarmGoatRegistryApiMapperTest {

    private final FarmGoatRegistryApiMapper mapper = new FarmGoatRegistryApiMapper();

    @Test
    @DisplayName("Maps FarmGoatRegistryItem to FarmGoatRegistryResponseDTO preserving all fields and extracting GoatId value")
    void toResponse_mapsAllFieldsCorrectly() {
        var item = new FarmGoatRegistryItem(
                new GoatId(123L),
                "RG123",
                "Bella",
                GoatStatus.ATIVO,
                10L,
                "Capril Bela Vista",
                Set.of(FarmGoatRegistryRole.CREATOR, FarmGoatRegistryRole.CURRENT_OWNER),
                FarmGoatRegistryDisposition.CURRENT,
                10L
        );

        FarmGoatRegistryResponseDTO dto = mapper.toResponse(item);

        assertThat(dto).isNotNull();
        assertThat(dto.goatId()).isEqualTo(123L);
        assertThat(dto.registrationNumber()).isEqualTo("RG123");
        assertThat(dto.name()).isEqualTo("Bella");
        assertThat(dto.globalStatus()).isEqualTo(GoatStatus.ATIVO);
        assertThat(dto.creatorFarmId()).isEqualTo(10L);
        assertThat(dto.creatorNameSnapshot()).isEqualTo("Capril Bela Vista");
        assertThat(dto.roles()).containsExactlyInAnyOrder(FarmGoatRegistryRole.CREATOR, FarmGoatRegistryRole.CURRENT_OWNER);
        assertThat(dto.disposition()).isEqualTo(FarmGoatRegistryDisposition.CURRENT);
        assertThat(dto.currentOwnerFarmId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("Maps null item to null")
    void toResponse_nullItem_returnsNull() {
        assertThat(mapper.toResponse(null)).isNull();
    }

    @Test
    @DisplayName("Maps list of items preserving list order")
    void toResponseList_mapsListAndPreservesOrder() {
        var item1 = new FarmGoatRegistryItem(new GoatId(1L), "RG1", "G1", GoatStatus.ATIVO, null, null, Set.of(), FarmGoatRegistryDisposition.NONE, null);
        var item2 = new FarmGoatRegistryItem(new GoatId(2L), "RG2", "G2", GoatStatus.ATIVO, null, null, Set.of(), FarmGoatRegistryDisposition.NONE, null);

        List<FarmGoatRegistryResponseDTO> dtos = mapper.toResponseList(List.of(item1, item2));

        assertThat(dtos).hasSize(2);
        assertThat(dtos.get(0).goatId()).isEqualTo(1L);
        assertThat(dtos.get(1).goatId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("Maps null or empty list to empty list")
    void toResponseList_nullOrEmptyList_returnsEmptyList() {
        assertThat(mapper.toResponseList(null)).isEmpty();
        assertThat(mapper.toResponseList(List.of())).isEmpty();
    }
}
