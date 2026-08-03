package org.javierGomez18.infrastructure.auditclient.feign;

import org.javierGomez18.infrastructure.audit.client.api.AuditApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(
        name = "audit-service",
        url = "${client.feign.audit.service.url}",
        configuration = AuditFeignConfig.class,
        fallbackFactory = AuditFallbackFactory.class
)
public interface AuditFeignClient extends AuditApi {
}
