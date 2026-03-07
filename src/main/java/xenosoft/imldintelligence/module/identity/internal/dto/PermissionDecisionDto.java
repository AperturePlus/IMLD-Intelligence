package xenosoft.imldintelligence.module.identity.internal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record PermissionDecisionDto(
        @NotNull(message = "TenantId must not be null")
        @Positive(message = "TenantId must be greater than 0")
        Long tenantId,
        @NotNull(message = "UserId must not be null")
        @Positive(message = "UserId must be greater than 0")
        Long userId,
        @NotBlank(message = "Resource type must not be blank")
        @Size(max = 64, message = "Resource type must be at most 64 characters")
        String resourceType,
        @NotBlank(message = "Action must not be blank")
        @Size(max = 32, message = "Action must be at most 32 characters")
        String action,
        boolean allowed,
        @NotNull(message = "Role codes must not be null")
        Set<@NotBlank(message = "Role code must not be blank") @Size(max = 64, message = "Role code must be at most 64 characters") String> roleCodes,
        @NotBlank(message = "Evaluation mode must not be blank")
        @Size(max = 64, message = "Evaluation mode must be at most 64 characters")
        String evaluationMode
) {
}
