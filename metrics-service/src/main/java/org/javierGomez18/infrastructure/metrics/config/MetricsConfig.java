package org.javierGomez18.infrastructure.metrics.config;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {

    /**
     * Etiquetas comunes con la identidad de la aplicación que usa esta librería (no la de la
     * librería), y aplicadas por el customizer de Boot antes de registrar ningún medidor.
     */
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> commonTagsCustomizer(
            @Value("${spring.application.name:unknown}") String application,
            @Value("${spring.profiles.active:default}") String environment) {
        return registry -> registry.config().commonTags(
                "application", application,
                "environment", environment
        );
    }
}
