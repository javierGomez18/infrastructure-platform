package org.javierGomez18.infrastructure.auditclient.dto;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum AuditMicroservice {
    CLIENTES_CUENTAS,
    AUDIT_SERVICE,
    RETAIL;

    private String value;
}
