package org.javierGomez18.infrastructure.auditclient.dto;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum AuditAction {
    CREATE,
    UPDATE,
    DELETE,
    GET,
    LOGIN,
    LOGOUT,
    PROCESS;

    private String value;
}
