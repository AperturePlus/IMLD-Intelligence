package xenosoft.imldintelligence.module.identity.internal.dto;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

/**
 * AbacPolicy 数据传输对象，封装AbacPolicy相关的对外传输字段。
 */
public record AbacPolicyDto(
        @NotNull(message = "Id must not be null")
        @Positive(message = "Id must be greater than 0")
        Long id,
        @NotNull(message = "TenantId must not be null")
        @Positive(message = "TenantId must be greater than 0")
        Long tenantId,
        @NotBlank(message = "Policy code must not be blank")
        @Size(max = 64, message = "Policy code must be at most 64 characters")
        String policyCode,
        @NotBlank(message = "Policy name must not be blank")
        @Size(max = 100, message = "Policy name must be at most 100 characters")
        String policyName,
        @NotNull(message = "Subject expression must not be null")
        JsonNode subjectExpr,
        @NotNull(message = "Resource expression must not be null")
        JsonNode resourceExpr,
        @NotNull(message = "Action expression must not be null")
        JsonNode actionExpr,
        @NotBlank(message = "Effect must not be blank")
        @Size(max = 16, message = "Effect must be at most 16 characters")
        String effect,
        @NotNull(message = "Priority must not be null")
        @Positive(message = "Priority must be greater than 0")
        Integer priority,
        @NotBlank(message = "Status must not be blank")
        @Size(max = 32, message = "Status must be at most 32 characters")
        String status,
        OffsetDateTime createdAt
) {
}
