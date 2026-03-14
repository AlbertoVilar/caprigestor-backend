package com.devmaster.goatfarm.goat.integration.abcc;

import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccSearchRequestVO;
import com.devmaster.goatfarm.goat.integration.abcc.adapter.GoatAbccPublicHttpAdapter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import static org.assertj.core.api.Assertions.assertThat;

@EnabledIfSystemProperty(named = "abcc.integration.enabled", matches = "true")
class GoatAbccPublicHttpAdapterIntegrationTest {

    @Test
    void shouldSearchAndPreviewUsingRealAbccPublicService() {
        GoatAbccPublicHttpAdapter adapter = new GoatAbccPublicHttpAdapter();

        var raceOptions = adapter.listRaces();
        assertThat(raceOptions).isNotEmpty();

        Integer raceId = raceOptions.stream()
                .filter(option -> "SAANEN".equalsIgnoreCase(option.getName()))
                .map(option -> option.getId())
                .findFirst()
                .orElse(9);

        var searchResult = adapter.search(GoatAbccSearchRequestVO.builder()
                .raceId(raceId)
                .affix("CRS")
                .page(1)
                .build());

        assertThat(searchResult).isNotNull();
        assertThat(searchResult.getItems()).isNotEmpty();
        assertThat(searchResult.getItems().getFirst().getExternalId()).isNotBlank();

        String externalId = searchResult.getItems().getFirst().getExternalId();
        var preview = adapter.preview(externalId);

        assertThat(preview).isNotNull();
        assertThat(preview.getExternalId()).isEqualTo(externalId);
        assertThat(preview.getNome()).isNotBlank();
        assertThat(preview.getRegistro()).isNotBlank();

        var complementary = adapter.findGenealogyByRegistrationNumber(preview.getRegistro());
        assertThat(complementary).isPresent();
        assertThat(complementary.get().getAnimalRegistrationNumber()).isNotBlank();
        assertThat(complementary.get().getFatherName()).isNotBlank();
    }
}

