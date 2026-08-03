package org.javierGomez18.infrastructure.auditclient.feign;

import org.javierGomez18.infrastructure.auditclient.exception.AuditCommunicationException;
import feign.RetryableException;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.security.cert.CertificateException;
import java.util.concurrent.TimeoutException;

public class AuditExceptionFactory {

    private static final String INSTANCE = "/api/v1/audit";

    public static AuditCommunicationException fromThrowable(Throwable cause) {

        if (cause instanceof CallNotPermittedException) {
            return new AuditCommunicationException(
                    AuditCommunicationException.Tipo.BY_CIRCUIT_OPENED,
                    503,
                    "Circuit breaker open",
                    INSTANCE,
                    cause
            );
        }

        if (cause instanceof BulkheadFullException) {
            return new AuditCommunicationException(
                    AuditCommunicationException.Tipo.BY_BULKHEAD_FULL,
                    429,
                    "Bulkhead full",
                    INSTANCE,
                    cause
            );
        }

        if (cause instanceof SocketTimeoutException || cause instanceof TimeoutException) {
            return new AuditCommunicationException(
                    AuditCommunicationException.Tipo.BY_TIMEOUT,
                    504,
                    cause.getMessage(),
                    INSTANCE,
                    cause
            );
        }

        if (cause instanceof UnknownHostException) {
            return new AuditCommunicationException(
                    AuditCommunicationException.Tipo.BY_DNS_ERROR,
                    502,
                    cause.getMessage(),
                    INSTANCE,
                    cause
            );
        }

        if (cause instanceof SSLHandshakeException ||
                cause instanceof SSLException ||
                cause instanceof CertificateException) {
            return new AuditCommunicationException(
                    AuditCommunicationException.Tipo.BY_SSL_ERROR,
                    502,
                    cause.getMessage(),
                    INSTANCE,
                    cause
            );
        }

        if (cause instanceof ConnectException) {
            return new AuditCommunicationException(
                    AuditCommunicationException.Tipo.BY_CONNECTION_REFUSED,
                    503,
                    cause.getMessage(),
                    INSTANCE,
                    cause
            );
        }

        if (cause instanceof RetryableException retry) {

            String msg = retry.getMessage().toLowerCase();

            if (msg.contains("connection refused")) {
                return new AuditCommunicationException(
                        AuditCommunicationException.Tipo.BY_CONNECTION_REFUSED,
                        503,
                        retry.getMessage(),
                        INSTANCE,
                        retry
                );
            }

            if (msg.contains("connection reset") || msg.contains("connection closed")) {
                return new AuditCommunicationException(
                        AuditCommunicationException.Tipo.BY_CONNECTION_CLOSED,
                        502,
                        retry.getMessage(),
                        INSTANCE,
                        retry
                );
            }

            if (msg.contains("timed out") || msg.contains("timeout")) {
                return new AuditCommunicationException(
                        AuditCommunicationException.Tipo.BY_TIMEOUT,
                        504,
                        retry.getMessage(),
                        INSTANCE,
                        retry
                );
            }

            return new AuditCommunicationException(
                    AuditCommunicationException.Tipo.BY_REMOTE_5XX,
                    500,
                    retry.getMessage(),
                    INSTANCE,
                    retry
            );
        }

        if (cause instanceof IOException) {
            return new AuditCommunicationException(
                    AuditCommunicationException.Tipo.BY_REMOTE_5XX,
                    500,
                    cause.getMessage(),
                    INSTANCE,
                    cause
            );
        }

        return new AuditCommunicationException(
                AuditCommunicationException.Tipo.BY_UNKNOWN_ERROR,
                500,
                cause.getMessage(),
                INSTANCE,
                cause
        );
    }
}
