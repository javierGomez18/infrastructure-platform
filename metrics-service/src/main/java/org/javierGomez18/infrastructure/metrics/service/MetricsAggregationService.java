package org.javierGomez18.infrastructure.metrics.service;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MetricsAggregationService {

    private final MeterRegistry registry;

    public double getAuditSuccessRate() {
        double success = registry.counter("audit.events.success").count();
        double failure = registry.counter("audit.events.failure").count();

        double total = success + failure;
        return total == 0 ? 0 : (success / total);
    }
}
