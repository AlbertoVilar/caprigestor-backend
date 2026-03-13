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

        var searchResult = adapter.search(GoatAbccSearchRequestVO.builder()
                .raceId(9)
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
    }
}

