package org.javierGomez18.infrastructure.audit.controller;

import org.javierGomez18.infrastructure.audit.mapper.AuditEventMapper;
import org.javierGomez18.infrastructure.audit.service.AuditEventService;
import lombok.RequiredArgsConstructor;
import org.javierGomez18.infrastructure.audit.api.AuditApi;
import org.javierGomez18.infrastructure.audit.model.AuditEventRQ;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuditController implements AuditApi {

    private final AuditEventService service;
    private final AuditEventMapper mapper;

    @Override
    public ResponseEntity<Void> createAuditEvent(AuditEventRQ dto) {

        service.save(mapper.toDomain(dto));
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
