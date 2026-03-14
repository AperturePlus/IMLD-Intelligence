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

/**
 * Sanitizes audit payloads before persistence.
 *
 * <p>The sanitizer applies two protection steps in order:</p>
 * <p>1. Mask configured sensitive fields recursively in JSON objects and arrays.</p>
 * <p>2. Enforce payload size limits by replacing oversized content with truncation metadata.</p>
 */
@Component
public class AuditPayloadSanitizer {
    private static final String MASKED_VALUE = "******";

    private final AuditProperties properties;
    private final ObjectMapper objectMapper;

    public AuditPayloadSanitizer(AuditProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    /**
     * Returns a sanitized payload that is safe for audit storage.
     */
    public JsonNode sanitize(JsonNode payload) {
        if (payload == null || payload.isNull()) {
            return payload;
        }

        JsonNode sanitized = properties.isMaskingEnabled() ? maskSensitiveFields(payload) : payload.deepCopy();
        byte[] rawBytes = writeBytes(sanitized);
        if (rawBytes.length <= properties.getMaxPayloadBytes()) {
            return sanitized;
        }

        // Store bounded metadata when payload content exceeds the configured audit size budget.
        ObjectNode truncated = objectMapper.createObjectNode();
        truncated.put("_truncated", true);
        truncated.put("_originalSizeBytes", rawBytes.length);
        truncated.put("_maxPayloadBytes", properties.getMaxPayloadBytes());
        truncated.put("_sha256", sha256Hex(rawBytes));
        return truncated;
    }

    /**
     * Builds a normalized lookup set and applies field-level masking recursively.
     */
    private JsonNode maskSensitiveFields(JsonNode source) {
        Set<String> normalizedMaskedFields = new HashSet<>();
        for (String field : properties.getMaskedFields()) {
            normalizedMaskedFields.add(normalizeFieldName(field));
        }
        return maskNode(source, normalizedMaskedFields);
    }

    /**
     * Traverses object and array nodes recursively and masks configured field names.
     */
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

    /**
     * Normalizes field names so configuration can match snake_case, kebab-case, and camelCase.
     */
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
