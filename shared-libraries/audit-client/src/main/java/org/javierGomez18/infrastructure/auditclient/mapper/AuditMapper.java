package org.javierGomez18.infrastructure.auditclient.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.javierGomez18.infrastructure.audit.client.model.AuditEventRQ;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.HashMap;
import java.util.Map;

@Mapper(componentModel = "spring")
public interface AuditMapper {
    @Mapping(target = "microservice", source = "microservice")
    @Mapping(target = "action", source = "action")
    @Mapping(target = "entity", source = "entity")
    @Mapping(target = "entityId", expression = "java(entityId != null ? String.valueOf(entityId) : null)")
    @Mapping(target = "correlationId", source = "correlationId")
    @Mapping(target = "payload", expression = "java(toPayload(request))")
    @Mapping(target = "timestamp", expression = "java(java.time.OffsetDateTime.now())")
    AuditEventRQ toAuditEventRQ(
            Object request,
            String microservice,
            String action,
            String entity,
            Long entityId,
            String correlationId
    );

    default Map<String, Object> toPayload(Object request) {
        if (request == null) return null;

        ObjectMapper mapper = new ObjectMapper();

        if (request instanceof Map<?, ?> map) {
            return mapper.convertValue(map, Map.class);
        }
        if (request instanceof JsonNode node) {
            if (node.isValueNode()) {
                Map<String, Object> wrapper = new HashMap<>();
                wrapper.put("value", mapper.convertValue(node, Object.class));
                return wrapper;
            }
            return mapper.convertValue(node, Map.class);
        }
        if (request instanceof String ||
                request instanceof Number ||
                request instanceof Boolean) {

            Map<String, Object> wrapper = new HashMap<>();
            wrapper.put("value", request);
            return wrapper;
        }

        return mapper.convertValue(request, Map.class);
    }
}
