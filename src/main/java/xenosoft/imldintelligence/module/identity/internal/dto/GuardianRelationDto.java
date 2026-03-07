package xenosoft.imldintelligence.module.identity.internal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

/**
 * GuardianRelation 数据传输对象，封装GuardianRelation相关的对外传输字段。
 */
public record GuardianRelationDto(
        @NotNull(message = "Id must not be null")
        @Positive(message = "Id must be greater than 0")
        Long id,
        @NotNull(message = "TenantId must not be null")
        @Positive(message = "TenantId must be greater than 0")
        Long tenantId,
        @NotNull(message = "PatientId must not be null")
        @Positive(message = "PatientId must be greater than 0")
        Long patientId,
        @NotBlank(message = "Guardian name must not be blank")
        @Size(max = 100, message = "Guardian name must be at most 100 characters")
        String guardianName,
        @NotBlank(message = "Relation type must not be blank")
        @Size(max = 32, message = "Relation type must be at most 32 characters")
        String relationType,
        @NotNull(message = "Primary flag must not be null")
        Boolean isPrimary,
        @NotBlank(message = "Status must not be blank")
        @Size(max = 32, message = "Status must be at most 32 characters")
        String status,
        boolean mobileBound,
        boolean idNoBound,
        OffsetDateTime createdAt
) {
}
