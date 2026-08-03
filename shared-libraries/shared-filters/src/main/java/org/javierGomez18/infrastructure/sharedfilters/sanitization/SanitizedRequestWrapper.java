package org.javierGomez18.infrastructure.sharedfilters.sanitization;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class SanitizedRequestWrapper extends HttpServletRequestWrapper {

    private final Map<String, String[]> sanitizedParams = new HashMap<>();
    private final byte[] sanitizedBody;

    public SanitizedRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);

        request.getParameterMap().forEach((key, values) -> {
            String[] sanitizedValues = new String[values.length];
            for (int i = 0; i < values.length; i++) {
                sanitizedValues[i] = sanitize(values[i]);
            }
            sanitizedParams.put(key, sanitizedValues);
        });

        byte[] originalBody = request.getInputStream().readAllBytes();

        if (originalBody.length == 0) {
            this.sanitizedBody = originalBody;
            return;
        }

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(originalBody);
        sanitizeNode(root);
        this.sanitizedBody = mapper.writeValueAsBytes(root);
    }

    private void sanitizeNode(JsonNode node) {
        if (node.isObject()) {
            node.fields().forEachRemaining(entry -> {
                JsonNode child = entry.getValue();
                if (child.isTextual()) {
                    ((ObjectNode) node).put(entry.getKey(), sanitize(child.asText()));
                } else {
                    sanitizeNode(child);
                }
            });
        } else if (node.isArray()) {
            for (JsonNode child : node) {
                sanitizeNode(child);
            }
        }
    }

    private String sanitize(String input) {
        if (input == null) return null;
        return input.replaceAll("[<>]", "");
    }

    @Override
    public String getParameter(String name) {
        String[] values = sanitizedParams.get(name);
        if (values == null || values.length == 0) return null;
        return values[0];
    }

    @Override
    public String[] getParameterValues(String name) {
        return sanitizedParams.get(name);
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return sanitizedParams;
    }

    @Override
    public ServletInputStream getInputStream() {
        ByteArrayInputStream byteArrayInputStream =
                new ByteArrayInputStream(this.sanitizedBody);

        return new ServletInputStream() {
            @Override
            public boolean isFinished() {
                return byteArrayInputStream.available() == 0;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setReadListener(ReadListener listener) {}

            @Override
            public int read() {
                return byteArrayInputStream.read();
            }
        };
    }

    @Override
    public BufferedReader getReader() {
        return new BufferedReader(
                new InputStreamReader(getInputStream(), StandardCharsets.UTF_8)
        );
    }
}