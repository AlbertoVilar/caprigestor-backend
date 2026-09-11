package com.devmaster.goatfarm.goat.application.routing;

import com.devmaster.goatfarm.goat.application.ports.out.GoatReference;
import com.devmaster.goatfarm.goat.application.ports.out.GoatReferenceQueryPort;
import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goat.enums.Gender;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GoatReferenceResolverTest {

    private final GoatReferenceQueryPort queryPort = mock(GoatReferenceQueryPort.class);
    private final GoatReferenceResolver resolver = new GoatReferenceResolver(queryPort);
    private final GoatReference reference = new GoatReference(new GoatId(42L), 7L, "12342", "Matriz", Gender.FEMEA);

    @Test
    void resolvesExplicitTechnicalTokenThroughTechnicalPort() {
        when(queryPort.findReferenceByTechnicalIdAndFarmId(new GoatId(42L), 7L)).thenReturn(Optional.of(reference));

        assertThat(resolver.resolve("technical-42", 7L)).contains(reference);
        verify(queryPort).findReferenceByTechnicalIdAndFarmId(new GoatId(42L), 7L);
    }

    @Test
    void treatsBareNumericTokenAsRegistrationNumberWithoutTechnicalGuessing() {
        when(queryPort.findReferenceByRegistrationNumberAndFarmId("42", 7L)).thenReturn(Optional.of(reference));

        assertThat(resolver.resolve("42", 7L)).contains(reference);
        verify(queryPort).findReferenceByRegistrationNumberAndFarmId("42", 7L);
    }
}
