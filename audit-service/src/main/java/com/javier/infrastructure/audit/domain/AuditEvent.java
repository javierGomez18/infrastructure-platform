package com.javier.infrastructure.audit.domain;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuditEvent {
    private String microservice;
    private String action;
    private String entity;
    private String entityId;
    private String correlationId;
    private JsonNode payload;
}
