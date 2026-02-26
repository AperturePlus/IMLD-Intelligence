package xenosoft.imldintelligence.module.audit.internal.service.impl;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import xenosoft.imldintelligence.module.audit.internal.config.AuditProperties;

@Component
public class AuditPayloadSanitizer {
    private static final String MASKED_VALUE = "******";

    private final AuditProperties properties;
    private final ObjectMapper objectMapper;

    public AuditPayloadSanitizer(AuditProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    public JsonNode sanitize(JsonNode payload) {
        if (payload == null || payload.isNull()) {
            return payload;
        }

        JsonNode sanitized = properties.isMaskingEnabled() ? maskSensitiveFields(payload) : payload.deepCopy();
        byte[] rawBytes = writeBytes(sanitized);
        if (rawBytes.length <= properties.getMaxPayloadBytes()) {
            return sanitized;
        }

        ObjectNode truncated = objectMapper.createObjectNode();
        truncated.put("_truncated", true);
        truncated.put("_originalSizeBytes", rawBytes.length);
        truncated.put("_maxPayloadBytes", properties.getMaxPayloadBytes());
        truncated.put("_sha256", sha256Hex(rawBytes));
        return truncated;
    }

    private JsonNode maskSensitiveFields(JsonNode source) {
        Set<String> normalizedMaskedFields = new HashSet<>();
        for (String field : properties.getMaskedFields()) {
            normalizedMaskedFields.add(normalizeFieldName(field));
        }
        return maskNode(source, normalizedMaskedFields);
    }

    private JsonNode maskNode(JsonNode node, Set<String> normalizedMaskedFields) {
        if (node == null || node.isNull()) {
            return node;
        }

        if (node.isObject()) {
            ObjectNode masked = objectMapper.createObjectNode();
            node.properties().forEach(entry -> {
                String fieldName = entry.getKey();
                JsonNode fieldValue = entry.getValue();
                if (normalizedMaskedFields.contains(normalizeFieldName(fieldName)) && !fieldValue.isNull()) {
                    masked.put(fieldName, MASKED_VALUE);
                } else {
                    masked.set(fieldName, maskNode(fieldValue, normalizedMaskedFields));
                }
            });
            return masked;
        }

        if (node.isArray()) {
            ArrayNode arrayNode = objectMapper.createArrayNode();
            for (JsonNode element : node) {
                arrayNode.add(maskNode(element, normalizedMaskedFields));
            }
            return arrayNode;
        }

        return node.deepCopy();
    }

    private String normalizeFieldName(String fieldName) {
        if (fieldName == null) {
            return "";
        }
        return fieldName.replaceAll("[^A-Za-z0-9]", "").toLowerCase(Locale.ROOT);
    }

    private byte[] writeBytes(JsonNode node) {
        try {
            return objectMapper.writeValueAsBytes(node);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize audit payload", ex);
        }
    }

    private String sha256Hex(byte[] input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input);
            StringBuilder builder = new StringBuilder(hash.length * 2);
            for (byte value : hash) {
                builder.append(String.format("%02x", value));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 algorithm unavailable", ex);
        }
    }
}
