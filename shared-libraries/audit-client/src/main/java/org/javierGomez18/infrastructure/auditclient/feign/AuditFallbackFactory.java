package org.javierGomez18.infrastructure.auditclient.feign;

import org.javierGomez18.infrastructure.audit.client.model.AuditEventRQ;
import org.javierGomez18.infrastructure.audit.client.model.AuditEventRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class AuditFallbackFactory implements FallbackFactory<AuditFeignClient> {
    @Override
    public AuditFeignClient create(Throwable cause) {

        log.error("Fallback activado en AuditFeignClient por causa: {}", cause.toString(), cause);

        return new AuditFeignClient() {

            @Override
            public ResponseEntity<Void> createAuditEvent(AuditEventRQ request) {
                throw AuditExceptionFactory.fromThrowable(cause);
            }

            @Override
            public ResponseEntity<List<AuditEventRecord>> getAuditEvents() {
                return null;
            }
        };
    }
}
