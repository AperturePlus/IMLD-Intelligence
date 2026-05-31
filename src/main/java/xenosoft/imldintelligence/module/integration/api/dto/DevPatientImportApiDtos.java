package xenosoft.imldintelligence.module.integration.api.dto;

import jakarta.validation.constraints.Size;

/**
 * Dev-only patient import preview DTOs.
 *
 * <p>These DTOs mirror the frontend mock contract and intentionally return
 * preview-only local data instead of triggering real HIS/LIS/OCR integrations.</p>
 */
public final class DevPatientImportApiDtos {
    private DevPatientImportApiDtos() {
    }

    public static final class Request {
        private Request() {
        }

        public record HisLisPatientImportRequest(
                @Size(max = 64, message = "patientNo must be at most 64 characters")
                String patientNo,
                @Size(max = 64, message = "visitNo must be at most 64 characters")
                String visitNo
        ) {
        }

        public record OcrPatientImportRequest(
                @Size(max = 128, message = "fileName must be at most 128 characters")
                String fileName,
                String fileContentBase64
        ) {
        }
    }

    public static final class Response {
        private Response() {
        }

        public record PatientImportPreview(
                String sourceType,
                String traceId,
                double confidence,
                String patientNo,
                String name,
                String gender,
                Integer age,
                String visitDate,
                String phone,
                String idCard,
                String occupation,
                String currentAddress,
                String nativePlace,
                String department,
                String encounterType
        ) {
        }
    }
}
