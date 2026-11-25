package org.javierGomez18.infrastructure.metrics.event;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MetricEventPublisher {

    private final ApplicationEventPublisher publisher;

    public void publish(String name) {
        publisher.publishEvent(new MetricEvent(name));
    }

    public void publish(String name, long value) {
        publisher.publishEvent(new MetricEvent(name, value));
    }
}
