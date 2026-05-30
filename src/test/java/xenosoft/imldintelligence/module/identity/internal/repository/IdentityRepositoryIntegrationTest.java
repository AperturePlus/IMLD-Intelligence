package xenosoft.imldintelligence.module.identity.internal.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.transaction.annotation.Transactional;
import xenosoft.imldintelligence.AbstractPostgresIntegrationTest;
import xenosoft.imldintelligence.module.identity.internal.model.AbacPolicy;
import xenosoft.imldintelligence.module.identity.internal.model.ConsentRecord;
import xenosoft.imldintelligence.module.identity.internal.model.Encounter;
import xenosoft.imldintelligence.module.identity.internal.model.GuardianRelation;
import xenosoft.imldintelligence.module.identity.internal.model.Patient;
import xenosoft.imldintelligence.module.identity.internal.model.PatientExternalId;
import xenosoft.imldintelligence.module.identity.internal.model.Role;
import xenosoft.imldintelligence.module.identity.internal.model.Tenant;
import xenosoft.imldintelligence.module.identity.internal.model.TocUser;
import xenosoft.imldintelligence.module.identity.internal.model.UserAccount;
import xenosoft.imldintelligence.module.identity.internal.model.UserRoleRel;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisabledInAotMode
class IdentityRepositoryIntegrationTest extends AbstractPostgresIntegrationTest {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRoleRelRepository userRoleRelRepository;

    @Autowired
    private AbacPolicyRepository abacPolicyRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private PatientExternalIdRepository patientExternalIdRepository;

    @Autowired
    private TocUserRepository tocUserRepository;

    @Autowired
    private GuardianRelationRepository guardianRelationRepository;

    @Autowired
    private ConsentRecordRepository consentRecordRepository;

    @Autowired
    private EncounterRepository encounterRepository;

    @Test
    void tenantCrud() {
        String suffix = unique("tenant");
        Tenant tenant = new Tenant();
        tenant.setTenantCode("TENANT_" + suffix);
        tenant.setTenantName("Tenant " + suffix);
        tenant.setDeployMode("SAAS");
        tenant.setStatus("ACTIVE");
        tenantRepository.save(tenant);

        assertThat(tenant.getId()).isNotNull();
        assertThat(tenantRepository.findById(tenant.getId())).isPresent();
        assertThat(tenantRepository.findByTenantCode(tenant.getTenantCode())).isPresent();
        assertThat(tenantRepository.listAll()).extracting(Tenant::getId).contains(tenant.getId());

        tenant.setTenantName("Updated " + suffix);
        tenantRepository.update(tenant);
        assertThat(tenantRepository.findById(tenant.getId())).get().extracting(Tenant::getTenantName).isEqualTo("Updated " + suffix);

        assertThat(tenantRepository.deleteById(tenant.getId())).isTrue();
        assertThat(tenantRepository.findById(tenant.getId())).get().extracting(Tenant::getStatus).isEqualTo("INACTIVE");
    }

    @Test
    void userAccountCrud() {
        Tenant tenant = createTenant();
        UserAccount userAccount = createUserAccount(tenant.getId());

        assertThat(userAccountRepository.findById(tenant.getId(), userAccount.getId())).isPresent();
        assertThat(userAccountRepository.findByUserNo(tenant.getId(), userAccount.getUserNo())).isPresent();
        assertThat(userAccountRepository.findByUsername(tenant.getId(), userAccount.getUsername())).isPresent();
        assertThat(userAccountRepository.listByTenantId(tenant.getId())).extracting(UserAccount::getId).contains(userAccount.getId());

        userAccount.setDisplayName("Display Updated");
        userAccountRepository.update(userAccount);
        assertThat(userAccountRepository.findById(tenant.getId(), userAccount.getId())).get().extracting(UserAccount::getDisplayName).isEqualTo("Display Updated");

        assertThat(userAccountRepository.deleteById(tenant.getId(), userAccount.getId())).isTrue();
        assertThat(userAccountRepository.findById(tenant.getId(), userAccount.getId())).get().extracting(UserAccount::getStatus).isEqualTo("INACTIVE");
    }

