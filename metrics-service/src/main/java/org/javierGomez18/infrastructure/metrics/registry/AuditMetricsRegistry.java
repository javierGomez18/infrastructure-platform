package org.javierGomez18.infrastructure.metrics.registry;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuditMetricsRegistry {

    private final MeterRegistry registry;

    public Counter customAuditCounter(String name) {
        return registry.counter("audit.custom." + name);
    }
}
