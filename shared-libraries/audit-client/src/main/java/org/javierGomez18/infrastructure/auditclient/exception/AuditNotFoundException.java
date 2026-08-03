package org.javierGomez18.infrastructure.auditclient.exception;

import lombok.Getter;

@Getter
public class AuditNotFoundException extends RuntimeException {

  public enum Tipo {
    BY_ID
  }

  private final Tipo tipo;

  public AuditNotFoundException(Long id) {
    super("AuditEvent no encontrado con ID: " + id);
    this.tipo = Tipo.BY_ID;
  }
}
