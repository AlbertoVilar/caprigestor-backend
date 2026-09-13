package com.devmaster.goatfarm.config;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.IOException;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

class ProductionReadinessConfigurationTest {

    @Test
    void shouldKeepMailHealthExplicitlyConfigurableInTheProductionProfile() throws IOException {
        Properties properties = PropertiesLoaderUtils.loadAllProperties("application-prod.properties");

        assertThat(properties.getProperty("management.health.mail.enabled"))
                .isEqualTo("${MAIL_HEALTH_ENABLED:true}");
    }
}
