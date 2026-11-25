package org.javierGomez18.infrastructure.metrics.event;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class MetricEventListener {

    private final MeterRegistry registry;

    private Counter auditSuccess;
    private Counter auditFailure;
    private Counter auditConflict;
    private Counter auditUnprocessable;
    private Timer auditLatency;

    @EventListener
    public void init(org.springframework.context.event.ContextRefreshedEvent event) {
        log.info("Inicializando métricas de auditoría");

        this.auditSuccess = registry.counter("audit.events.success");
        this.auditFailure = registry.counter("audit.events.failure");
        this.auditConflict = registry.counter("audit.events.conflict");
        this.auditUnprocessable = registry.counter("audit.events.unprocessable");
        this.auditLatency = registry.timer("audit.events.latency");
    }

    @EventListener
    public void onMetricEvent(MetricEvent event) {
        log.debug("Recibido MetricEvent: {}", event);

        switch (event.getName()) {
            case "audit.event.success" -> auditSuccess.increment();
            case "audit.event.failure" -> auditFailure.increment();
            case "audit.event.conflict" -> auditConflict.increment();
            case "audit.event.unprocessable" -> auditUnprocessable.increment();
            case "audit.event.latency" -> {
                if (event.getValue() != null) {
                    auditLatency.record(event.getValue(), TimeUnit.MILLISECONDS);
                }
            }
            default -> log.warn("Evento de métrica desconocido: {}", event.getName());
        }
    }
}
