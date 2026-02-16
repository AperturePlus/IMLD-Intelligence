package xenosoft.imldintelligence.module.report.internal.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.transaction.annotation.Transactional;
import xenosoft.imldintelligence.AbstractPostgresIntegrationTest;
import xenosoft.imldintelligence.module.diagnoses.internal.repository.DiagnosisSessionRepository;
import xenosoft.imldintelligence.module.diagnoses.internal.repository.ModelRegistryRepository;
import xenosoft.imldintelligence.module.diagnoses.model.DiagnosisSession;
import xenosoft.imldintelligence.module.diagnoses.model.ModelRegistry;
import xenosoft.imldintelligence.module.identity.internal.model.Encounter;
import xenosoft.imldintelligence.module.identity.internal.model.Patient;
import xenosoft.imldintelligence.module.identity.internal.model.Tenant;
import xenosoft.imldintelligence.module.identity.internal.model.UserAccount;
import xenosoft.imldintelligence.module.identity.internal.repository.EncounterRepository;
import xenosoft.imldintelligence.module.identity.internal.repository.PatientRepository;
import xenosoft.imldintelligence.module.identity.internal.repository.TenantRepository;
import xenosoft.imldintelligence.module.identity.internal.repository.UserAccountRepository;
import xenosoft.imldintelligence.module.report.model.Report;
import xenosoft.imldintelligence.module.report.model.ReportTemplate;
import xenosoft.imldintelligence.module.report.model.ReportVersion;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisabledInAotMode
class ReportRepositoryIntegrationTest extends AbstractPostgresIntegrationTest {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private EncounterRepository encounterRepository;

    @Autowired
    private ModelRegistryRepository modelRegistryRepository;

    @Autowired
    private DiagnosisSessionRepository diagnosisSessionRepository;

    @Autowired
    private ReportTemplateRepository reportTemplateRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private ReportVersionRepository reportVersionRepository;

    @Test
    void reportTemplateCrudAndTenantIsolation() {
        Tenant tenantA = createTenant();
        Tenant tenantB = createTenant();

        ReportTemplate reportTemplate = new ReportTemplate();
        reportTemplate.setTenantId(tenantA.getId());
        reportTemplate.setTemplateCode("TPL_" + unique("code"));
        reportTemplate.setTemplateName("Template");
        reportTemplate.setDiseaseCode("D001");
        reportTemplate.setDepartment("GI");
        reportTemplate.setTemplateSchema(OBJECT_MAPPER.createObjectNode().put("k", "v"));
        reportTemplate.setStatus("ACTIVE");
        reportTemplate.setVersionNo(1);
        reportTemplateRepository.save(reportTemplate);

        assertThat(reportTemplate.getId()).isNotNull();
        assertThat(reportTemplateRepository.findById(tenantA.getId(), reportTemplate.getId())).isPresent();
        assertThat(reportTemplateRepository.findById(tenantB.getId(), reportTemplate.getId())).isEmpty();
        assertThat(reportTemplateRepository.findByTemplateCodeAndVersionNo(tenantA.getId(), reportTemplate.getTemplateCode(), 1)).isPresent();
        assertThat(reportTemplateRepository.listByTenantId(tenantA.getId())).extracting(ReportTemplate::getId).contains(reportTemplate.getId());

        reportTemplate.setTemplateSchema(OBJECT_MAPPER.createObjectNode().put("k", "updated"));
        reportTemplateRepository.update(reportTemplate);
        assertThat(reportTemplateRepository.findById(tenantA.getId(), reportTemplate.getId())).get()
                .satisfies(value -> assertThat(value.getTemplateSchema().get("k").asText()).isEqualTo("updated"));

        assertThat(reportTemplateRepository.deleteById(tenantA.getId(), reportTemplate.getId())).isTrue();
        assertThat(reportTemplateRepository.findById(tenantA.getId(), reportTemplate.getId())).get().extracting(ReportTemplate::getStatus).isEqualTo("INACTIVE");
    }

