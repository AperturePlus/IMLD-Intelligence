package xenosoft.imldintelligence.module.identity.internal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;
import java.util.Set;

/**
 * UserAccount 数据传输对象，封装UserAccount相关的对外传输字段。
 */
public record UserAccountDto(
        @NotNull(message = "Id must not be null")
        @Positive(message = "Id must be greater than 0")
        Long id,
        @NotNull(message = "TenantId must not be null")
        @Positive(message = "TenantId must be greater than 0")
        Long tenantId,
        @NotBlank(message = "UserNo must not be blank")
        @Size(max = 64, message = "UserNo must be at most 64 characters")
        String userNo,
        @NotBlank(message = "Username must not be blank")
        @Size(max = 100, message = "Username must be at most 100 characters")
        String username,
        @NotBlank(message = "Display name must not be blank")
        @Size(max = 100, message = "Display name must be at most 100 characters")
        String displayName,
        @NotBlank(message = "User type must not be blank")
        @Size(max = 32, message = "User type must be at most 32 characters")
        String userType,
        @Size(max = 100, message = "Department name must be at most 100 characters")
        String deptName,
        boolean emailBound,
        boolean mobileBound,
        @NotBlank(message = "Status must not be blank")
        @Size(max = 32, message = "Status must be at most 32 characters")
        String status,
        OffsetDateTime lastLoginAt,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        @NotNull(message = "Role codes must not be null")
        Set<@NotBlank(message = "Role code must not be blank") @Size(max = 64, message = "Role code must be at most 64 characters") String> roleCodes
) {
}
