package com.devmaster.goatfarm.goat.integration.abcc.adapter;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GoatAbccPublicHttpAdapterTest {

    @Test
    void preservesTheAlphabeticSuffixOfAnAbccRegistration() {
        assertThat(GoatAbccPublicHttpAdapter.normalizeRegistration(" 1635719026a "))
                .isEqualTo("1635719026A");
    }
}