    @Test
    void roleCrud() {
        Tenant tenant = createTenant();
        Role role = createRole(tenant.getId());

        assertThat(roleRepository.findById(tenant.getId(), role.getId())).isPresent();
        assertThat(roleRepository.findByRoleCode(tenant.getId(), role.getRoleCode())).isPresent();
        assertThat(roleRepository.listByTenantId(tenant.getId())).extracting(Role::getId).contains(role.getId());

        role.setRoleName("Role Updated");
        roleRepository.update(role);
        assertThat(roleRepository.findById(tenant.getId(), role.getId())).get().extracting(Role::getRoleName).isEqualTo("Role Updated");

        assertThat(roleRepository.deleteById(tenant.getId(), role.getId())).isTrue();
        assertThat(roleRepository.findById(tenant.getId(), role.getId())).get().extracting(Role::getStatus).isEqualTo("INACTIVE");
    }

    @Test
    void userRoleRelCrud() {
        Tenant tenant = createTenant();
        UserAccount userAccount = createUserAccount(tenant.getId());
        Role role = createRole(tenant.getId());

        UserRoleRel userRoleRel = new UserRoleRel();
        userRoleRel.setTenantId(tenant.getId());
        userRoleRel.setUserId(userAccount.getId());
        userRoleRel.setRoleId(role.getId());
        userRoleRel.setGrantedBy(userAccount.getId());
        userRoleRel.setGrantedAt(OffsetDateTime.now().withNano(0));
        userRoleRelRepository.save(userRoleRel);

        assertThat(userRoleRel.getId()).isNotNull();
        assertThat(userRoleRelRepository.findById(tenant.getId(), userRoleRel.getId())).isPresent();
        assertThat(userRoleRelRepository.findByUserIdAndRoleId(tenant.getId(), userAccount.getId(), role.getId())).isPresent();
        assertThat(userRoleRelRepository.listByUserId(tenant.getId(), userAccount.getId())).extracting(UserRoleRel::getId).contains(userRoleRel.getId());
        assertThat(userRoleRelRepository.listByRoleId(tenant.getId(), role.getId())).extracting(UserRoleRel::getId).contains(userRoleRel.getId());

        userRoleRel.setGrantedAt(OffsetDateTime.now().plusMinutes(1).withNano(0));
        userRoleRelRepository.update(userRoleRel);
        assertThat(userRoleRelRepository.findById(tenant.getId(), userRoleRel.getId())).get()
                .satisfies(actual -> assertThat(actual.getGrantedAt().toInstant()).isEqualTo(userRoleRel.getGrantedAt().toInstant()));

        assertThat(userRoleRelRepository.deleteByUserIdAndRoleId(tenant.getId(), userAccount.getId(), role.getId())).isTrue();
        assertThat(userRoleRelRepository.findById(tenant.getId(), userRoleRel.getId())).isEmpty();
    }

    @Test
    void abacPolicyCrudWithJsonb() {
        Tenant tenant = createTenant();

        AbacPolicy abacPolicy = new AbacPolicy();
        abacPolicy.setTenantId(tenant.getId());
        abacPolicy.setPolicyCode("POL_" + unique("code"));
        abacPolicy.setPolicyName("Policy");
        abacPolicy.setSubjectExpr(OBJECT_MAPPER.createObjectNode().put("role", "DOCTOR"));
        abacPolicy.setResourceExpr(OBJECT_MAPPER.createObjectNode().put("resource", "PATIENT"));
        abacPolicy.setActionExpr(OBJECT_MAPPER.createObjectNode().put("action", "READ"));
        abacPolicy.setEffect("ALLOW");
        abacPolicy.setPriority(100);
        abacPolicy.setStatus("ACTIVE");
        abacPolicyRepository.save(abacPolicy);

        assertThat(abacPolicy.getId()).isNotNull();
        assertThat(abacPolicyRepository.findById(tenant.getId(), abacPolicy.getId())).isPresent();
        assertThat(abacPolicyRepository.findByPolicyCode(tenant.getId(), abacPolicy.getPolicyCode())).isPresent();
        assertThat(abacPolicyRepository.listByTenantId(tenant.getId())).extracting(AbacPolicy::getId).contains(abacPolicy.getId());
        assertThat(abacPolicyRepository.findById(tenant.getId(), abacPolicy.getId())).get()
                .satisfies(policy -> assertThat(policy.getSubjectExpr().get("role").asText()).isEqualTo("DOCTOR"));

        abacPolicy.setPolicyName("Policy Updated");
        abacPolicy.setPriority(50);
        abacPolicy.setActionExpr(OBJECT_MAPPER.createObjectNode().put("action", "UPDATE"));
        abacPolicyRepository.update(abacPolicy);
        assertThat(abacPolicyRepository.findById(tenant.getId(), abacPolicy.getId())).get()
                .satisfies(policy -> {
                    assertThat(policy.getPolicyName()).isEqualTo("Policy Updated");
                    assertThat(policy.getPriority()).isEqualTo(50);
                    assertThat(policy.getActionExpr().get("action").asText()).isEqualTo("UPDATE");
                });

        assertThat(abacPolicyRepository.deleteById(tenant.getId(), abacPolicy.getId())).isTrue();
        assertThat(abacPolicyRepository.findById(tenant.getId(), abacPolicy.getId())).get().extracting(AbacPolicy::getStatus).isEqualTo("INACTIVE");
    }

