package xenosoft.imldintelligence.module.identity.internal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

/**
 * Encounter 数据传输对象，封装Encounter相关的对外传输字段。
 */
public record EncounterDto(
        @NotNull(message = "Id must not be null")
        @Positive(message = "Id must be greater than 0")
        Long id,
        @NotNull(message = "TenantId must not be null")
        @Positive(message = "TenantId must be greater than 0")
        Long tenantId,
        @NotNull(message = "PatientId must not be null")
        @Positive(message = "PatientId must be greater than 0")
        Long patientId,
        @NotBlank(message = "EncounterNo must not be blank")
        @Size(max = 64, message = "EncounterNo must be at most 64 characters")
        String encounterNo,
        @NotBlank(message = "Encounter type must not be blank")
        @Size(max = 32, message = "Encounter type must be at most 32 characters")
        String encounterType,
        @Size(max = 100, message = "Department name must be at most 100 characters")
        String deptName,
        @Positive(message = "AttendingDoctorId must be greater than 0")
        Long attendingDoctorId,
        @NotNull(message = "StartAt must not be null")
        OffsetDateTime startAt,
        OffsetDateTime endAt,
        @Size(max = 64, message = "Source system must be at most 64 characters")
        String sourceSystem,
        OffsetDateTime createdAt
) {
}
