package org.javierGomez18.infrastructure.auditclient.dto;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum AuditEntity {

    CLIENTE,
    CUENTA,
    MOVIMIENTO;

    private String value;
}
