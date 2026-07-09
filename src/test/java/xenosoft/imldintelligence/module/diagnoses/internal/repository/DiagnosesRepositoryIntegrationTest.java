package xenosoft.imldintelligence.module.diagnoses.internal.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.transaction.annotation.Transactional;
import xenosoft.imldintelligence.AbstractPostgresIntegrationTest;
import xenosoft.imldintelligence.module.diagnoses.internal.model.DiagnosisRecommendation;
import xenosoft.imldintelligence.module.diagnoses.internal.model.DiagnosisResult;
import xenosoft.imldintelligence.module.diagnoses.internal.model.DiagnosisSession;
import xenosoft.imldintelligence.module.diagnoses.internal.model.DoctorFeedback;
import xenosoft.imldintelligence.module.diagnoses.internal.model.ModelRegistry;
import xenosoft.imldintelligence.module.diagnoses.internal.repository.query.DiagnosisSessionQuery;
import xenosoft.imldintelligence.module.diagnoses.internal.repository.query.ModelRegistryQuery;
import xenosoft.imldintelligence.module.identity.internal.model.Encounter;
import xenosoft.imldintelligence.module.identity.internal.model.Patient;
import xenosoft.imldintelligence.module.identity.internal.model.Tenant;
import xenosoft.imldintelligence.module.identity.internal.model.UserAccount;
import xenosoft.imldintelligence.module.identity.internal.repository.EncounterRepository;
import xenosoft.imldintelligence.module.identity.internal.repository.PatientRepository;
import xenosoft.imldintelligence.module.identity.internal.repository.TenantRepository;
import xenosoft.imldintelligence.module.identity.internal.repository.UserAccountRepository;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisabledInAotMode
class DiagnosesRepositoryIntegrationTest extends AbstractPostgresIntegrationTest {
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
    private DiagnosisResultRepository diagnosisResultRepository;

    @Autowired
    private DiagnosisRecommendationRepository diagnosisRecommendationRepository;

    @Autowired
    private DoctorFeedbackRepository doctorFeedbackRepository;

    @Test
    void modelRegistryCrudAndTenantIsolation() {
        Tenant tenantA = createTenant();
        Tenant tenantB = createTenant();

        ModelRegistry registry = new ModelRegistry();
        registry.setTenantId(tenantA.getId());
        registry.setModelCode("MODEL_" + unique("code"));
        registry.setModelName("Model");
        registry.setModelType("RULE");
        registry.setModelVersion("v1");
        registry.setProvider("LOCAL");
        registry.setStatus("ACTIVE");
        registry.setReleasedAt(OffsetDateTime.now().withNano(0));
        modelRegistryRepository.save(registry);

        assertThat(registry.getId()).isNotNull();
        assertThat(modelRegistryRepository.findById(tenantA.getId(), registry.getId())).isPresent();
        assertThat(modelRegistryRepository.findById(tenantB.getId(), registry.getId())).isEmpty();
        assertThat(modelRegistryRepository.findByModelCodeAndModelVersion(tenantA.getId(), registry.getModelCode(), registry.getModelVersion())).isPresent();
        assertThat(modelRegistryRepository.listByTenantId(tenantA.getId())).extracting(ModelRegistry::getId).contains(registry.getId());

        registry.setModelName("Model Updated");
        modelRegistryRepository.update(registry);
        assertThat(modelRegistryRepository.findById(tenantA.getId(), registry.getId())).get().extracting(ModelRegistry::getModelName).isEqualTo("Model Updated");

        assertThat(modelRegistryRepository.deleteById(tenantA.getId(), registry.getId())).isTrue();
        assertThat(modelRegistryRepository.findById(tenantA.getId(), registry.getId())).get().extracting(ModelRegistry::getStatus).isEqualTo("INACTIVE");
    }

