package com.devmaster.goatfarm.config.security;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;

import static org.assertj.core.api.Assertions.assertThat;

class JwtDurationProfileConfigurationTest {

    @Test
    void devProfileDefaultsToTwentyFourHoursForManualQa() {
        contextForProfile("dev").run(context ->
                assertThat(context.getEnvironment().getProperty("security.jwt.duration", Long.class))
                        .isEqualTo(86_400L));
    }

    @Test
    void testProfileKeepsTheSafeFifteenMinuteFallback() {
        contextForProfile("test").run(context ->
                assertThat(context.getEnvironment().getProperty("security.jwt.duration", Long.class))
                        .isEqualTo(900L));
    }

    @Test
    void devProfileCanBeExplicitlyOverriddenForQa() {
        new ApplicationContextRunner()
                .withInitializer(new ConfigDataApplicationContextInitializer())
                .withPropertyValues("spring.profiles.active=dev", "JWT_DURATION=1800")
                .run(context ->
                        assertThat(context.getEnvironment().getProperty("security.jwt.duration", Long.class))
                                .isEqualTo(1_800L));
    }

    private ApplicationContextRunner contextForProfile(String profile) {
        return new ApplicationContextRunner()
                .withInitializer(new ConfigDataApplicationContextInitializer())
                .withPropertyValues("spring.profiles.active=" + profile);
    }
}
