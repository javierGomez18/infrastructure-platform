package org.javierGomez18.infrastructure.audit.service;

import org.javierGomez18.infrastructure.audit.domain.AuditEvent;
import org.javierGomez18.infrastructure.audit.mapper.AuditEventMapper;
import org.javierGomez18.infrastructure.audit.persistence.AuditEventEntity;
import org.javierGomez18.infrastructure.audit.persistence.AuditEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditEventService {

    private final AuditEventRepository repository;
    private final AuditEventMapper mapper;

    public void save(AuditEvent event) {
        repository.save(mapper.toEntity(event));
    }

    public List<AuditEventEntity> findAll() {
        return repository.findAll();
    }
}

