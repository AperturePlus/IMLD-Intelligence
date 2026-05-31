package xenosoft.imldintelligence.module.identity.internal.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.aot.DisabledInAotMode;
import xenosoft.imldintelligence.AbstractPostgresIntegrationTest;
import xenosoft.imldintelligence.module.diagnoses.internal.model.DiagnosisSession;
import xenosoft.imldintelligence.module.diagnoses.internal.repository.DiagnosisSessionRepository;
import xenosoft.imldintelligence.module.identity.internal.model.Patient;
import xenosoft.imldintelligence.module.identity.internal.model.Role;
import xenosoft.imldintelligence.module.identity.internal.model.Tenant;
import xenosoft.imldintelligence.module.identity.internal.model.UserAccount;
import xenosoft.imldintelligence.module.identity.internal.repository.AbacPolicyRepository;
import xenosoft.imldintelligence.module.identity.internal.repository.PatientRepository;
import xenosoft.imldintelligence.module.identity.internal.repository.RoleRepository;
import xenosoft.imldintelligence.module.identity.internal.repository.TenantRepository;
import xenosoft.imldintelligence.module.identity.internal.repository.UserAccountRepository;
import xenosoft.imldintelligence.module.identity.internal.repository.UserRoleRelRepository;

import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles({"test", "dev"})
@DisabledInAotMode
class DevDataSeedRunnerIntegrationTest extends AbstractPostgresIntegrationTest {

    private static final Set<String> EXPECTED_PATIENT_NOS = Set.of(
            "P001", "P002", "P003", "P004", "P005", "P006",
            "P007", "P008", "P009", "P010", "P011", "P012"
    );

    @Autowired
    private DevDataSeedRunner devDataSeedRunner;

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
    private DiagnosisSessionRepository diagnosisSessionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void devSeedCreatesExpectedDataAndIsIdempotent() {
        Tenant tenant = tenantRepository.findByTenantCode(DevDataSeedRunner.TENANT_CODE).orElseThrow();
        Long tenantId = tenant.getId();

        assertSeededData(tenantId);
        long patientCountBefore = seededPatientCount(tenantId);
        long diagnosisCountBefore = seededDiagnosisSessionCount(tenantId);

        devDataSeedRunner.run();

        assertSeededData(tenantId);
        assertThat(seededPatientCount(tenantId)).isEqualTo(patientCountBefore);
        assertThat(seededDiagnosisSessionCount(tenantId)).isEqualTo(diagnosisCountBefore);
    }

    private void assertSeededData(Long tenantId) {
        UserAccount doctor = userAccountRepository.findByUsername(tenantId, DevDataSeedRunner.DOCTOR_USERNAME)
                .orElseThrow();
        assertThat(passwordEncoder.matches(DevDataSeedRunner.DOCTOR_PASSWORD, doctor.getPasswordHash())).isTrue();

        Role doctorRole = roleRepository.findByRoleCode(tenantId, "DOCTOR").orElseThrow();
        assertThat(roleRepository.findByRoleCode(tenantId, "SYSTEM_ADMIN")).isPresent();
        assertThat(roleRepository.findByRoleCode(tenantId, "TOC_USER")).isPresent();
        assertThat(userRoleRelRepository.findByUserIdAndRoleId(tenantId, doctor.getId(), doctorRole.getId())).isPresent();
        assertThat(abacPolicyRepository.findByPolicyCode(tenantId, "DEV_DOCTOR_PATIENT_RW")).isPresent();

        Set<String> seededPatientNos = patientRepository.listByTenantId(tenantId).stream()
                .map(Patient::getPatientNo)
                .filter(EXPECTED_PATIENT_NOS::contains)
                .collect(Collectors.toSet());
        assertThat(seededPatientNos).containsExactlyInAnyOrderElementsOf(EXPECTED_PATIENT_NOS);
        assertThat(seededDiagnosisSessionCount(tenantId)).isEqualTo(3L);
    }

    private long seededPatientCount(Long tenantId) {
        return patientRepository.listByTenantId(tenantId).stream()
                .map(Patient::getPatientNo)
                .filter(EXPECTED_PATIENT_NOS::contains)
                .count();
    }

    private long seededDiagnosisSessionCount(Long tenantId) {
        return diagnosisSessionRepository.listByTenantId(tenantId).stream()
                .map(DiagnosisSession::getInputSnapshot)
                .filter(snapshot -> snapshot != null && snapshot.has("devSeedKey"))
                .map(snapshot -> snapshot.get("devSeedKey").asText())
                .filter(seedKey -> seedKey.startsWith("REP-202311-"))
                .count();
    }
}
