package xenosoft.imldintelligence.module.identity.internal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

public record TenantDto(
        @NotNull(message = "Id must not be null")
        @Positive(message = "Id must be greater than 0")
        Long id,
        @NotBlank(message = "Tenant code must not be blank")
        @Size(max = 64, message = "Tenant code must be at most 64 characters")
        String tenantCode,
        @NotBlank(message = "Tenant name must not be blank")
        @Size(max = 200, message = "Tenant name must be at most 200 characters")
        String tenantName,
        @NotBlank(message = "Deploy mode must not be blank")
        @Size(max = 32, message = "Deploy mode must be at most 32 characters")
        String deployMode,
        @NotBlank(message = "Status must not be blank")
        @Size(max = 32, message = "Status must be at most 32 characters")
        String status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
