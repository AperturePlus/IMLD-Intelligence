package xenosoft.imldintelligence.module.careplan.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.web.servlet.MockMvc;
import xenosoft.imldintelligence.AbstractPostgresIntegrationTest;
import xenosoft.imldintelligence.module.clinical.internal.model.ClinicalHistoryEntry;
import xenosoft.imldintelligence.module.clinical.internal.repository.ClinicalHistoryEntryRepository;
import xenosoft.imldintelligence.module.diagnoses.internal.model.DiagnosisResult;
import xenosoft.imldintelligence.module.diagnoses.internal.model.DiagnosisSession;
import xenosoft.imldintelligence.module.diagnoses.internal.repository.DiagnosisResultRepository;
import xenosoft.imldintelligence.module.diagnoses.internal.repository.DiagnosisSessionRepository;
import xenosoft.imldintelligence.module.identity.internal.model.Patient;
import xenosoft.imldintelligence.module.identity.internal.model.Tenant;
import xenosoft.imldintelligence.module.identity.internal.repository.PatientRepository;
import xenosoft.imldintelligence.module.identity.internal.repository.TenantRepository;
import xenosoft.imldintelligence.module.notify.internal.model.NotificationMessage;
import xenosoft.imldintelligence.module.notify.internal.repository.NotificationMessageRepository;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.empty;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisabledInAotMode
class WebDietControllerIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private ClinicalHistoryEntryRepository clinicalHistoryEntryRepository;

    @Autowired
    private DiagnosisSessionRepository diagnosisSessionRepository;

    @Autowired
    private DiagnosisResultRepository diagnosisResultRepository;

    @Autowired
    private NotificationMessageRepository notificationMessageRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Long tenantId;
    private Patient wilsonPatient;
    private Patient diagnosisFallbackPatient;

    @BeforeEach
    void setUp() {
        Tenant tenant = new Tenant();
        tenant.setTenantCode("WEB_DIET_" + System.nanoTime());
        tenant.setTenantName("Web Diet Test");
        tenant.setDeployMode("SAAS");
        tenant.setStatus("ACTIVE");
        tenantRepository.save(tenant);
        this.tenantId = tenant.getId();

        wilsonPatient = savePatient("P910", "林膳食", "女", 42, "ACTIVE");
        diagnosisFallbackPatient = savePatient("P911", "王血色", "男", 50, "ACTIVE");
        savePatient("P912", "林停用", "男", 39, "INACTIVE");

        saveClinicalDecision(wilsonPatient, "肝豆状核变性 (Wilson病)", "极佳");
        saveDiagnosisResult(diagnosisFallbackPatient, "遗传性血色病");
    }

    @Test
    void listDietPatientsReturnsActivePatientsAndTreatsBlankKeywordAsEmpty() throws Exception {
        mockMvc.perform(get("/api/v1/web/diet/patients/")
                        .header("X-Tenant-Id", tenantId)
                        .param("keyword", " "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.items", hasSize(2)))
                .andExpect(jsonPath("$.data.items[0].id").value("P910"))
                .andExpect(jsonPath("$.data.items[0].name").value("林膳食"))
                .andExpect(jsonPath("$.data.items[0].avatar").value(""))
                .andExpect(jsonPath("$.data.items[0].disease").value("肝豆状核变性 (Wilson病)"))
                .andExpect(jsonPath("$.data.items[0].compliance").value("极佳"))
                .andExpect(jsonPath("$.data.items[1].id").value("P911"))
                .andExpect(jsonPath("$.data.items[1].disease").value("遗传性血色病"));
    }

    @Test
    void listDietPatientsSupportsKeywordByNameOrPatientNo() throws Exception {
        mockMvc.perform(get("/api/v1/web/diet/patients")
                        .header("X-Tenant-Id", tenantId)
                        .param("keyword", "P911"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items", hasSize(1)))
                .andExpect(jsonPath("$.data.items[0].name").value("王血色"));

        mockMvc.perform(get("/api/v1/web/diet/patients")
                        .header("X-Tenant-Id", tenantId)
                        .param("keyword", "林"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items", hasSize(1)))
                .andExpect(jsonPath("$.data.items[0].id").value("P910"));
    }

    @Test
    void getDietPlanReturnsLocalRuleAndUnknownPatientReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/web/diet/patients/P910/plan/")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.targets", hasSize(3)))
                .andExpect(jsonPath("$.data.foods.red", not(empty())))
                .andExpect(jsonPath("$.data.mealPlan", hasSize(3)))
                .andExpect(jsonPath("$.data.targets[0].label").value("每日铜摄入量"));

        mockMvc.perform(get("/api/v1/web/diet/patients/UNKNOWN/plan/")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    void regenerateDietPlanReturnsDeterministicVariant() throws Exception {
        mockMvc.perform(post("/api/v1/web/diet/patients/P910/regenerate/")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.mealPlan", hasSize(3)))
                .andExpect(jsonPath("$.data.mealPlan[0].nutrition").value(org.hamcrest.Matchers.containsString("方案版本 1")))
                .andExpect(jsonPath("$.data.regeneratedAt").isString());
    }

    @Test
    void pushDietPlanWritesLocalNotificationWithoutSensitiveDetails() throws Exception {
        mockMvc.perform(post("/api/v1/web/diet/patients/P910/push/")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.delivered").value(true))
                .andExpect(jsonPath("$.data.patientId").value("P910"))
                .andExpect(jsonPath("$.data.deliveredAt").isString());

        List<NotificationMessage> messages = notificationMessageRepository.listByReceiver(tenantId, "PATIENT", wilsonPatient.getId());
        assertThat(messages).hasSize(1);
        NotificationMessage message = messages.get(0);
        assertThat(message.getBizType()).isEqualTo("DIET_PLAN");
        assertThat(message.getBizId()).isEqualTo("P910");
        assertThat(message.getChannel()).isEqualTo("APP");
        assertThat(message.getStatus()).isEqualTo("SENT");
        assertThat(message.getContent()).doesNotContain(wilsonPatient.getPatientName());
        assertThat(message.getContent()).doesNotContain("清蒸", "巧克力", "猪肝");
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
        patient.setCreatedAt(now());
        patient.setUpdatedAt(now());
        return patientRepository.save(patient);
    }

    private void saveClinicalDecision(Patient patient, String diagnosis, String compliance) {
        ObjectNode content = objectMapper.createObjectNode();
        content.put("diagnosis", diagnosis);
        content.put("compliance", compliance);

        ClinicalHistoryEntry entry = new ClinicalHistoryEntry();
        entry.setTenantId(tenantId);
        entry.setPatientId(patient.getId());
        entry.setHistoryType("CLINICAL_DECISION");
        entry.setContentJson(content);
        entry.setSourceType("TEST");
        entry.setRecordedAt(now());
        entry.setCreatedAt(now());
        clinicalHistoryEntryRepository.save(entry);
    }

    private void saveDiagnosisResult(Patient patient, String diseaseName) {
        DiagnosisSession session = new DiagnosisSession();
        session.setTenantId(tenantId);
        session.setPatientId(patient.getId());
        session.setTriggeredBy("MANUAL");
        session.setInputSnapshot(objectMapper.createObjectNode().put("test", true));
        session.setStatus("COMPLETED");
        session.setStartedAt(now().minusHours(1));
        session.setCompletedAt(now());
        session.setCreatedAt(now());
        diagnosisSessionRepository.save(session);

        DiagnosisResult result = new DiagnosisResult();
        result.setTenantId(tenantId);
        result.setSessionId(session.getId());
        result.setDiseaseCode("HEMOCHROMATOSIS");
        result.setDiseaseName(diseaseName);
        result.setConfidence(0.91D);
        result.setRankNo(1);
        result.setRiskLevel("HIGH");
        result.setEvidenceJson(objectMapper.createObjectNode().put("test", true));
        result.setIsDisplayToPatient(Boolean.FALSE);
        result.setCreatedAt(now());
        diagnosisResultRepository.save(result);
    }

    private OffsetDateTime now() {
        return OffsetDateTime.now(ZoneOffset.UTC).withNano(0);
    }
}
