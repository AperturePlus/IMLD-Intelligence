package xenosoft.imldintelligence.module.careplan.internal.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.transaction.annotation.Transactional;
import xenosoft.imldintelligence.AbstractPostgresIntegrationTest;
import xenosoft.imldintelligence.module.careplan.model.AlertAction;
import xenosoft.imldintelligence.module.careplan.model.AlertEvent;
import xenosoft.imldintelligence.module.careplan.model.CarePlan;
import xenosoft.imldintelligence.module.careplan.model.CarePlanTemplate;
import xenosoft.imldintelligence.module.careplan.model.FollowupTask;
import xenosoft.imldintelligence.module.careplan.model.PatientReportedData;
import xenosoft.imldintelligence.module.identity.internal.model.Patient;
import xenosoft.imldintelligence.module.identity.internal.model.Tenant;
import xenosoft.imldintelligence.module.identity.internal.model.UserAccount;
import xenosoft.imldintelligence.module.identity.internal.repository.PatientRepository;
import xenosoft.imldintelligence.module.identity.internal.repository.TenantRepository;
import xenosoft.imldintelligence.module.identity.internal.repository.UserAccountRepository;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisabledInAotMode
class CareplanRepositoryIntegrationTest extends AbstractPostgresIntegrationTest {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private CarePlanTemplateRepository carePlanTemplateRepository;

    @Autowired
    private CarePlanRepository carePlanRepository;

    @Autowired
    private FollowupTaskRepository followupTaskRepository;

    @Autowired
    private PatientReportedDataRepository patientReportedDataRepository;

    @Autowired
    private AlertEventRepository alertEventRepository;

    @Autowired
    private AlertActionRepository alertActionRepository;

    @Test
    void carePlanTemplateCrudWithJsonbAndTenantIsolation() {
        Tenant tenantA = createTenant();
        Tenant tenantB = createTenant();
        UserAccount doctor = createUserAccount(tenantA.getId());

        CarePlanTemplate template = new CarePlanTemplate();
        template.setTenantId(tenantA.getId());
        template.setTemplateCode("TPL_" + unique("code"));
        template.setTemplateName("Template");
        template.setPlanSchema(OBJECT_MAPPER.createObjectNode().put("freq", "daily"));
        template.setStatus("ACTIVE");
        template.setVersionNo(1);
        template.setCreatedBy(doctor.getId());
        carePlanTemplateRepository.save(template);

        assertThat(template.getId()).isNotNull();
        assertThat(carePlanTemplateRepository.findById(tenantA.getId(), template.getId())).isPresent();
        assertThat(carePlanTemplateRepository.findById(tenantB.getId(), template.getId())).isEmpty();
        assertThat(carePlanTemplateRepository.findByTemplateCodeAndVersionNo(tenantA.getId(), template.getTemplateCode(), 1)).isPresent();
        assertThat(carePlanTemplateRepository.listByTenantId(tenantA.getId())).extracting(CarePlanTemplate::getId).contains(template.getId());

        template.setPlanSchema(OBJECT_MAPPER.createObjectNode().put("freq", "weekly"));
        carePlanTemplateRepository.update(template);
        assertThat(carePlanTemplateRepository.findById(tenantA.getId(), template.getId())).get()
                .satisfies(value -> assertThat(value.getPlanSchema().get("freq").asText()).isEqualTo("weekly"));

        assertThat(carePlanTemplateRepository.deleteById(tenantA.getId(), template.getId())).isTrue();
        assertThat(carePlanTemplateRepository.findById(tenantA.getId(), template.getId())).get().extracting(CarePlanTemplate::getStatus).isEqualTo("INACTIVE");
    }

    @Test
    void carePlanCrud() {
        Tenant tenant = createTenant();
        UserAccount doctor = createUserAccount(tenant.getId());
        Patient patient = createPatient(tenant.getId());
        CarePlanTemplate template = createCarePlanTemplate(tenant.getId(), doctor.getId());

        CarePlan carePlan = new CarePlan();
        carePlan.setTenantId(tenant.getId());
        carePlan.setPatientId(patient.getId());
        carePlan.setDiseaseCode("D001");
        carePlan.setPlanType("STANDARD");
        carePlan.setEnrollmentType("DOCTOR_ASSIGNED");
        carePlan.setTemplateId(template.getId());
        carePlan.setStatus("ACTIVE");
        carePlan.setStartDate(LocalDate.now());
        carePlan.setEndDate(LocalDate.now().plusDays(30));
        carePlan.setCreatedBy(doctor.getId());
        carePlanRepository.save(carePlan);

        assertThat(carePlan.getId()).isNotNull();
        assertThat(carePlanRepository.findById(tenant.getId(), carePlan.getId())).isPresent();
        assertThat(carePlanRepository.listByTenantId(tenant.getId())).extracting(CarePlan::getId).contains(carePlan.getId());
        assertThat(carePlanRepository.listByPatientId(tenant.getId(), patient.getId())).extracting(CarePlan::getId).contains(carePlan.getId());

        carePlan.setStatus("PAUSED");
        carePlanRepository.update(carePlan);
        assertThat(carePlanRepository.findById(tenant.getId(), carePlan.getId())).get().extracting(CarePlan::getStatus).isEqualTo("PAUSED");

        assertThat(carePlanRepository.deleteById(tenant.getId(), carePlan.getId())).isTrue();
        assertThat(carePlanRepository.findById(tenant.getId(), carePlan.getId())).isEmpty();
    }

