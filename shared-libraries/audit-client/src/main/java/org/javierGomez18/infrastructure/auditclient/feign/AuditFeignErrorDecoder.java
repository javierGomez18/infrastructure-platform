package org.javierGomez18.infrastructure.auditclient.feign;

import org.javierGomez18.infrastructure.auditclient.exception.AuditCommunicationException;
import org.javierGomez18.infrastructure.auditclient.exception.AuditConflictException;
import org.javierGomez18.infrastructure.auditclient.exception.AuditNotFoundException;
import org.javierGomez18.infrastructure.auditclient.exception.AuditUnprocessableEntityException;
import feign.Response;
import feign.codec.ErrorDecoder;

public class AuditFeignErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {

        int status = response.status();
        String detail = response.reason();
        String instance = "/api/v1/audit";

        switch (status) {

            case 404:
                return new AuditNotFoundException(null);

            case 409:
                return new AuditConflictException("idempotency-key");

            case 422:
                return new AuditUnprocessableEntityException("payload");

            case 500:
                return new AuditCommunicationException(
                        AuditCommunicationException.Tipo.BY_REMOTE_5XX,
                        500,
                        detail,
                        instance,
                        null
                );

            case 503:
                return new AuditCommunicationException(
                        AuditCommunicationException.Tipo.BY_REMOTE_5XX,
                        503,
                        detail,
                        instance,
                        null
                );

            case 504:
                return new AuditCommunicationException(
                        AuditCommunicationException.Tipo.BY_TIMEOUT,
                        504,
                        detail,
                        instance,
                        null
                );

            default:
                return new AuditCommunicationException(
                        status >= 500
                                ? AuditCommunicationException.Tipo.BY_REMOTE_5XX
                                : AuditCommunicationException.Tipo.BY_REMOTE_4XX,
                        status,
                        detail,
                        instance,
                        null
                );
        }
    }
}