package xenosoft.imldintelligence.module.clinical.api;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import xenosoft.imldintelligence.common.dto.ApiResponse;
import xenosoft.imldintelligence.module.clinical.api.dto.WebPatientApiDtos;
import xenosoft.imldintelligence.module.diagnoses.internal.model.DiagnosisResult;
import xenosoft.imldintelligence.module.diagnoses.internal.model.DiagnosisSession;
import xenosoft.imldintelligence.module.diagnoses.internal.repository.DiagnosisResultRepository;
import xenosoft.imldintelligence.module.diagnoses.internal.repository.DiagnosisSessionRepository;
import xenosoft.imldintelligence.module.identity.internal.model.Patient;
import xenosoft.imldintelligence.module.identity.internal.repository.PatientRepository;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@RestController
@RequiredArgsConstructor
public class WebPatientController implements WebPatientControllerContract {
    private static final String STATUS_DIAGNOSED = "已诊断";
    private static final String STATUS_UNDIAGNOSED = "未诊断";

    private final PatientRepository patientRepository;
    private final DiagnosisSessionRepository diagnosisSessionRepository;
    private final DiagnosisResultRepository diagnosisResultRepository;

    @Override
    public ApiResponse<WebPatientApiDtos.Response.PatientListResponse> listPatients(Long tenantId, String keyword) {
        String normalizedKeyword = trimToNull(keyword);
        List<WebPatientApiDtos.Response.PatientSummary> items = patientRepository.listByTenantId(tenantId).stream()
                .filter(patient -> "ACTIVE".equalsIgnoreCase(patient.getStatus()))
                .filter(patient -> matchesKeyword(patient, normalizedKeyword))
                .sorted(Comparator.comparing(Patient::getPatientNo, Comparator.nullsLast(String::compareTo)))
                .map(patient -> {
                    PatientDiagnosisSummary diagnosis = resolveDiagnosisSummary(tenantId, patient);
                    return new WebPatientApiDtos.Response.PatientSummary(
                            patient.getPatientNo(),
                            patient.getPatientName(),
                            normalizeGender(patient.getGender()),
                            ageOf(patient),
                            diagnosis.riskLevel(),
                            diagnosis.aiStatus(),
                            ""
                    );
                })
                .toList();
        return ApiResponse.success(new WebPatientApiDtos.Response.PatientListResponse(items));
    }

    private boolean matchesKeyword(Patient patient, String keyword) {
        if (keyword == null) {
            return true;
        }
        String normalized = keyword.toLowerCase(Locale.ROOT);
        return contains(patient.getPatientNo(), normalized) || contains(patient.getPatientName(), normalized);
    }

    private boolean contains(String value, String normalizedKeyword) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(normalizedKeyword);
    }

    private PatientDiagnosisSummary resolveDiagnosisSummary(Long tenantId, Patient patient) {
        String riskLevel = diagnosisSessionRepository.listByPatientId(tenantId, patient.getId()).stream()
                .filter(this::isAuthoritativeDiagnosisSession)
                .sorted(Comparator
                        .comparing(this::diagnosisSessionSortTime,
                                Comparator.nullsLast(Comparator.naturalOrder()))
                        .reversed()
                        .thenComparing(DiagnosisSession::getId, Comparator.nullsLast(Comparator.reverseOrder())))
                .flatMap(session -> diagnosisResultRepository.listBySessionId(tenantId, session.getId()).stream())
                .findFirst()
                .map(DiagnosisResult::getRiskLevel)
                .map(this::toFrontendRiskLevel)
                .orElse(null);

        if (riskLevel == null) {
            return new PatientDiagnosisSummary(STATUS_UNDIAGNOSED, null);
        }
        return new PatientDiagnosisSummary(STATUS_DIAGNOSED, riskLevel);
    }

    private OffsetDateTime diagnosisSessionSortTime(DiagnosisSession session) {
        return session.getCompletedAt() != null ? session.getCompletedAt() : session.getStartedAt();
    }

    private boolean isAuthoritativeDiagnosisSession(DiagnosisSession session) {
        return session != null && (
                "COMPLETED".equalsIgnoreCase(session.getStatus()) ||
                        "REVIEWED".equalsIgnoreCase(session.getStatus())
        );
    }

    private String toFrontendRiskLevel(String riskLevel) {
        if (riskLevel == null || riskLevel.isBlank()) {
            return "中";
        }
        String normalized = riskLevel.trim().toUpperCase(Locale.ROOT);
        if (normalized.contains("HIGH") || riskLevel.contains("高")) {
            return "高";
        }
        if (normalized.contains("LOW") || riskLevel.contains("低")) {
            return "低";
        }
        return "中";
    }

    private int ageOf(Patient patient) {
        LocalDate birthDate = patient.getBirthDate();
        if (birthDate == null) {
            return 0;
        }
        return Math.max(0, (int) ChronoUnit.YEARS.between(birthDate, LocalDate.now(ZoneOffset.UTC)));
    }

    private String normalizeGender(String gender) {
        if (gender == null || gender.isBlank()) {
            return "--";
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

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private record PatientDiagnosisSummary(String aiStatus, String riskLevel) {
    }
}