    @Test
    void followupTaskCrud() {
        Tenant tenant = createTenant();
        UserAccount doctor = createUserAccount(tenant.getId());
        Patient patient = createPatient(tenant.getId());
        CarePlan carePlan = createCarePlan(tenant.getId(), patient.getId(), doctor.getId());

        FollowupTask task = new FollowupTask();
        task.setTenantId(tenant.getId());
        task.setCarePlanId(carePlan.getId());
        task.setPatientId(patient.getId());
        task.setTaskType("PHONE_CALL");
        task.setScheduledAt(OffsetDateTime.now().plusDays(1).withNano(0));
        task.setChannel("PHONE");
        task.setStatus("PENDING");
        task.setAssignedTo(doctor.getId());
        followupTaskRepository.save(task);

        assertThat(task.getId()).isNotNull();
        assertThat(followupTaskRepository.findById(tenant.getId(), task.getId())).isPresent();
        assertThat(followupTaskRepository.listByTenantId(tenant.getId())).extracting(FollowupTask::getId).contains(task.getId());
        assertThat(followupTaskRepository.listByCarePlanId(tenant.getId(), carePlan.getId())).extracting(FollowupTask::getId).contains(task.getId());
        assertThat(followupTaskRepository.listByPatientId(tenant.getId(), patient.getId())).extracting(FollowupTask::getId).contains(task.getId());

        task.setStatus("COMPLETED");
        task.setCompletedAt(OffsetDateTime.now().withNano(0));
        followupTaskRepository.update(task);
        assertThat(followupTaskRepository.findById(tenant.getId(), task.getId())).get().extracting(FollowupTask::getStatus).isEqualTo("COMPLETED");

        assertThat(followupTaskRepository.deleteById(tenant.getId(), task.getId())).isTrue();
        assertThat(followupTaskRepository.findById(tenant.getId(), task.getId())).isEmpty();
    }

    @Test
    void patientReportedDataCrudWithJsonb() {
        Tenant tenant = createTenant();
        UserAccount doctor = createUserAccount(tenant.getId());
        Patient patient = createPatient(tenant.getId());
        CarePlan carePlan = createCarePlan(tenant.getId(), patient.getId(), doctor.getId());

        PatientReportedData data = new PatientReportedData();
        data.setTenantId(tenant.getId());
        data.setPatientId(patient.getId());
        data.setCarePlanId(carePlan.getId());
        data.setIndicatorCode("WEIGHT");
        data.setValueNumeric(65.2D);
        data.setSource("DEVICE_SYNC");
        data.setDeviceInfo(OBJECT_MAPPER.createObjectNode().put("device", "watch"));
        data.setRecordedAt(OffsetDateTime.now().withNano(0));
        patientReportedDataRepository.save(data);

        assertThat(data.getId()).isNotNull();
        assertThat(patientReportedDataRepository.findById(tenant.getId(), data.getId())).isPresent();
        assertThat(patientReportedDataRepository.listByTenantId(tenant.getId())).extracting(PatientReportedData::getId).contains(data.getId());
        assertThat(patientReportedDataRepository.listByCarePlanId(tenant.getId(), carePlan.getId())).extracting(PatientReportedData::getId).contains(data.getId());
        assertThat(patientReportedDataRepository.listByPatientId(tenant.getId(), patient.getId())).extracting(PatientReportedData::getId).contains(data.getId());

        data.setDeviceInfo(OBJECT_MAPPER.createObjectNode().put("device", "updated"));
        patientReportedDataRepository.update(data);
        assertThat(patientReportedDataRepository.findById(tenant.getId(), data.getId())).get()
                .satisfies(value -> assertThat(value.getDeviceInfo().get("device").asText()).isEqualTo("updated"));

        assertThat(patientReportedDataRepository.deleteById(tenant.getId(), data.getId())).isTrue();
        assertThat(patientReportedDataRepository.findById(tenant.getId(), data.getId())).isEmpty();
    }

