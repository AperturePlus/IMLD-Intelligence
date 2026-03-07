package xenosoft.imldintelligence.module.identity.internal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * Patient 数据传输对象，封装Patient相关的对外传输字段。
 */
public record PatientDto(
        @NotNull(message = "Id must not be null")
        @Positive(message = "Id must be greater than 0")
        Long id,
        @NotNull(message = "TenantId must not be null")
        @Positive(message = "TenantId must be greater than 0")
        Long tenantId,
        @NotBlank(message = "PatientNo must not be blank")
        @Size(max = 64, message = "PatientNo must be at most 64 characters")
        String patientNo,
        @NotBlank(message = "Patient name must not be blank")
        @Size(max = 100, message = "Patient name must be at most 100 characters")
        String patientName,
        @Size(max = 16, message = "Gender must be at most 16 characters")
        String gender,
        @PastOrPresent(message = "Birth date must be in the past or present")
        LocalDate birthDate,
        @NotBlank(message = "Patient type must not be blank")
        @Size(max = 32, message = "Patient type must be at most 32 characters")
        String patientType,
        @NotBlank(message = "Status must not be blank")
        @Size(max = 32, message = "Status must be at most 32 characters")
        String status,
        @NotBlank(message = "Source channel must not be blank")
        @Size(max = 32, message = "Source channel must be at most 32 characters")
        String sourceChannel,
        boolean idNoBound,
        boolean mobileBound,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
