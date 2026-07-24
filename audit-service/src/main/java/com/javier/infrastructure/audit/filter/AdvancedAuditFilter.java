package com.javier.infrastructure.audit.filter;

import com.javier.infrastructure.audit.wrapper.CachedBodyHttpServletRequest;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(3)
public class AdvancedAuditFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        CachedBodyHttpServletRequest wrapped = new CachedBodyHttpServletRequest(request);

        long start = System.currentTimeMillis();

        filterChain.doFilter(wrapped, response);

        long duration = System.currentTimeMillis() - start;

        String payload = wrapped.getReader().lines().collect(Collectors.joining());

        String correlationId = MDC.get("correlationId");
        String clientId = MDC.get("clientId");

        // Request metadata
        String method = request.getMethod();
        String path = request.getRequestURI();
        String ip = request.getRemoteAddr();

        log.info("[AUDIT] [{}] [{}] {} {} from {} processed in {} ms | payload: {}",
                correlationId,
                clientId,
                method,
                path,
                ip,
                duration,
                payload);
    }
}
