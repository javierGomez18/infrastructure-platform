package com.javier.infrastructure.audit.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.javier.infrastructure.audit.domain.AuditEvent;
import com.javier.infrastructure.audit.persistence.AuditEventEntity;
import org.javier.infrastructure.audit.model.AuditEventRQ;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AuditEventMapper {

    ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Mapping(target = "id", ignore = true)
    AuditEventEntity toEntity(AuditEvent auditEvent);

    AuditEvent toDomain(AuditEventRQ dto);

    default JsonNode toJsonNode(Object value) {
        if (value == null) {
            return null;
        }

        try {
            if (value instanceof JsonNode) {
                return (JsonNode) value;
            }

            if (value instanceof String) {
                return OBJECT_MAPPER.readTree((String) value);
            }

            return OBJECT_MAPPER.valueToTree(value);

        } catch (Exception e) {
            throw new RuntimeException("Error converting payload to JsonNode", e);
        }
    }
}

