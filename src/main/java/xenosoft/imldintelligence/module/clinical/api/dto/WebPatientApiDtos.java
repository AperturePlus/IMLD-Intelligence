package xenosoft.imldintelligence.module.clinical.api.dto;

import java.util.List;

/**
 * 医生工作站患者门面 DTO。
 */
public final class WebPatientApiDtos {
    private WebPatientApiDtos() {
    }

    public static final class Response {
        private Response() {
        }

        public record PatientListResponse(
                List<PatientSummary> items
        ) {
        }

        public record PatientSummary(
                String id,
                String name,
                String gender,
                int age,
                String riskLevel,
                String aiStatus,
                String avatar
        ) {
        }
    }
}
