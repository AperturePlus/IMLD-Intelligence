package xenosoft.imldintelligence.module.identity.internal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

/**
 * PatientExternalId 数据传输对象，封装PatientExternalId相关的对外传输字段。
 */
public record PatientExternalIdDto(
        @NotNull(message = "Id must not be null")
        @Positive(message = "Id must be greater than 0")
        Long id,
        @NotNull(message = "TenantId must not be null")
        @Positive(message = "TenantId must be greater than 0")
        Long tenantId,
        @NotNull(message = "PatientId must not be null")
        @Positive(message = "PatientId must be greater than 0")
        Long patientId,
        @NotBlank(message = "Id type must not be blank")
        @Size(max = 32, message = "Id type must be at most 32 characters")
        String idType,
        @NotBlank(message = "Masked id value must not be blank")
        @Size(max = 128, message = "Masked id value must be at most 128 characters")
        String idValueMasked,
        @Size(max = 200, message = "Source org must be at most 200 characters")
        String sourceOrg,
        @NotNull(message = "Primary flag must not be null")
        Boolean isPrimary,
        OffsetDateTime createdAt
) {
}
