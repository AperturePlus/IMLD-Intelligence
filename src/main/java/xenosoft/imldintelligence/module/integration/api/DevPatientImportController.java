package xenosoft.imldintelligence.module.integration.api;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import xenosoft.imldintelligence.common.dto.ApiResponse;
import xenosoft.imldintelligence.module.identity.internal.model.Patient;
import xenosoft.imldintelligence.module.identity.internal.repository.PatientRepository;
import xenosoft.imldintelligence.module.integration.api.dto.DevPatientImportApiDtos;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Local dev import preview adapter. It never calls external HIS/LIS/OCR systems.
 */
@RestController
@Profile("dev")
@RequiredArgsConstructor
public class DevPatientImportController implements DevPatientImportApi {
    private static final String PHONE = "13800138000";
    private static final String ID_CARD = "510101198601012233";
    private static final String OCCUPATION = "教师";
    private static final String CURRENT_ADDRESS = "四川省成都市武侯区人民南路三段";
    private static final String NATIVE_PLACE = "四川成都";
    private static final String DEPARTMENT = "肝病医学科";
    private static final String ENCOUNTER_TYPE = "OUTPATIENT";

    private final PatientRepository patientRepository;

    @Override
    public ApiResponse<DevPatientImportApiDtos.Response.PatientImportPreview> previewFromHisLis(
            Long tenantId,
            DevPatientImportApiDtos.Request.HisLisPatientImportRequest request
    ) {
        if (request == null || (isBlank(request.patientNo()) && isBlank(request.visitNo()))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "patientNo 或 visitNo 至少提供一个");
        }
        Patient patient = pickPatient(tenantId, request.patientNo(), request.visitNo());
        String patientNo = firstNonBlank(request.patientNo(), patient == null ? null : patient.getPatientNo(), generatedId("P"));
        return ApiResponse.success(preview(
                "HIS_LIS",
                0.96D,
                patientNo,
                patient == null ? "未知患者" : patient.getPatientName(),
                patient == null ? "男" : normalizeGender(patient.getGender()),
                patient == null ? 35 : ageOf(patient)
        ));
    }

    @Override
    public ApiResponse<DevPatientImportApiDtos.Response.PatientImportPreview> previewFromImage(
            Long tenantId,
            DevPatientImportApiDtos.Request.OcrPatientImportRequest request
    ) {
        validateFilePayload(request, "图片文件内容不能为空");
        return ApiResponse.success(preview("IMAGE_OCR", 0.83D, generatedId("IMG"), "图片识别患者", "女", 29));
    }

    @Override
    public ApiResponse<DevPatientImportApiDtos.Response.PatientImportPreview> previewFromPdf(
            Long tenantId,
            DevPatientImportApiDtos.Request.OcrPatientImportRequest request
    ) {
        validateFilePayload(request, "PDF 文件内容不能为空");
        return ApiResponse.success(preview("PDF_OCR", 0.88D, generatedId("PDF"), "PDF识别患者", "男", 47));
    }

    private Patient pickPatient(Long tenantId, String patientNo, String visitNo) {
        if (!isBlank(patientNo)) {
            return patientRepository.findByPatientNo(tenantId, patientNo.trim()).orElse(null);
        }
        List<Patient> candidates = patientRepository.listByTenantId(tenantId).stream()
                .filter(patient -> "ACTIVE".equalsIgnoreCase(patient.getStatus()))
                .sorted(Comparator.comparing(Patient::getPatientNo, Comparator.nullsLast(String::compareTo)))
                .toList();
        if (candidates.isEmpty()) {
            return null;
        }
        int bucket = Math.floorMod(visitNo.trim().chars().sum(), candidates.size());
        return candidates.get(bucket);
    }

    private void validateFilePayload(DevPatientImportApiDtos.Request.OcrPatientImportRequest request, String message) {
        if (request == null || isBlank(request.fileName()) || isBlank(request.fileContentBase64())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
    }

    private DevPatientImportApiDtos.Response.PatientImportPreview preview(
            String sourceType,
            double confidence,
            String patientNo,
            String name,
            String gender,
            Integer age
    ) {
        return new DevPatientImportApiDtos.Response.PatientImportPreview(
                sourceType,
                "IMP-" + System.currentTimeMillis(),
                confidence,
                patientNo,
                name,
                gender,
                age,
                LocalDate.now(ZoneOffset.UTC).toString(),
                PHONE,
                ID_CARD,
                OCCUPATION,
                CURRENT_ADDRESS,
                NATIVE_PLACE,
                DEPARTMENT,
                ENCOUNTER_TYPE
        );
    }

    private int ageOf(Patient patient) {
        if (patient.getBirthDate() == null) {
            return 0;
        }
        return Math.max(0, (int) ChronoUnit.YEARS.between(patient.getBirthDate(), LocalDate.now(ZoneOffset.UTC)));
    }

    private String normalizeGender(String gender) {
        if (isBlank(gender)) {
            return "男";
        }
        String normalized = gender.trim().toLowerCase(Locale.ROOT);
        if (normalized.equals("male") || normalized.equals("m")) {
            return "男";
        }
        if (normalized.equals("female") || normalized.equals("f")) {
            return "女";
        }
        return gender.trim();
    }

    private String generatedId(String prefix) {
        return prefix + "-" + (System.currentTimeMillis() % 1_000_000L);
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (!isBlank(value)) {
                return value.trim();
            }
        }
        return "";
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
