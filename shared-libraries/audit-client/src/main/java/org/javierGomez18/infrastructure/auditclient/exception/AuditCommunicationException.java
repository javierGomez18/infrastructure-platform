package org.javierGomez18.infrastructure.auditclient.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public class AuditCommunicationException extends RuntimeException {
    public enum Tipo {
        BY_CONNECTION_REFUSED,
        BY_CONNECTION_CLOSED,
        BY_CALL_NOT_PERMITTED,
        BY_CIRCUIT_OPENED,
        BY_BULKHEAD_FULL,
        BY_TIMEOUT,
        BY_DNS_ERROR,
        BY_SSL_ERROR,
        BY_UNKNOWN_HOST_ERROR,
        BY_REMOTE_5XX,
        BY_REMOTE_4XX,
        BY_UNKNOWN_ERROR
    }

    private final Tipo tipo;
    private final int statusCode;
    private final Throwable cause;

    public AuditCommunicationException(Tipo tipo, Integer status, String detail, String instance, Throwable cause) {
        super("Error "+status.toString()+" con AuditService. Excepcion: "+cause.getMessage());
        this.tipo = tipo;
        this.statusCode = status;
        this.cause = cause;
    }
}