    @Test
    void alertEventCrudWithJsonb() {
        Tenant tenant = createTenant();
        UserAccount doctor = createUserAccount(tenant.getId());
        Patient patient = createPatient(tenant.getId());
        CarePlan carePlan = createCarePlan(tenant.getId(), patient.getId(), doctor.getId());

        AlertEvent event = new AlertEvent();
        event.setTenantId(tenant.getId());
        event.setPatientId(patient.getId());
        event.setCarePlanId(carePlan.getId());
        event.setTriggerType("THRESHOLD");
        event.setTriggerDetail(OBJECT_MAPPER.createObjectNode().put("bp", "160/100"));
        event.setSeverity("WARNING");
        event.setStatus("OPEN");
        event.setAssignedTo(doctor.getId());
        alertEventRepository.save(event);

        assertThat(event.getId()).isNotNull();
        assertThat(alertEventRepository.findById(tenant.getId(), event.getId())).isPresent();
        assertThat(alertEventRepository.listByTenantId(tenant.getId())).extracting(AlertEvent::getId).contains(event.getId());
        assertThat(alertEventRepository.listByCarePlanId(tenant.getId(), carePlan.getId())).extracting(AlertEvent::getId).contains(event.getId());
        assertThat(alertEventRepository.listByPatientId(tenant.getId(), patient.getId())).extracting(AlertEvent::getId).contains(event.getId());

        event.setTriggerDetail(OBJECT_MAPPER.createObjectNode().put("bp", "170/110"));
        event.setStatus("ACKNOWLEDGED");
        alertEventRepository.update(event);
        assertThat(alertEventRepository.findById(tenant.getId(), event.getId())).get()
                .satisfies(value -> {
                    assertThat(value.getStatus()).isEqualTo("ACKNOWLEDGED");
                    assertThat(value.getTriggerDetail().get("bp").asText()).isEqualTo("170/110");
                });

        assertThat(alertEventRepository.deleteById(tenant.getId(), event.getId())).isTrue();
        assertThat(alertEventRepository.findById(tenant.getId(), event.getId())).isEmpty();
    }

    @Test
    void alertActionCrud() {
        Tenant tenant = createTenant();
        UserAccount doctor = createUserAccount(tenant.getId());
        Patient patient = createPatient(tenant.getId());
        CarePlan carePlan = createCarePlan(tenant.getId(), patient.getId(), doctor.getId());
        AlertEvent event = createAlertEvent(tenant.getId(), patient.getId(), carePlan.getId(), doctor.getId());

        AlertAction action = new AlertAction();
        action.setTenantId(tenant.getId());
        action.setAlertId(event.getId());
        action.setActionType("ACK");
        action.setActionBy(doctor.getId());
        action.setActionNote("acked");
        alertActionRepository.save(action);

        assertThat(action.getId()).isNotNull();
        assertThat(alertActionRepository.findById(tenant.getId(), action.getId())).isPresent();
        assertThat(alertActionRepository.listByTenantId(tenant.getId())).extracting(AlertAction::getId).contains(action.getId());
        assertThat(alertActionRepository.listByAlertId(tenant.getId(), event.getId())).extracting(AlertAction::getId).contains(action.getId());

        action.setActionType("RESOLVE");
        alertActionRepository.update(action);
        assertThat(alertActionRepository.findById(tenant.getId(), action.getId())).get().extracting(AlertAction::getActionType).isEqualTo("RESOLVE");

        assertThat(alertActionRepository.deleteById(tenant.getId(), action.getId())).isTrue();
        assertThat(alertActionRepository.findById(tenant.getId(), action.getId())).isEmpty();
    }

    private AlertEvent createAlertEvent(Long tenantId, Long patientId, Long carePlanId, Long assignedTo) {
        AlertEvent event = new AlertEvent();
        event.setTenantId(tenantId);
        event.setPatientId(patientId);
        event.setCarePlanId(carePlanId);
        event.setTriggerType("THRESHOLD");
        event.setTriggerDetail(OBJECT_MAPPER.createObjectNode().put("x", 1));
        event.setSeverity("WARNING");
        event.setStatus("OPEN");
        event.setAssignedTo(assignedTo);
        alertEventRepository.save(event);
        return event;
    }

    private CarePlan createCarePlan(Long tenantId, Long patientId, Long createdBy) {
        CarePlanTemplate template = createCarePlanTemplate(tenantId, createdBy);
        CarePlan carePlan = new CarePlan();
        carePlan.setTenantId(tenantId);
        carePlan.setPatientId(patientId);
        carePlan.setDiseaseCode("D001");
        carePlan.setPlanType("STANDARD");
        carePlan.setEnrollmentType("DOCTOR_ASSIGNED");
        carePlan.setTemplateId(template.getId());
        carePlan.setStatus("ACTIVE");
        carePlan.setStartDate(LocalDate.now());
        carePlan.setCreatedBy(createdBy);
        carePlanRepository.save(carePlan);
        return carePlan;
    }

    private CarePlanTemplate createCarePlanTemplate(Long tenantId, Long createdBy) {
        CarePlanTemplate template = new CarePlanTemplate();
        template.setTenantId(tenantId);
        template.setTemplateCode("TPL_" + unique("code"));
        template.setTemplateName("Template");
        template.setPlanSchema(OBJECT_MAPPER.createObjectNode().put("f", "d"));
        template.setStatus("ACTIVE");
        template.setVersionNo(1);
        template.setCreatedBy(createdBy);
        carePlanTemplateRepository.save(template);
        return template;
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

    private String unique(String prefix) {
        return prefix + "_" + System.nanoTime();
    }
}