    @Test
    void diagnosisSessionCrudWithJsonb() {
        Tenant tenant = createTenant();
        Patient patient = createPatient(tenant.getId());
        UserAccount doctor = createUserAccount(tenant.getId());
        Encounter encounter = createEncounter(tenant.getId(), patient.getId(), doctor.getId());
        ModelRegistry registry = createModelRegistry(tenant.getId());

        DiagnosisSession session = new DiagnosisSession();
        session.setTenantId(tenant.getId());
        session.setPatientId(patient.getId());
        session.setEncounterId(encounter.getId());
        session.setDoctorId(doctor.getId());
        session.setTriggeredBy("MANUAL");
        session.setModelRegistryId(registry.getId());
        session.setInputSnapshot(OBJECT_MAPPER.createObjectNode().put("k", "v"));
        session.setStatus("RUNNING");
        session.setStartedAt(OffsetDateTime.now().withNano(0));
        diagnosisSessionRepository.save(session);

        assertThat(session.getId()).isNotNull();
        assertThat(diagnosisSessionRepository.findById(tenant.getId(), session.getId())).isPresent();
        assertThat(diagnosisSessionRepository.listByTenantId(tenant.getId())).extracting(DiagnosisSession::getId).contains(session.getId());
        assertThat(diagnosisSessionRepository.listByPatientId(tenant.getId(), patient.getId())).extracting(DiagnosisSession::getId).contains(session.getId());
        assertThat(diagnosisSessionRepository.listByEncounterId(tenant.getId(), encounter.getId())).extracting(DiagnosisSession::getId).contains(session.getId());

        session.setStatus("COMPLETED");
        session.setInputSnapshot(OBJECT_MAPPER.createObjectNode().put("k", "updated"));
        diagnosisSessionRepository.update(session);
        assertThat(diagnosisSessionRepository.findById(tenant.getId(), session.getId())).get()
                .satisfies(value -> {
                    assertThat(value.getStatus()).isEqualTo("COMPLETED");
                    assertThat(value.getInputSnapshot().get("k").asText()).isEqualTo("updated");
                });

        assertThat(diagnosisSessionRepository.deleteById(tenant.getId(), session.getId())).isTrue();
        assertThat(diagnosisSessionRepository.findById(tenant.getId(), session.getId())).get()
                .extracting(DiagnosisSession::getStatus)
                .isEqualTo("ARCHIVED");
    }

    @Test
    void diagnosisResultCrudWithJsonb() {
        Tenant tenant = createTenant();
        DiagnosisSession session = createDiagnosisSession(tenant.getId());

        DiagnosisResult result = new DiagnosisResult();
        result.setTenantId(tenant.getId());
        result.setSessionId(session.getId());
        result.setDiseaseCode("DC");
        result.setDiseaseName("Name");
        result.setConfidence(0.9D);
        result.setRankNo(1);
        result.setRiskLevel("HIGH");
        result.setEvidenceJson(OBJECT_MAPPER.createObjectNode().put("from", "model"));
        result.setIsDisplayToPatient(false);
        diagnosisResultRepository.save(result);

        assertThat(result.getId()).isNotNull();
        assertThat(diagnosisResultRepository.findById(tenant.getId(), result.getId())).isPresent();
        assertThat(diagnosisResultRepository.listByTenantId(tenant.getId())).extracting(DiagnosisResult::getId).contains(result.getId());
        assertThat(diagnosisResultRepository.listBySessionId(tenant.getId(), session.getId())).extracting(DiagnosisResult::getId).contains(result.getId());

        result.setIsDisplayToPatient(true);
        result.setEvidenceJson(OBJECT_MAPPER.createObjectNode().put("from", "doctor"));
        diagnosisResultRepository.update(result);
        assertThat(diagnosisResultRepository.findById(tenant.getId(), result.getId())).get()
                .satisfies(value -> {
                    assertThat(value.getIsDisplayToPatient()).isTrue();
                    assertThat(value.getEvidenceJson().get("from").asText()).isEqualTo("doctor");
                });

        assertThat(diagnosisResultRepository.deleteById(tenant.getId(), result.getId())).isTrue();
        assertThat(diagnosisResultRepository.findById(tenant.getId(), result.getId())).isEmpty();
    }

    @Test
    void diagnosisRecommendationCrud() {
        Tenant tenant = createTenant();
        DiagnosisSession session = createDiagnosisSession(tenant.getId());

        DiagnosisRecommendation recommendation = new DiagnosisRecommendation();
        recommendation.setTenantId(tenant.getId());
        recommendation.setSessionId(session.getId());
        recommendation.setRecType("EXAM");
        recommendation.setContent("do exam");
        recommendation.setPriority(10);
        recommendation.setReason("reason");
        diagnosisRecommendationRepository.save(recommendation);

        assertThat(recommendation.getId()).isNotNull();
        assertThat(diagnosisRecommendationRepository.findById(tenant.getId(), recommendation.getId())).isPresent();
        assertThat(diagnosisRecommendationRepository.listByTenantId(tenant.getId())).extracting(DiagnosisRecommendation::getId).contains(recommendation.getId());
        assertThat(diagnosisRecommendationRepository.listBySessionId(tenant.getId(), session.getId())).extracting(DiagnosisRecommendation::getId).contains(recommendation.getId());

        recommendation.setContent("updated");
        diagnosisRecommendationRepository.update(recommendation);
        assertThat(diagnosisRecommendationRepository.findById(tenant.getId(), recommendation.getId())).get().extracting(DiagnosisRecommendation::getContent).isEqualTo("updated");

        assertThat(diagnosisRecommendationRepository.deleteById(tenant.getId(), recommendation.getId())).isTrue();
        assertThat(diagnosisRecommendationRepository.findById(tenant.getId(), recommendation.getId())).isEmpty();
    }

