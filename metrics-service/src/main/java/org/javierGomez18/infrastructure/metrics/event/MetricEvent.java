package org.javierGomez18.infrastructure.metrics.event;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor
@Getter
@Builder
@ToString
public class MetricEvent {

    private final String name;
    private final Long value;

    public MetricEvent(String name) {
        this.name = name;
        this.value = null;
    }
}
