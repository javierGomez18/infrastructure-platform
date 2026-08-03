package org.javierGomez18.infrastructure.auditclient.exception;

import lombok.Getter;

@Getter
public class AuditConflictException extends RuntimeException {
  public enum Tipo {
    DUPLICATED_IDEMPOTENCY_KEY,
    DUPLICATED_CORRELATION_ID,
    IDENTICAL_EVENT
  }

  private final Tipo tipo;

  public AuditConflictException(String idempotencyKey) {
    super("Idempotency key already exists");
    this.tipo = Tipo.DUPLICATED_IDEMPOTENCY_KEY;
  }

  public AuditConflictException(String correlationId, Tipo tipo) {
    super("Correlation ID already exists");
    this.tipo = Tipo.DUPLICATED_CORRELATION_ID;
  }
}
