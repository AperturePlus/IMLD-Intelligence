package xenosoft.imldintelligence.module.screening.internal.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.transaction.annotation.Transactional;
import xenosoft.imldintelligence.AbstractPostgresIntegrationTest;
import xenosoft.imldintelligence.module.identity.internal.model.Patient;
import xenosoft.imldintelligence.module.identity.internal.model.Tenant;
import xenosoft.imldintelligence.module.identity.internal.model.TocUser;
import xenosoft.imldintelligence.module.identity.internal.model.UserAccount;
import xenosoft.imldintelligence.module.identity.internal.repository.PatientRepository;
import xenosoft.imldintelligence.module.identity.internal.repository.TenantRepository;
import xenosoft.imldintelligence.module.identity.internal.repository.TocUserRepository;
import xenosoft.imldintelligence.module.identity.internal.repository.UserAccountRepository;
import xenosoft.imldintelligence.module.screening.model.Questionnaire;
import xenosoft.imldintelligence.module.screening.model.QuestionnaireQuestion;
import xenosoft.imldintelligence.module.screening.model.QuestionnaireResponse;
import xenosoft.imldintelligence.module.screening.model.TocClinicalTransfer;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisabledInAotMode
class ScreeningRepositoryIntegrationTest extends AbstractPostgresIntegrationTest {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private TocUserRepository tocUserRepository;

    @Autowired
    private QuestionnaireRepository questionnaireRepository;

    @Autowired
    private QuestionnaireQuestionRepository questionnaireQuestionRepository;

    @Autowired
    private QuestionnaireResponseRepository questionnaireResponseRepository;

    @Autowired
    private TocClinicalTransferRepository tocClinicalTransferRepository;

    @Test
    void questionnaireCrudAndTenantIsolation() {
        Tenant tenantA = createTenant();
        Tenant tenantB = createTenant();

        Questionnaire questionnaire = new Questionnaire();
        questionnaire.setTenantId(tenantA.getId());
        questionnaire.setQuestionnaireCode("Q_" + unique("code"));
        questionnaire.setTitle("Questionnaire");
        questionnaire.setDescription("desc");
        questionnaire.setDiseaseScope("D001");
        questionnaire.setStatus("ACTIVE");
        questionnaire.setVersionNo(1);
        questionnaire.setValidatedFlag(true);
        questionnaireRepository.save(questionnaire);

        assertThat(questionnaire.getId()).isNotNull();
        assertThat(questionnaireRepository.findById(tenantA.getId(), questionnaire.getId())).isPresent();
        assertThat(questionnaireRepository.findById(tenantB.getId(), questionnaire.getId())).isEmpty();
        assertThat(questionnaireRepository.findByQuestionnaireCodeAndVersionNo(tenantA.getId(), questionnaire.getQuestionnaireCode(), 1)).isPresent();
        assertThat(questionnaireRepository.listByTenantId(tenantA.getId())).extracting(Questionnaire::getId).contains(questionnaire.getId());

        questionnaire.setTitle("Updated");
        questionnaireRepository.update(questionnaire);
        assertThat(questionnaireRepository.findById(tenantA.getId(), questionnaire.getId())).get().extracting(Questionnaire::getTitle).isEqualTo("Updated");

        assertThat(questionnaireRepository.deleteById(tenantA.getId(), questionnaire.getId())).isTrue();
        assertThat(questionnaireRepository.findById(tenantA.getId(), questionnaire.getId())).get().extracting(Questionnaire::getStatus).isEqualTo("INACTIVE");
    }

    @Test
    void questionnaireQuestionCrudWithJsonb() {
        Tenant tenant = createTenant();
        Questionnaire questionnaire = createQuestionnaire(tenant.getId());

        QuestionnaireQuestion question = new QuestionnaireQuestion();
        question.setTenantId(tenant.getId());
        question.setQuestionnaireId(questionnaire.getId());
        question.setQuestionNo("Q1");
        question.setContent("How are you?");
        question.setQuestionType("SINGLE");
        question.setOptionsJson(OBJECT_MAPPER.createArrayNode().add("GOOD").add("BAD"));
        question.setScoringRuleJson(OBJECT_MAPPER.createObjectNode().put("GOOD", 0).put("BAD", 5));
        question.setSortOrder(1);
        question.setRequiredFlag(true);
        questionnaireQuestionRepository.save(question);

        assertThat(question.getId()).isNotNull();
        assertThat(questionnaireQuestionRepository.findById(tenant.getId(), question.getId())).isPresent();
        assertThat(questionnaireQuestionRepository.findByQuestionnaireIdAndQuestionNo(tenant.getId(), questionnaire.getId(), "Q1")).isPresent();
        assertThat(questionnaireQuestionRepository.listByTenantId(tenant.getId())).extracting(QuestionnaireQuestion::getId).contains(question.getId());
        assertThat(questionnaireQuestionRepository.listByQuestionnaireId(tenant.getId(), questionnaire.getId())).extracting(QuestionnaireQuestion::getId).contains(question.getId());

        question.setScoringRuleJson(OBJECT_MAPPER.createObjectNode().put("GOOD", 1));
        questionnaireQuestionRepository.update(question);
        assertThat(questionnaireQuestionRepository.findById(tenant.getId(), question.getId())).get()
                .satisfies(value -> assertThat(value.getScoringRuleJson().get("GOOD").asInt()).isEqualTo(1));

        assertThat(questionnaireQuestionRepository.deleteById(tenant.getId(), question.getId())).isTrue();
        assertThat(questionnaireQuestionRepository.findById(tenant.getId(), question.getId())).isEmpty();
    }

