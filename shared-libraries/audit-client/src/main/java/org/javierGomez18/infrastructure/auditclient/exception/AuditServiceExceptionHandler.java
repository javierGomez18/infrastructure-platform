package org.javierGomez18.infrastructure.auditclient.exception;

import org.javierGomez18.infrastructure.audit.client.model.ProblemDetailRS;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Slf4j
@RestControllerAdvice
public class AuditServiceExceptionHandler {

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetailRS> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpServletRequest request
    ) {
        log.error("Jackson ha fallado antes del controller: {}", ex.getMessage());

        ProblemDetailRS body = new ProblemDetailRS(
                URI.create("/errors/payload-malformed"),
                "Error deserializando payload",
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                URI.create(request.getRequestURI()),
                OffsetDateTime.now(ZoneOffset.UTC),
                ex.getClass().getSimpleName()
        );

        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(AuditNotFoundException.class)
    public ResponseEntity<ProblemDetailRS> handleAuditNotFound(
            AuditNotFoundException ex,
            HttpServletRequest request
    ) {
        log.warn("AuditNotFoundException: {}", ex.getMessage());

        ProblemDetailRS body = new ProblemDetailRS(
                URI.create("/errors/audit/not-found"),
                "Audit no encontrado",
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                URI.create(request.getRequestURI()),
                OffsetDateTime.now(ZoneOffset.UTC),
                ex.getClass().getSimpleName()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(AuditConflictException.class)
    public ResponseEntity<ProblemDetailRS> handleAuditConflict(
            AuditConflictException ex,
            HttpServletRequest request
    ) {
        log.warn("AuditConflictException: {}", ex.getMessage());

        String typeUri = switch (ex.getTipo()) {
            case DUPLICATED_IDEMPOTENCY_KEY -> "/errors/audit/idempotency-key-duplicated";
            case DUPLICATED_CORRELATION_ID -> "/errors/audit/correlation-id-duplicated";
            case IDENTICAL_EVENT -> "/errors/audit/identical-event";
            default -> "/errors/audit/conflict";
        };

        ProblemDetailRS body = new ProblemDetailRS(
                URI.create(typeUri),
                "Conflicto con audit",
                HttpStatus.CONFLICT.value(),
                ex.getMessage(),
                URI.create(request.getRequestURI()),
                OffsetDateTime.now(ZoneOffset.UTC),
                ex.getClass().getSimpleName()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(AuditUnprocessableEntityException.class)
    public ResponseEntity<ProblemDetailRS> handleAuditUnprocessable(
            AuditUnprocessableEntityException ex,
            HttpServletRequest request
    ) {
        log.warn("AuditUnprocessableEntityException: {}", ex.getMessage());

        String typeUri = switch (ex.getTipo()) {
            case PAYLOAD_MALFORMED -> "/errors/audit/payload-malformed";
            case MISSING_REQUIRED_FIELDS -> "/errors/audit/missing-required-fields";
            case INVALID_ENTITY -> "/errors/audit/invalid-entity";
            default -> "/errors/audit/unprocessable";
        };

        ProblemDetailRS body = new ProblemDetailRS(
                URI.create(typeUri),
                "Audit no procesable",
                HttpStatus.UNPROCESSABLE_ENTITY.value(),
                ex.getMessage(),
                URI.create(request.getRequestURI()),
                OffsetDateTime.now(ZoneOffset.UTC),
                ex.getClass().getSimpleName()
        );

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(body);
    }

    @ExceptionHandler(AuditCommunicationException.class)
    public ResponseEntity<ProblemDetailRS> handleAuditCommunication(
            AuditCommunicationException ex,
            HttpServletRequest request
    ) {
        log.error("AuditCommunicationException [{}]: {}", ex.getTipo(), ex.getCause().getMessage(), ex.getCause());

        String typeUri = "/errors/audit/" + ex.getTipo().name().toLowerCase().replace("_", "-");

        HttpStatus status = HttpStatus.resolve(ex.getStatusCode());
        if (status == null) status = HttpStatus.INTERNAL_SERVER_ERROR;

        ProblemDetailRS body = new ProblemDetailRS(
                URI.create(typeUri),
                "Error de comunicación con AuditService",
                status.value(),
                ex.getCause().getMessage(),
                URI.create(request.getRequestURI()),
                OffsetDateTime.now(ZoneOffset.UTC),
                ex.getClass().getSimpleName()
        );

        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetailRS> handleIllegalArgument(
            IllegalArgumentException ex,
            HttpServletRequest request
    ) {
        log.warn("IllegalArgumentException: {}", ex.getMessage());

        ProblemDetailRS body = new ProblemDetailRS(
                URI.create("/errors/bad-request"),
                "Parámetros inválidos",
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                URI.create(request.getRequestURI()),
                OffsetDateTime.now(ZoneOffset.UTC),
                ex.getClass().getSimpleName()
        );

        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetailRS> handleGeneric(
            Exception ex,
            HttpServletRequest request
    ) {
        log.error("Excepción inesperada", ex);

        ProblemDetailRS body = new ProblemDetailRS(
                URI.create("/errors/unexpected"),
                "Excepción inesperada",
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                ex.getMessage(),
                URI.create(request.getRequestURI()),
                OffsetDateTime.now(ZoneOffset.UTC),
                ex.getClass().getSimpleName()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
