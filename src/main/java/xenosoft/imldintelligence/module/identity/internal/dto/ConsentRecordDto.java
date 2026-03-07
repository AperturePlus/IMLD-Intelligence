package xenosoft.imldintelligence.module.identity.internal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

/**
 * ConsentRecord 数据传输对象，封装ConsentRecord相关的对外传输字段。
 */
public record ConsentRecordDto(
        @NotNull(message = "Id must not be null")
        @Positive(message = "Id must be greater than 0")
        Long id,
        @NotNull(message = "TenantId must not be null")
        @Positive(message = "TenantId must be greater than 0")
        Long tenantId,
        @Positive(message = "PatientId must be greater than 0")
        Long patientId,
        @Positive(message = "TocUserId must be greater than 0")
        Long tocUserId,
        @NotBlank(message = "Consent type must not be blank")
        @Size(max = 32, message = "Consent type must be at most 32 characters")
        String consentType,
        @NotBlank(message = "Consent version must not be blank")
        @Size(max = 32, message = "Consent version must be at most 32 characters")
        String consentVersion,
        @NotNull(message = "SignedOffline must not be null")
        Boolean signedOffline,
        OffsetDateTime signedAt,
        @Positive(message = "FileId must be greater than 0")
        Long fileId,
        @NotBlank(message = "Status must not be blank")
        @Size(max = 32, message = "Status must be at most 32 characters")
        String status,
        String remark,
        OffsetDateTime createdAt
) {
}
