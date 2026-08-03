package org.javierGomez18.infrastructure.sharedfilters.filters;

import org.javierGomez18.infrastructure.sharedfilters.config.ClientWhitelistProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Order(2)
public class HeaderValidationFilter extends OncePerRequestFilter {

    private final ClientWhitelistProperties whitelist;

    private static final List<String> REQUIRED_HEADERS = List.of(
            "X-Client-Id"
    );

    public HeaderValidationFilter(ClientWhitelistProperties whitelist) {
        this.whitelist = whitelist;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        final String clientId = request.getHeader("X-Client-Id");

        for (String header : REQUIRED_HEADERS) {
            if (request.getHeader(header) == null) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                        "[SECURITY] Missing required header: " + header);
                log.warn("[SECURITY] Missing required header: {}", header);
                return;
            }
            if (!whitelist.getAllowedClients().contains(clientId)) {
                log.warn("[SECURITY] Client '{}' rejected — not in whitelist", clientId);
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("Client not allowed: " + clientId);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
