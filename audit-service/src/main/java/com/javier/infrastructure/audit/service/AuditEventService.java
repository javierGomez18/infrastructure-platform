package com.javier.infrastructure.audit.service;

import com.javier.infrastructure.audit.domain.AuditEvent;
import com.javier.infrastructure.audit.mapper.AuditEventMapper;
import com.javier.infrastructure.audit.persistence.AuditEventEntity;
import com.javier.infrastructure.audit.persistence.AuditEventRepository;
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