    @Test
    void questionnaireResponseCrud() {
        Tenant tenant = createTenant();
        Questionnaire questionnaire = createQuestionnaire(tenant.getId());
        TocUser tocUser = createTocUser(tenant.getId());

        QuestionnaireResponse response = new QuestionnaireResponse();
        response.setTenantId(tenant.getId());
        response.setQuestionnaireId(questionnaire.getId());
        response.setTocUserId(tocUser.getId());
        response.setResponseNo("RES_" + unique("no"));
        response.setRiskScore(new java.math.BigDecimal("1.23"));
        response.setRiskLevel("LOW");
        response.setSuggestion("ok");
        response.setCanShowPatient(true);
        response.setDataConsent(true);
        response.setExpiresAt(OffsetDateTime.now().plusDays(30).withNano(0));
        questionnaireResponseRepository.save(response);

        assertThat(response.getId()).isNotNull();
        assertThat(questionnaireResponseRepository.findById(tenant.getId(), response.getId())).isPresent();
        assertThat(questionnaireResponseRepository.findByResponseNo(tenant.getId(), response.getResponseNo())).isPresent();
        assertThat(questionnaireResponseRepository.listByTenantId(tenant.getId())).extracting(QuestionnaireResponse::getId).contains(response.getId());
        assertThat(questionnaireResponseRepository.listByQuestionnaireId(tenant.getId(), questionnaire.getId())).extracting(QuestionnaireResponse::getId).contains(response.getId());
        assertThat(questionnaireResponseRepository.listByTocUserId(tenant.getId(), tocUser.getId())).extracting(QuestionnaireResponse::getId).contains(response.getId());

        response.setRiskLevel("MEDIUM");
        questionnaireResponseRepository.update(response);
        assertThat(questionnaireResponseRepository.findById(tenant.getId(), response.getId())).get().extracting(QuestionnaireResponse::getRiskLevel).isEqualTo("MEDIUM");

        assertThat(questionnaireResponseRepository.deleteById(tenant.getId(), response.getId())).isTrue();
        assertThat(questionnaireResponseRepository.findById(tenant.getId(), response.getId())).isEmpty();
    }

    @Test
    void tocClinicalTransferCrud() {
        Tenant tenant = createTenant();
        UserAccount doctor = createUserAccount(tenant.getId());
        Patient patient = createPatient(tenant.getId());
        Questionnaire questionnaire = createQuestionnaire(tenant.getId());
        TocUser tocUser = createTocUser(tenant.getId());
        QuestionnaireResponse response = createQuestionnaireResponse(tenant.getId(), questionnaire.getId(), tocUser.getId());

        TocClinicalTransfer transfer = new TocClinicalTransfer();
        transfer.setTenantId(tenant.getId());
        transfer.setResponseId(response.getId());
        transfer.setPatientId(patient.getId());
        transfer.setTransferStatus("PENDING");
        transfer.setApprovedBy(doctor.getId());
        transfer.setApprovedAt(OffsetDateTime.now().withNano(0));
        transfer.setTransferNote("note");
        tocClinicalTransferRepository.save(transfer);

        assertThat(transfer.getId()).isNotNull();
        assertThat(tocClinicalTransferRepository.findById(tenant.getId(), transfer.getId())).isPresent();
        assertThat(tocClinicalTransferRepository.listByTenantId(tenant.getId())).extracting(TocClinicalTransfer::getId).contains(transfer.getId());
        assertThat(tocClinicalTransferRepository.listByResponseId(tenant.getId(), response.getId())).extracting(TocClinicalTransfer::getId).contains(transfer.getId());
        assertThat(tocClinicalTransferRepository.listByPatientId(tenant.getId(), patient.getId())).extracting(TocClinicalTransfer::getId).contains(transfer.getId());

        transfer.setTransferStatus("COMPLETED");
        tocClinicalTransferRepository.update(transfer);
        assertThat(tocClinicalTransferRepository.findById(tenant.getId(), transfer.getId())).get().extracting(TocClinicalTransfer::getTransferStatus).isEqualTo("COMPLETED");

        assertThat(tocClinicalTransferRepository.deleteById(tenant.getId(), transfer.getId())).isTrue();
        assertThat(tocClinicalTransferRepository.findById(tenant.getId(), transfer.getId())).isEmpty();
    }

    private Questionnaire createQuestionnaire(Long tenantId) {
        Questionnaire questionnaire = new Questionnaire();
        questionnaire.setTenantId(tenantId);
        questionnaire.setQuestionnaireCode("Q_" + unique("code"));
        questionnaire.setTitle("Questionnaire");
        questionnaire.setStatus("ACTIVE");
        questionnaire.setVersionNo(1);
        questionnaire.setValidatedFlag(true);
        questionnaireRepository.save(questionnaire);
        return questionnaire;
    }

    private QuestionnaireResponse createQuestionnaireResponse(Long tenantId, Long questionnaireId, Long tocUserId) {
        QuestionnaireResponse response = new QuestionnaireResponse();
        response.setTenantId(tenantId);
        response.setQuestionnaireId(questionnaireId);
        response.setTocUserId(tocUserId);
        response.setResponseNo("RES_" + unique("no"));
        response.setCanShowPatient(true);
        response.setDataConsent(false);
        questionnaireResponseRepository.save(response);
        return response;
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

    private TocUser createTocUser(Long tenantId) {
        String suffix = unique("toc");
        TocUser tocUser = new TocUser();
        tocUser.setTenantId(tenantId);
        tocUser.setTocUid("TOC_" + suffix);
        tocUser.setNickname("Nick");
        tocUser.setVipStatus("NORMAL");
        tocUser.setStatus("ACTIVE");
        tocUserRepository.save(tocUser);
        return tocUser;
    }

    private String unique(String prefix) {
        return prefix + "_" + System.nanoTime();
    }
}

