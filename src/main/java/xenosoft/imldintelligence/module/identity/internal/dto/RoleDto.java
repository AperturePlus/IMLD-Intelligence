package xenosoft.imldintelligence.module.identity.internal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

public record RoleDto(
        @NotNull(message = "Id must not be null")
        @Positive(message = "Id must be greater than 0")
        Long id,
        @NotNull(message = "TenantId must not be null")
        @Positive(message = "TenantId must be greater than 0")
        Long tenantId,
        @NotBlank(message = "Role code must not be blank")
        @Size(max = 64, message = "Role code must be at most 64 characters")
        String roleCode,
        @NotBlank(message = "Role name must not be blank")
        @Size(max = 100, message = "Role name must be at most 100 characters")
        String roleName,
        String description,
        @NotBlank(message = "Status must not be blank")
        @Size(max = 32, message = "Status must be at most 32 characters")
        String status,
        OffsetDateTime createdAt
) {
}
