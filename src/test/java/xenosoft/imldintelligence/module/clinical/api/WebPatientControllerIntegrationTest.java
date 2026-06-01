package xenosoft.imldintelligence.module.clinical.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.web.servlet.MockMvc;
import xenosoft.imldintelligence.AbstractPostgresIntegrationTest;
import xenosoft.imldintelligence.module.diagnoses.internal.model.DiagnosisResult;
import xenosoft.imldintelligence.module.diagnoses.internal.model.DiagnosisSession;
import xenosoft.imldintelligence.module.diagnoses.internal.repository.DiagnosisResultRepository;
import xenosoft.imldintelligence.module.diagnoses.internal.repository.DiagnosisSessionRepository;
import xenosoft.imldintelligence.module.identity.internal.model.Patient;
import xenosoft.imldintelligence.module.identity.internal.model.Tenant;
import xenosoft.imldintelligence.module.identity.internal.repository.PatientRepository;
import xenosoft.imldintelligence.module.identity.internal.repository.TenantRepository;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisabledInAotMode
class WebPatientControllerIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DiagnosisSessionRepository diagnosisSessionRepository;

    @Autowired
    private DiagnosisResultRepository diagnosisResultRepository;

    private Long tenantId;

    @BeforeEach
    void setUp() {
        Tenant tenant = new Tenant();
        tenant.setTenantCode("WEB_PATIENT_" + System.nanoTime());
        tenant.setTenantName("Web Patient Test");
        tenant.setDeployMode("SAAS");
        tenant.setStatus("ACTIVE");
        tenantRepository.save(tenant);
        this.tenantId = tenant.getId();

        savePatient("P900", "林测试", "女", 42, "ACTIVE");
        savePatient("P901", "王无关", "男", 50, "ACTIVE");
        savePatient("P902", "林已停用", "男", 39, "INACTIVE");
    }

    @Test
    void listPatientsReturnsFrontendItemsAndSupportsKeyword() throws Exception {
        mockMvc.perform(get("/api/v1/web/patients")
                        .header("X-Tenant-Id", tenantId)
                        .param("keyword", "林"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.items", hasSize(1)))
                .andExpect(jsonPath("$.data.items[0].id").value("P900"))
                .andExpect(jsonPath("$.data.items[0].name").value("林测试"))
                .andExpect(jsonPath("$.data.items[0].gender").value("女"))
                .andExpect(jsonPath("$.data.items[0].age").value(42))
                .andExpect(jsonPath("$.data.items[0].riskLevel").value(nullValue()))
                .andExpect(jsonPath("$.data.items[0].aiStatus").value("未诊断"))
                .andExpect(jsonPath("$.data.items[0].avatar").value(""));
    }

    @Test
    void listPatientsUsesLatestCompletedDiagnosisResultRiskLevel() throws Exception {
        Patient patient = savePatient("P910", "风险覆盖患者", "男", 51, "ACTIVE");
        DiagnosisSession oldSession = saveDiagnosisSession(patient.getId(), "COMPLETED", OffsetDateTime.now(ZoneOffset.UTC).minusDays(2));
        saveDiagnosisResult(oldSession.getId(), "低风险", 0.12D);
        DiagnosisSession newestRunningSession = saveDiagnosisSession(patient.getId(), "RUNNING", OffsetDateTime.now(ZoneOffset.UTC).plusMinutes(5));
        saveDiagnosisResult(newestRunningSession.getId(), "LOW", 0.1D);
        DiagnosisSession latestCompletedSession = saveDiagnosisSession(patient.getId(), "REVIEWED", OffsetDateTime.now(ZoneOffset.UTC).minusHours(1));
        saveDiagnosisResult(latestCompletedSession.getId(), "HIGH", 0.91D);

        mockMvc.perform(get("/api/v1/web/patients")
                        .header("X-Tenant-Id", tenantId)
                        .param("keyword", "风险覆盖"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.items", hasSize(1)))
                .andExpect(jsonPath("$.data.items[0].id").value("P910"))
                .andExpect(jsonPath("$.data.items[0].riskLevel").value("高"))
                .andExpect(jsonPath("$.data.items[0].aiStatus").value("已诊断"));
    }

    private Patient savePatient(String patientNo, String name, String gender, int age, String status) {
        Patient patient = new Patient();
        patient.setTenantId(tenantId);
        patient.setPatientNo(patientNo);
        patient.setPatientName(name);
        patient.setGender(gender);
        patient.setBirthDate(LocalDate.now(ZoneOffset.UTC).minusYears(age));
        patient.setPatientType("OUTPATIENT");
        patient.setStatus(status);
        patient.setSourceChannel("HOSPITAL");
        patient.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC).withNano(0));
        patient.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC).withNano(0));
        patientRepository.save(patient);
        return patient;
    }

    private DiagnosisSession saveDiagnosisSession(Long patientId, String status, OffsetDateTime completedAt) {
        DiagnosisSession session = new DiagnosisSession();
        session.setTenantId(tenantId);
        session.setPatientId(patientId);
        session.setTriggeredBy("MANUAL");
        session.setInputSnapshot(JsonNodeFactory.instance.objectNode());
        session.setStatus(status);
        session.setStartedAt(completedAt.minusMinutes(5).withNano(0));
        session.setCompletedAt(completedAt.withNano(0));
        diagnosisSessionRepository.save(session);
        return session;
    }

    private DiagnosisResult saveDiagnosisResult(Long sessionId, String riskLevel, Double confidence) {
        DiagnosisResult result = new DiagnosisResult();
        result.setTenantId(tenantId);
        result.setSessionId(sessionId);
        result.setDiseaseCode("IMLD_RISK");
        result.setDiseaseName("遗传代谢性肝病风险提示");
        result.setConfidence(confidence);
        result.setRankNo(1);
        result.setRiskLevel(riskLevel);
        result.setEvidenceJson(JsonNodeFactory.instance.objectNode());
        result.setIsDisplayToPatient(Boolean.FALSE);
        diagnosisResultRepository.save(result);
        return result;
    }
}
