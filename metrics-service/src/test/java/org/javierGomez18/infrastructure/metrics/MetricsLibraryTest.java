package org.javierGomez18.infrastructure.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import org.javierGomez18.infrastructure.metrics.config.MetricsConfig;
import org.javierGomez18.infrastructure.metrics.event.MetricEvent;
import org.javierGomez18.infrastructure.metrics.event.MetricEventListener;
import org.javierGomez18.infrastructure.metrics.event.MetricEventPublisher;
import org.springframework.boot.actuate.autoconfigure.metrics.CompositeMeterRegistryAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.export.prometheus.PrometheusMetricsExportAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.junit.jupiter.api.Test;

/**
 * La librería se prueba junto a la autoconfiguración REAL de Actuator/Prometheus de Boot, que es
 * donde fallaba: un @Configuration propio llamado PrometheusConfig chocaba con el bean
 * "prometheusConfig" de Boot y la aplicación no arrancaba.
 */
class MetricsLibraryTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    MetricsAutoConfiguration.class,
                    CompositeMeterRegistryAutoConfiguration.class,
                    PrometheusMetricsExportAutoConfiguration.class))
            .withUserConfiguration(MetricsConfig.class, MetricEventListener.class, MetricEventPublisher.class)
            .withPropertyValues("spring.application.name=aplicacion-consumidora");

    @Test
    void arrancaJuntoALaAutoconfiguracionDePrometheusDeBoot() {
        runner.run(context -> assertThat(context).hasNotFailed());
    }

    @Test
    void losEventosIncrementanSusContadores() {
        runner.run(context -> {
            MetricEventPublisher publisher = context.getBean(MetricEventPublisher.class);
            MeterRegistry registry = context.getBean(MeterRegistry.class);

            publisher.publish("audit.event.success");
            publisher.publish("audit.event.success");
            publisher.publish("audit.event.failure");
            publisher.publish("audit.event.latency", 120L);

            assertThat(registry.get("audit.events.success").counter().count()).isEqualTo(2.0);
            assertThat(registry.get("audit.events.failure").counter().count()).isEqualTo(1.0);
            assertThat(registry.get("audit.events.latency").timer().count()).isEqualTo(1L);
        });
    }

    @Test
    void unEventoDesconocidoNoRompeElListener() {
        runner.run(context -> {
            context.publishEvent(new MetricEvent("evento.que.no.existe"));
            assertThat(context).hasNotFailed();
        });
    }

    @Test
    void lasEtiquetasComunesSonLasDeLaAplicacionConsumidoraYNoLasDeLaLibreria() {
        runner.run(context -> {
            context.getBean(MetricEventPublisher.class).publish("audit.event.success");

            String scrape = context.getBean(PrometheusMeterRegistry.class).scrape();

            assertThat(scrape).contains("application=\"aplicacion-consumidora\"");
            assertThat(scrape).doesNotContain("application=\"metrics-service\"");
        });
    }
}
