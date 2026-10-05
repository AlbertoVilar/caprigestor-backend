package com.devmaster.goatfarm.config.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PasswordHashingAdapterTest {

    @Test
    void delegatesHashingToConfiguredPasswordEncoder() {
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        when(encoder.encode("raw-password")).thenReturn("encoded-password");

        PasswordHashingAdapter adapter = new PasswordHashingAdapter(encoder);

        assertThat(adapter.hash("raw-password")).isEqualTo("encoded-password");
        verify(encoder).encode("raw-password");
    }
}
