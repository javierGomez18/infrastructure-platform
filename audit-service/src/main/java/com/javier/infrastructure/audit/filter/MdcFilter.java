package com.javier.infrastructure.audit.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(2)
public class MdcFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String correlationId = request.getHeader("X-Correlation-Id");
            String clientId = request.getHeader("X-Client-Id");

            MDC.put("correlationId", correlationId != null ? correlationId : UUID.randomUUID().toString());
            MDC.put("clientId", clientId != null ? clientId : "N/A");
            MDC.put("method", request.getMethod());
            MDC.put("path", request.getRequestURI());
            MDC.put("ip", request.getRemoteAddr());

            filterChain.doFilter(request, response);

        } finally {
            MDC.clear();
        }
    }
}