    @Test
    void patientCrud() {
        Tenant tenant = createTenant();
        Patient patient = createPatient(tenant.getId());

        assertThat(patientRepository.findById(tenant.getId(), patient.getId())).isPresent();
        assertThat(patientRepository.findByPatientNo(tenant.getId(), patient.getPatientNo())).isPresent();
        assertThat(patientRepository.listByTenantId(tenant.getId())).extracting(Patient::getId).contains(patient.getId());

        patient.setPatientName("Patient Updated");
        patientRepository.update(patient);
        assertThat(patientRepository.findById(tenant.getId(), patient.getId())).get().extracting(Patient::getPatientName).isEqualTo("Patient Updated");

        assertThat(patientRepository.deleteByPatientNo(tenant.getId(), patient.getPatientNo())).isTrue();
        assertThat(patientRepository.findById(tenant.getId(), patient.getId())).get().extracting(Patient::getStatus).isEqualTo("INACTIVE");

        Patient another = createPatient(tenant.getId());
        assertThat(patientRepository.deleteById(tenant.getId(), another.getId())).isTrue();
        assertThat(patientRepository.findById(tenant.getId(), another.getId())).get().extracting(Patient::getStatus).isEqualTo("INACTIVE");
    }

    @Test
    void patientExternalIdCrud() {
        Tenant tenant = createTenant();
        Patient patient = createPatient(tenant.getId());

        PatientExternalId patientExternalId = new PatientExternalId();
        patientExternalId.setTenantId(tenant.getId());
        patientExternalId.setPatientId(patient.getId());
        patientExternalId.setIdType("EMPI");
        patientExternalId.setIdValue("ID_" + unique("value"));
        patientExternalId.setSourceOrg("HIS");
        patientExternalId.setIsPrimary(true);
        patientExternalIdRepository.save(patientExternalId);

        assertThat(patientExternalId.getId()).isNotNull();
        assertThat(patientExternalIdRepository.findById(tenant.getId(), patientExternalId.getId())).isPresent();
        assertThat(patientExternalIdRepository.findByIdTypeAndIdValue(tenant.getId(), patientExternalId.getIdType(), patientExternalId.getIdValue())).isPresent();
        assertThat(patientExternalIdRepository.listByPatientId(tenant.getId(), patient.getId())).extracting(PatientExternalId::getId).contains(patientExternalId.getId());
        assertThat(patientExternalIdRepository.listByTenantId(tenant.getId())).extracting(PatientExternalId::getId).contains(patientExternalId.getId());

        patientExternalId.setSourceOrg("EMR");
        patientExternalId.setIsPrimary(false);
        patientExternalIdRepository.update(patientExternalId);
        assertThat(patientExternalIdRepository.findById(tenant.getId(), patientExternalId.getId())).get()
                .satisfies(value -> {
                    assertThat(value.getSourceOrg()).isEqualTo("EMR");
                    assertThat(value.getIsPrimary()).isFalse();
                });

        assertThat(patientExternalIdRepository.deleteById(tenant.getId(), patientExternalId.getId())).isTrue();
        assertThat(patientExternalIdRepository.findById(tenant.getId(), patientExternalId.getId())).isEmpty();
    }

