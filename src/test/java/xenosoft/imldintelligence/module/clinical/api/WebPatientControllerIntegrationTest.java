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
import xenosoft.imldintelligence.module.identity.internal.model.Patient;
import xenosoft.imldintelligence.module.identity.internal.model.Tenant;
import xenosoft.imldintelligence.module.identity.internal.repository.PatientRepository;
import xenosoft.imldintelligence.module.identity.internal.repository.TenantRepository;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.hamcrest.Matchers.hasSize;
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
                .andExpect(jsonPath("$.data.items[0].riskLevel").value("中"))
                .andExpect(jsonPath("$.data.items[0].avatar").isString());
    }

    private void savePatient(String patientNo, String name, String gender, int age, String status) {
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
    }
}
