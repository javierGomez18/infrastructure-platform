package org.javierGomez18.infrastructure.sharedfilters.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Order(1)
public class MdcFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String traceId = Optional.ofNullable(request.getHeader("X-Trace-Id"))
                .orElse(UUID.randomUUID().toString());
        String correlationId = request.getHeader("X-Correlation-Id");
        String clientId = request.getHeader("X-Client-Id");
        String spanId = UUID.randomUUID().toString();

        MDC.put("traceId", traceId);
        MDC.put(
                "correlationId", correlationId != null ? correlationId : UUID.randomUUID().toString());
        MDC.put("clientId", clientId != null ? clientId : "N/A");
        MDC.put("spanId", spanId);
        MDC.put("path", request.getRequestURI());
        MDC.put("method", request.getMethod());
        MDC.put("clientIp", request.getRemoteAddr());

        response.addHeader("X-Trace-Id", traceId);
        response.addHeader("X-Span-Id", spanId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}
