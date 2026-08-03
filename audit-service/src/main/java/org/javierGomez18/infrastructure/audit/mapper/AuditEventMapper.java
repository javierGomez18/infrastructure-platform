package org.javierGomez18.infrastructure.audit.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.javierGomez18.infrastructure.audit.domain.AuditEvent;
import org.javierGomez18.infrastructure.audit.persistence.AuditEventEntity;
import org.javierGomez18.infrastructure.audit.model.AuditEventRQ;
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