    @Test
    void reportCrudWithJsonb() {
        Tenant tenant = createTenant();
        UserAccount doctor = createUserAccount(tenant.getId());
        Patient patient = createPatient(tenant.getId());
        Encounter encounter = createEncounter(tenant.getId(), patient.getId(), doctor.getId());
        DiagnosisSession session = createDiagnosisSession(tenant.getId(), patient.getId(), encounter.getId(), doctor.getId());
        ReportTemplate template = createReportTemplate(tenant.getId(), doctor.getId());

        Report report = new Report();
        report.setTenantId(tenant.getId());
        report.setPatientId(patient.getId());
        report.setEncounterId(encounter.getId());
        report.setSessionId(session.getId());
        report.setTemplateId(template.getId());
        report.setReportNo("RPT_" + unique("no"));
        report.setStatus("DRAFT");
        report.setCurrentVersion(1);
        report.setCreatedBy(doctor.getId());
        report.setSignatureData(OBJECT_MAPPER.createObjectNode().put("signed", false));
        reportRepository.save(report);

        assertThat(report.getId()).isNotNull();
        assertThat(reportRepository.findById(tenant.getId(), report.getId())).isPresent();
        assertThat(reportRepository.findByReportNo(tenant.getId(), report.getReportNo())).isPresent();
        assertThat(reportRepository.listByTenantId(tenant.getId())).extracting(Report::getId).contains(report.getId());
        assertThat(reportRepository.listByPatientId(tenant.getId(), patient.getId())).extracting(Report::getId).contains(report.getId());
        assertThat(reportRepository.listBySessionId(tenant.getId(), session.getId())).extracting(Report::getId).contains(report.getId());

        report.setStatus("SIGNED");
        report.setSignedBy(doctor.getId());
        report.setSignedAt(OffsetDateTime.now().withNano(0));
        report.setSignatureData(OBJECT_MAPPER.createObjectNode().put("signed", true));
        reportRepository.update(report);
        assertThat(reportRepository.findById(tenant.getId(), report.getId())).get()
                .satisfies(value -> {
                    assertThat(value.getStatus()).isEqualTo("SIGNED");
                    assertThat(value.getSignatureData().get("signed").asBoolean()).isTrue();
                });

        assertThat(reportRepository.deleteById(tenant.getId(), report.getId())).isTrue();
        assertThat(reportRepository.findById(tenant.getId(), report.getId())).isEmpty();
    }

    @Test
    void reportVersionCrudWithJsonb() {
        Tenant tenant = createTenant();
        UserAccount doctor = createUserAccount(tenant.getId());
        Patient patient = createPatient(tenant.getId());
        Encounter encounter = createEncounter(tenant.getId(), patient.getId(), doctor.getId());
        DiagnosisSession session = createDiagnosisSession(tenant.getId(), patient.getId(), encounter.getId(), doctor.getId());
        ReportTemplate template = createReportTemplate(tenant.getId(), doctor.getId());
        Report report = createReport(tenant.getId(), patient.getId(), encounter.getId(), session.getId(), template.getId(), doctor.getId());

        ReportVersion reportVersion = new ReportVersion();
        reportVersion.setTenantId(tenant.getId());
        reportVersion.setReportId(report.getId());
        reportVersion.setVersionNum(1);
        reportVersion.setContentSnapshot(OBJECT_MAPPER.createObjectNode().put("v", 1));
        reportVersion.setChangeSummary("init");
        reportVersion.setChangedBy(doctor.getId());
        reportVersionRepository.save(reportVersion);

        assertThat(reportVersion.getId()).isNotNull();
        assertThat(reportVersionRepository.findById(tenant.getId(), reportVersion.getId())).isPresent();
        assertThat(reportVersionRepository.findByReportIdAndVersionNum(tenant.getId(), report.getId(), 1)).isPresent();
        assertThat(reportVersionRepository.listByReportId(tenant.getId(), report.getId())).extracting(ReportVersion::getId).contains(reportVersion.getId());

        reportVersion.setContentSnapshot(OBJECT_MAPPER.createObjectNode().put("v", 2));
        reportVersion.setChangeSummary("updated");
        reportVersionRepository.update(reportVersion);
        assertThat(reportVersionRepository.findById(tenant.getId(), reportVersion.getId())).get()
                .satisfies(value -> {
                    assertThat(value.getChangeSummary()).isEqualTo("updated");
                    assertThat(value.getContentSnapshot().get("v").asInt()).isEqualTo(2);
                });

        assertThat(reportVersionRepository.deleteById(tenant.getId(), reportVersion.getId())).isTrue();
        assertThat(reportVersionRepository.findById(tenant.getId(), reportVersion.getId())).isEmpty();
    }

