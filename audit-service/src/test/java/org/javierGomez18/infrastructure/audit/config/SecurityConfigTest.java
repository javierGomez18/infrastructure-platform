package org.javierGomez18.infrastructure.audit.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Keycloak publica los roles en el claim anidado resource_access.<client>.roles. El conversor
 * estándar de Spring no lo resolvía (devolvía siempre vacío) y hasRole() denegaba todo.
 */
class SecurityConfigTest {

    private final SecurityConfig config = new SecurityConfig();

    private Jwt jwtConClaims(Map<String, Object> claims) {
        Jwt.Builder builder = Jwt.withTokenValue("token")
                .header("alg", "none")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(60))
                .subject("servicio");
        claims.forEach(builder::claim);
        return builder.build();
    }

    private List<String> authorities(Jwt jwt) {
        return config.jwtAuthenticationConverter().convert(jwt).getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
    }

    @Test
    void conviertePrefijaYExtraeLosRolesDelClienteAuditService() {
        Jwt jwt = jwtConClaims(Map.of("resource_access",
                Map.of("audit-service", Map.of("roles", List.of("audit.write", "audit.read")))));

        assertThat(authorities(jwt)).containsExactlyInAnyOrder("ROLE_audit.write", "ROLE_audit.read");
    }

    @Test
    void ignoraLosRolesDeOtrosClientes() {
        Jwt jwt = jwtConClaims(Map.of("resource_access",
                Map.of("otro-cliente", Map.of("roles", List.of("audit.write")))));

        assertThat(authorities(jwt)).isEmpty();
    }

    @Test
    void sinResourceAccessNoHayPermisosYNoFalla() {
        assertThat(authorities(jwtConClaims(Map.of()))).isEmpty();
    }

    @Test
    void conResourceAccessSinRolesNoFalla() {
        Jwt jwt = jwtConClaims(Map.of("resource_access", Map.of("audit-service", Map.of())));

        assertThat(authorities(jwt)).isEmpty();
    }
}