    @Test
    void tocUserCrud() {
        Tenant tenant = createTenant();
        TocUser tocUser = createTocUser(tenant.getId());

        assertThat(tocUserRepository.findById(tenant.getId(), tocUser.getId())).isPresent();
        assertThat(tocUserRepository.findByTocUid(tenant.getId(), tocUser.getTocUid())).isPresent();
        assertThat(tocUserRepository.listByTenantId(tenant.getId())).extracting(TocUser::getId).contains(tocUser.getId());

        tocUser.setNickname("Nickname Updated");
        tocUserRepository.update(tocUser);
        assertThat(tocUserRepository.findById(tenant.getId(), tocUser.getId())).get().extracting(TocUser::getNickname).isEqualTo("Nickname Updated");

        assertThat(tocUserRepository.deleteById(tenant.getId(), tocUser.getId())).isTrue();
        assertThat(tocUserRepository.findById(tenant.getId(), tocUser.getId())).get().extracting(TocUser::getStatus).isEqualTo("INACTIVE");
    }

    @Test
    void guardianRelationCrud() {
        Tenant tenant = createTenant();
        Patient patient = createPatient(tenant.getId());

        GuardianRelation guardianRelation = new GuardianRelation();
        guardianRelation.setTenantId(tenant.getId());
        guardianRelation.setPatientId(patient.getId());
        guardianRelation.setGuardianName("Guardian");
        guardianRelation.setGuardianMobileEncrypted("13800000000");
        guardianRelation.setGuardianIdNoEncrypted("IDNO");
        guardianRelation.setRelationType("FATHER");
        guardianRelation.setIsPrimary(true);
        guardianRelation.setStatus("ACTIVE");
        guardianRelationRepository.save(guardianRelation);

        assertThat(guardianRelation.getId()).isNotNull();
        assertThat(guardianRelationRepository.findById(tenant.getId(), guardianRelation.getId())).isPresent();
        assertThat(guardianRelationRepository.findPrimaryByPatientId(tenant.getId(), patient.getId())).isPresent();
        assertThat(guardianRelationRepository.listByPatientId(tenant.getId(), patient.getId())).extracting(GuardianRelation::getId).contains(guardianRelation.getId());
        assertThat(guardianRelationRepository.listByTenantId(tenant.getId())).extracting(GuardianRelation::getId).contains(guardianRelation.getId());

        guardianRelation.setRelationType("MOTHER");
        guardianRelationRepository.update(guardianRelation);
        assertThat(guardianRelationRepository.findById(tenant.getId(), guardianRelation.getId())).get().extracting(GuardianRelation::getRelationType).isEqualTo("MOTHER");

        assertThat(guardianRelationRepository.deleteById(tenant.getId(), guardianRelation.getId())).isTrue();
        assertThat(guardianRelationRepository.findById(tenant.getId(), guardianRelation.getId())).get().extracting(GuardianRelation::getStatus).isEqualTo("INACTIVE");
    }

    @Test
    void consentRecordCrud() {
        Tenant tenant = createTenant();
        Patient patient = createPatient(tenant.getId());
        TocUser tocUser = createTocUser(tenant.getId());

        ConsentRecord consentRecord = new ConsentRecord();
        consentRecord.setTenantId(tenant.getId());
        consentRecord.setPatientId(patient.getId());
        consentRecord.setTocUserId(tocUser.getId());
        consentRecord.setConsentType("CLINICAL");
        consentRecord.setConsentVersion("v1");
        consentRecord.setSignedOffline(true);
        consentRecord.setSignedAt(OffsetDateTime.now().withNano(0));
        consentRecord.setStatus("VALID");
        consentRecord.setRemark("initial");
        consentRecordRepository.save(consentRecord);

        assertThat(consentRecord.getId()).isNotNull();
        assertThat(consentRecordRepository.findById(tenant.getId(), consentRecord.getId())).isPresent();
        assertThat(consentRecordRepository.listByTenantId(tenant.getId())).extracting(ConsentRecord::getId).contains(consentRecord.getId());
        assertThat(consentRecordRepository.listByPatientId(tenant.getId(), patient.getId())).extracting(ConsentRecord::getId).contains(consentRecord.getId());
        assertThat(consentRecordRepository.listByTocUserId(tenant.getId(), tocUser.getId())).extracting(ConsentRecord::getId).contains(consentRecord.getId());

        consentRecord.setRemark("updated");
        consentRecordRepository.update(consentRecord);
        assertThat(consentRecordRepository.findById(tenant.getId(), consentRecord.getId())).get().extracting(ConsentRecord::getRemark).isEqualTo("updated");

        assertThat(consentRecordRepository.deleteById(tenant.getId(), consentRecord.getId())).isTrue();
        assertThat(consentRecordRepository.findById(tenant.getId(), consentRecord.getId())).get().extracting(ConsentRecord::getStatus).isEqualTo("REVOKED");
    }

