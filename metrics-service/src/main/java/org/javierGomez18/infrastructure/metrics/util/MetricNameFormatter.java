package org.javierGomez18.infrastructure.metrics.util;

public class MetricNameFormatter {
    public static String format(String raw) {
        return raw.replace(" ", "_").toLowerCase();
    }
}
