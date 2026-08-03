package org.javierGomez18.infrastructure.auditclient.feign;

import feign.Logger;
import feign.Request;
import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuditFeignConfig {

    @Bean
    public ErrorDecoder auditErrorDecoder() {
        return new AuditFeignErrorDecoder();
    }

    @Bean
    public AuditFallbackFactory auditFallbackFactory() {
        return new AuditFallbackFactory();
    }

    @Bean
    public Request.Options feignOptions() {
        return new Request.Options(
                2000,
                5000
        );
    }

    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }

    @Bean
    public RequestInterceptor auditClientIdInterceptor(
            @Value("${spring.security.oauth2.client.registration.keycloak.client-id}") String clientId
    ) {
        return template -> template.header("X-Client-Id", clientId);
    }
}
