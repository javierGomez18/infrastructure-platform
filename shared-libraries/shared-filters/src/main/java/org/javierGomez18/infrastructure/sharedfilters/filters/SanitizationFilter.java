package org.javierGomez18.infrastructure.sharedfilters.filters;

import org.javierGomez18.infrastructure.sharedfilters.sanitization.SanitizedRequestWrapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Order(3)
public class SanitizationFilter extends OncePerRequestFilter{

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        SanitizedRequestWrapper sanitizedRequest =
                new SanitizedRequestWrapper(request);

        filterChain.doFilter(sanitizedRequest, response);
    }
}