    @Test
    void doctorFeedbackCrudWithJsonb() {
        Tenant tenant = createTenant();
        DiagnosisSession session = createDiagnosisSession(tenant.getId());
        DiagnosisResult result = createDiagnosisResult(tenant.getId(), session.getId());
        UserAccount doctor = createUserAccount(tenant.getId());

        DoctorFeedback feedback = new DoctorFeedback();
        feedback.setTenantId(tenant.getId());
        feedback.setSessionId(session.getId());
        feedback.setResultId(result.getId());
        feedback.setDoctorId(doctor.getId());
        feedback.setAction("MODIFY");
        feedback.setModifiedValue(OBJECT_MAPPER.createObjectNode().put("confidence", 0.88));
        feedback.setRejectReason("none");
        doctorFeedbackRepository.save(feedback);

        assertThat(feedback.getId()).isNotNull();
        assertThat(doctorFeedbackRepository.findById(tenant.getId(), feedback.getId())).isPresent();
        assertThat(doctorFeedbackRepository.listByTenantId(tenant.getId())).extracting(DoctorFeedback::getId).contains(feedback.getId());
        assertThat(doctorFeedbackRepository.listBySessionId(tenant.getId(), session.getId())).extracting(DoctorFeedback::getId).contains(feedback.getId());
        assertThat(doctorFeedbackRepository.listByResultId(tenant.getId(), result.getId())).extracting(DoctorFeedback::getId).contains(feedback.getId());

        feedback.setModifiedValue(OBJECT_MAPPER.createObjectNode().put("confidence", 0.95));
        doctorFeedbackRepository.update(feedback);
        assertThat(doctorFeedbackRepository.findById(tenant.getId(), feedback.getId())).get()
                .satisfies(value -> assertThat(value.getModifiedValue().get("confidence").asDouble()).isEqualTo(0.95D));

        assertThat(doctorFeedbackRepository.deleteById(tenant.getId(), feedback.getId())).isTrue();
        assertThat(doctorFeedbackRepository.findById(tenant.getId(), feedback.getId())).isEmpty();
    }

    @Test
    void diagnosisSessionQueryPaginatesFiltersAndSortsInSql() {
        Tenant tenant = createTenant();
        Patient patient = createPatient(tenant.getId());
        UserAccount doctorA = createUserAccount(tenant.getId());
        UserAccount doctorB = createUserAccount(tenant.getId());
        Encounter encounter = createEncounter(tenant.getId(), patient.getId(), doctorA.getId());
        ModelRegistry registry = createModelRegistry(tenant.getId());

        DiagnosisSession runningA = createSession(tenant.getId(), patient.getId(), encounter.getId(),
                doctorA.getId(), registry.getId(), "MANUAL", "RUNNING");
        DiagnosisSession completedB = createSession(tenant.getId(), patient.getId(), encounter.getId(),
                doctorB.getId(), registry.getId(), "AUTO", "COMPLETED");
        DiagnosisSession runningC = createSession(tenant.getId(), patient.getId(), encounter.getId(),
                doctorA.getId(), registry.getId(), "MANUAL", "RUNNING");

        DiagnosisSessionQuery runningByA = new DiagnosisSessionQuery();
        runningByA.setTenantId(tenant.getId());
        runningByA.setDoctorId(doctorA.getId());
        runningByA.setStatus("RUNNING");

        assertThat(diagnosisSessionRepository.count(runningByA)).isEqualTo(2L);
        List<DiagnosisSession> firstPage = diagnosisSessionRepository.query(runningByA, 0, 1);
        assertThat(firstPage).hasSize(1);
        // newest first (id desc as tiebreak on equal started_at): the later-inserted runningC
        assertThat(firstPage.get(0).getId()).isEqualTo(runningC.getId());

        List<DiagnosisSession> secondPage = diagnosisSessionRepository.query(runningByA, 1, 1);
        assertThat(secondPage).hasSize(1);
        assertThat(secondPage.get(0).getId()).isEqualTo(runningA.getId());

        // status filter excludes the completed session
        DiagnosisSessionQuery allTenant = new DiagnosisSessionQuery();
        allTenant.setTenantId(tenant.getId());
        assertThat(diagnosisSessionRepository.count(allTenant)).isEqualTo(3L);

        DiagnosisSessionQuery completedOnly = new DiagnosisSessionQuery();
        completedOnly.setTenantId(tenant.getId());
        completedOnly.setStatus("COMPLETED");
        assertThat(diagnosisSessionRepository.count(completedOnly)).isEqualTo(1L);
        assertThat(diagnosisSessionRepository.query(completedOnly, 0, 10))
                .extracting(DiagnosisSession::getId).containsExactly(completedB.getId());
    }