    private Report createReport(Long tenantId, Long patientId, Long encounterId, Long sessionId, Long templateId, Long createdBy) {
        Report report = new Report();
        report.setTenantId(tenantId);
        report.setPatientId(patientId);
        report.setEncounterId(encounterId);
        report.setSessionId(sessionId);
        report.setTemplateId(templateId);
        report.setReportNo("RPT_" + unique("no"));
        report.setStatus("DRAFT");
        report.setCurrentVersion(1);
        report.setCreatedBy(createdBy);
        report.setSignatureData(OBJECT_MAPPER.createObjectNode().put("signed", false));
        reportRepository.save(report);
        return report;
    }

    private ReportTemplate createReportTemplate(Long tenantId, Long createdBy) {
        ReportTemplate reportTemplate = new ReportTemplate();
        reportTemplate.setTenantId(tenantId);
        reportTemplate.setTemplateCode("TPL_" + unique("code"));
        reportTemplate.setTemplateName("Template");
        reportTemplate.setTemplateSchema(OBJECT_MAPPER.createObjectNode().put("x", 1));
        reportTemplate.setStatus("ACTIVE");
        reportTemplate.setVersionNo(1);
        reportTemplate.setCreatedBy(createdBy);
        reportTemplateRepository.save(reportTemplate);
        return reportTemplate;
    }

    private DiagnosisSession createDiagnosisSession(Long tenantId, Long patientId, Long encounterId, Long doctorId) {
        ModelRegistry registry = new ModelRegistry();
        registry.setTenantId(tenantId);
        registry.setModelCode("MODEL_" + unique("code"));
        registry.setModelName("Model");
        registry.setModelType("RULE");
        registry.setModelVersion("v1");
        registry.setStatus("ACTIVE");
        modelRegistryRepository.save(registry);

        DiagnosisSession session = new DiagnosisSession();
        session.setTenantId(tenantId);
        session.setPatientId(patientId);
        session.setEncounterId(encounterId);
        session.setDoctorId(doctorId);
        session.setTriggeredBy("MANUAL");
        session.setModelRegistryId(registry.getId());
        session.setInputSnapshot(OBJECT_MAPPER.createObjectNode().put("k", "v"));
        session.setStatus("RUNNING");
        session.setStartedAt(OffsetDateTime.now().withNano(0));
        diagnosisSessionRepository.save(session);
        return session;
    }

    private Tenant createTenant() {
        String suffix = unique("tenant");
        Tenant tenant = new Tenant();
        tenant.setTenantCode("TEN_" + suffix);
        tenant.setTenantName("Tenant " + suffix);
        tenant.setDeployMode("SAAS");
        tenant.setStatus("ACTIVE");
        tenantRepository.save(tenant);
        return tenant;
    }

    private UserAccount createUserAccount(Long tenantId) {
        String suffix = unique("user");
        UserAccount userAccount = new UserAccount();
        userAccount.setTenantId(tenantId);
        userAccount.setUserNo("UNO_" + suffix);
        userAccount.setUsername("username_" + suffix);
        userAccount.setPasswordHash("pwd_hash");
        userAccount.setDisplayName("Display Name");
        userAccount.setUserType("DOCTOR");
        userAccount.setStatus("ACTIVE");
        userAccountRepository.save(userAccount);
        return userAccount;
    }

    private Patient createPatient(Long tenantId) {
        String suffix = unique("patient");
        Patient patient = new Patient();
        patient.setTenantId(tenantId);
        patient.setPatientNo("PAT_" + suffix);
        patient.setPatientName("Patient " + suffix);
        patient.setPatientType("OUTPATIENT");
        patient.setStatus("ACTIVE");
        patient.setSourceChannel("HOSPITAL");
        patientRepository.save(patient);
        return patient;
    }

    private Encounter createEncounter(Long tenantId, Long patientId, Long doctorId) {
        Encounter encounter = new Encounter();
        encounter.setTenantId(tenantId);
        encounter.setPatientId(patientId);
        encounter.setEncounterNo("ENC_" + unique("enc"));
        encounter.setEncounterType("OUTPATIENT");
        encounter.setAttendingDoctorId(doctorId);
        encounter.setStartAt(OffsetDateTime.now().withNano(0));
        encounter.setSourceSystem("HIS");
        encounterRepository.save(encounter);
        return encounter;
    }

    private String unique(String prefix) {
        return prefix + "_" + System.nanoTime();
    }
}