    @Test
    void encounterCrud() {
        Tenant tenant = createTenant();
        Patient patient = createPatient(tenant.getId());
        UserAccount userAccount = createUserAccount(tenant.getId());

        Encounter encounter = new Encounter();
        encounter.setTenantId(tenant.getId());
        encounter.setPatientId(patient.getId());
        encounter.setEncounterNo("ENC_" + unique("no"));
        encounter.setEncounterType("OUTPATIENT");
        encounter.setDeptName("Dept-A");
        encounter.setAttendingDoctorId(userAccount.getId());
        encounter.setStartAt(OffsetDateTime.now().withNano(0));
        encounter.setEndAt(OffsetDateTime.now().plusHours(1).withNano(0));
        encounter.setSourceSystem("HIS");
        encounterRepository.save(encounter);

        assertThat(encounter.getId()).isNotNull();
        assertThat(encounterRepository.findById(tenant.getId(), encounter.getId())).isPresent();
        assertThat(encounterRepository.findByEncounterNo(tenant.getId(), encounter.getEncounterNo())).isPresent();
        assertThat(encounterRepository.listByTenantId(tenant.getId())).extracting(Encounter::getId).contains(encounter.getId());
        assertThat(encounterRepository.listByPatientId(tenant.getId(), patient.getId())).extracting(Encounter::getId).contains(encounter.getId());

        encounter.setDeptName("Dept-B");
        encounterRepository.update(encounter);
        assertThat(encounterRepository.findById(tenant.getId(), encounter.getId())).get().extracting(Encounter::getDeptName).isEqualTo("Dept-B");

        assertThat(encounterRepository.deleteById(tenant.getId(), encounter.getId())).isTrue();
        assertThat(encounterRepository.findById(tenant.getId(), encounter.getId())).isEmpty();
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
        userAccount.setDeptName("Dept");
        userAccount.setMobileEncrypted("13800001111");
        userAccount.setEmail(suffix + "@mail.com");
        userAccount.setStatus("ACTIVE");
        userAccount.setLastLoginAt(OffsetDateTime.now().withNano(0));
        userAccountRepository.save(userAccount);
        return userAccount;
    }

    private Role createRole(Long tenantId) {
        String suffix = unique("role");
        Role role = new Role();
        role.setTenantId(tenantId);
        role.setRoleCode("ROLE_" + suffix);
        role.setRoleName("Role " + suffix);
        role.setDescription("role description");
        role.setStatus("ACTIVE");
        roleRepository.save(role);
        return role;
    }

    private Patient createPatient(Long tenantId) {
        String suffix = unique("patient");
        Patient patient = new Patient();
        patient.setTenantId(tenantId);
        patient.setPatientNo("PAT_" + suffix);
        patient.setPatientName("Patient " + suffix);
        patient.setGender("MALE");
        patient.setBirthDate(LocalDate.of(2000, 1, 1));
        patient.setIdNoEncrypted("idNo");
        patient.setMobileEncrypted("13800002222");
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
        tocUser.setNickname("Nick " + suffix);
        tocUser.setMobileEncrypted("13900003333");
        tocUser.setOpenid("openid_" + suffix);
        tocUser.setUnionid("unionid_" + suffix);
        tocUser.setVipStatus("NORMAL");
        tocUser.setStatus("ACTIVE");
        tocUserRepository.save(tocUser);
        return tocUser;
    }

    private String unique(String prefix) {
        return prefix + "_" + System.nanoTime();
    }
}
