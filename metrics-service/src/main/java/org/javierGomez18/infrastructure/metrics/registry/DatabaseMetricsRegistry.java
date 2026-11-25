package org.javierGomez18.infrastructure.metrics.registry;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DatabaseMetricsRegistry {

    private final MeterRegistry registry;

    public void registerQueryTimer(String name) {
        registry.timer("database.query." + name);
    }
}