    @Test
    void modelRegistryQueryPaginatesFiltersInSql() {
        Tenant tenant = createTenant();

        ModelRegistry active = createModelRegistryWith(tenant.getId(), "LOCAL", "ML", "ACTIVE");
        ModelRegistry inactive = createModelRegistryWith(tenant.getId(), "OPENAI", "LLM", "INACTIVE");
        ModelRegistry activeRule = createModelRegistryWith(tenant.getId(), "LOCAL", "RULE", "ACTIVE");

        ModelRegistryQuery activeLocal = new ModelRegistryQuery();
        activeLocal.setTenantId(tenant.getId());
        activeLocal.setProvider("LOCAL");
        activeLocal.setStatus("ACTIVE");

        assertThat(modelRegistryRepository.count(activeLocal)).isEqualTo(2L);
        List<ModelRegistry> page = modelRegistryRepository.query(activeLocal, 0, 1);
        assertThat(page).hasSize(1);
        assertThat(modelRegistryRepository.query(activeLocal, 1, 1)).hasSize(1);

        ModelRegistryQuery all = new ModelRegistryQuery();
        all.setTenantId(tenant.getId());
        assertThat(modelRegistryRepository.count(all)).isEqualTo(3L);
    }

    private DiagnosisSession createSession(Long tenantId, Long patientId, Long encounterId,
                                           Long doctorId, Long modelRegistryId, String triggeredBy, String status) {
        DiagnosisSession session = new DiagnosisSession();
        session.setTenantId(tenantId);
        session.setPatientId(patientId);
        session.setEncounterId(encounterId);
        session.setDoctorId(doctorId);
        session.setTriggeredBy(triggeredBy);
        session.setModelRegistryId(modelRegistryId);
        session.setInputSnapshot(OBJECT_MAPPER.createObjectNode().put("k", "v"));
        session.setStatus(status);
        session.setStartedAt(OffsetDateTime.now().withNano(0));
        diagnosisSessionRepository.save(session);
        return session;
    }

    private ModelRegistry createModelRegistryWith(Long tenantId, String provider, String modelType, String status) {
        ModelRegistry registry = new ModelRegistry();
        registry.setTenantId(tenantId);
        registry.setModelCode("MODEL_" + unique("code"));
        registry.setModelName("Model");
        registry.setModelType(modelType);
        registry.setModelVersion("v" + System.nanoTime());
        registry.setProvider(provider);
        registry.setStatus(status);
        registry.setReleasedAt(OffsetDateTime.now().withNano(0));
        modelRegistryRepository.save(registry);
        return registry;
    }

    private ModelRegistry createModelRegistry(Long tenantId) {
        ModelRegistry registry = new ModelRegistry();
        registry.setTenantId(tenantId);
        registry.setModelCode("MODEL_" + unique("code"));
        registry.setModelName("Model");
        registry.setModelType("RULE");
        registry.setModelVersion("v1");
        registry.setProvider("LOCAL");
        registry.setStatus("ACTIVE");
        modelRegistryRepository.save(registry);
        return registry;
    }

    private DiagnosisSession createDiagnosisSession(Long tenantId) {
        Patient patient = createPatient(tenantId);
        UserAccount doctor = createUserAccount(tenantId);
        Encounter encounter = createEncounter(tenantId, patient.getId(), doctor.getId());
        ModelRegistry registry = createModelRegistry(tenantId);

        DiagnosisSession session = new DiagnosisSession();
        session.setTenantId(tenantId);
        session.setPatientId(patient.getId());
        session.setEncounterId(encounter.getId());
        session.setDoctorId(doctor.getId());
        session.setTriggeredBy("MANUAL");
        session.setModelRegistryId(registry.getId());
        session.setInputSnapshot(OBJECT_MAPPER.createObjectNode().put("x", 1));
        session.setStatus("RUNNING");
        session.setStartedAt(OffsetDateTime.now().withNano(0));
        diagnosisSessionRepository.save(session);
        return session;
    }

    private DiagnosisResult createDiagnosisResult(Long tenantId, Long sessionId) {
        DiagnosisResult result = new DiagnosisResult();
        result.setTenantId(tenantId);
        result.setSessionId(sessionId);
        result.setDiseaseCode("DC");
        result.setDiseaseName("Name");
        result.setConfidence(0.9D);
        result.setRankNo(1);
        result.setRiskLevel("HIGH");
        result.setEvidenceJson(OBJECT_MAPPER.createObjectNode().put("e", "x"));
        result.setIsDisplayToPatient(false);
        diagnosisResultRepository.save(result);
        return result;
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
