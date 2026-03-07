package xenosoft.imldintelligence.module.identity.internal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

/**
 * TocUser 数据传输对象，封装TocUser相关的对外传输字段。
 */
public record TocUserDto(
        @NotNull(message = "Id must not be null")
        @Positive(message = "Id must be greater than 0")
        Long id,
        @NotNull(message = "TenantId must not be null")
        @Positive(message = "TenantId must be greater than 0")
        Long tenantId,
        @NotBlank(message = "TocUid must not be blank")
        @Size(max = 64, message = "TocUid must be at most 64 characters")
        String tocUid,
        @Size(max = 100, message = "Nickname must be at most 100 characters")
        String nickname,
        @NotBlank(message = "Vip status must not be blank")
        @Size(max = 32, message = "Vip status must be at most 32 characters")
        String vipStatus,
        @NotBlank(message = "Status must not be blank")
        @Size(max = 32, message = "Status must be at most 32 characters")
        String status,
        boolean mobileBound,
        boolean openIdBound,
        boolean unionIdBound,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
