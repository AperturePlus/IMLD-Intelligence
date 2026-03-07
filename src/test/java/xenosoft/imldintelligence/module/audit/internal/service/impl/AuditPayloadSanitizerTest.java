package xenosoft.imldintelligence.module.audit.internal.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import xenosoft.imldintelligence.module.audit.internal.config.AuditProperties;

import static org.assertj.core.api.Assertions.assertThat;

class AuditPayloadSanitizerTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldMaskSensitiveFieldsRecursively() {
        AuditProperties properties = new AuditProperties();
        AuditPayloadSanitizer sanitizer = new AuditPayloadSanitizer(properties, objectMapper);

        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("patientName", "Alice");
        payload.put("safeField", "safe");
        payload.set("nested", objectMapper.createObjectNode()
                .put("id_card", "330000000000000000")
                .put("phone", "13800000000"));

        JsonNode sanitized = sanitizer.sanitize(payload);

        assertThat(sanitized.get("patientName").asText()).isEqualTo("******");
        assertThat(sanitized.get("safeField").asText()).isEqualTo("safe");
        assertThat(sanitized.get("nested").get("id_card").asText()).isEqualTo("******");
        assertThat(sanitized.get("nested").get("phone").asText()).isEqualTo("******");
    }

    @Test
    void shouldReplaceOversizedPayloadWithDigestObject() {
        AuditProperties properties = new AuditProperties();
        properties.setMaxPayloadBytes(40);
        AuditPayloadSanitizer sanitizer = new AuditPayloadSanitizer(properties, objectMapper);

        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("content", "abcdefghijklmnopqrstuvwxyz0123456789abcdefghijklmnopqrstuvwxyz0123456789");

        JsonNode sanitized = sanitizer.sanitize(payload);

        assertThat(sanitized.get("_truncated").asBoolean()).isTrue();
        assertThat(sanitized.get("_originalSizeBytes").asInt()).isGreaterThan(40);
        assertThat(sanitized.get("_maxPayloadBytes").asInt()).isEqualTo(40);
        assertThat(sanitized.get("_sha256").asText()).hasSize(64);
    }

    @Test
    void shouldReturnNullWhenPayloadIsNull() {
        AuditProperties properties = new AuditProperties();
        AuditPayloadSanitizer sanitizer = new AuditPayloadSanitizer(properties, objectMapper);

        JsonNode sanitized = sanitizer.sanitize(null);

        assertThat(sanitized).isNull();
    }
}
