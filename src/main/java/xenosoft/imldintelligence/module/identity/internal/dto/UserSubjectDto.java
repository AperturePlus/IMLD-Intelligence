package xenosoft.imldintelligence.module.identity.internal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.Set;

/**
 * UserSubject 数据传输对象，封装UserSubject相关的对外传输字段。
 */
public record UserSubjectDto(
        @NotNull(message = "UserId must not be null")
        @Positive(message = "UserId must be greater than 0")
        Long userId,
        @NotNull(message = "TenantId must not be null")
        @Positive(message = "TenantId must be greater than 0")
        Long tenantId,
        @NotBlank(message = "User type must not be blank")
        @Size(max = 32, message = "User type must be at most 32 characters")
        String userType,
        @Size(max = 100, message = "Department name must be at most 100 characters")
        String deptName,
        @NotNull(message = "Role codes must not be null")
        Set<@NotBlank(message = "Role code must not be blank") @Size(max = 64, message = "Role code must be at most 64 characters") String> roleCodes
) {
}
