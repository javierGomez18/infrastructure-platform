package org.javierGomez18.infrastructure.auditclient.exception;

import lombok.Getter;

@Getter
public class AuditUnprocessableEntityException extends RuntimeException {
  public enum Tipo {
    PAYLOAD_MALFORMED,
    MISSING_REQUIRED_FIELDS,
    INVALID_ENTITY
  }

  private final Tipo tipo;

  public AuditUnprocessableEntityException(String payload) {
    super("El payload enviado es mal formado.");
    this.tipo = Tipo.PAYLOAD_MALFORMED;
  }

  public AuditUnprocessableEntityException(Tipo tipo) {
    super("Faltan campos requeridos.");
    this.tipo = tipo;
  }

  public AuditUnprocessableEntityException(String entity, Tipo tipo) {
    super("La entidad enviada no es válida.");
    this.tipo = tipo;
  }
}
