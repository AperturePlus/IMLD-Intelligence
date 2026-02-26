package xenosoft.imldintelligence.module.audit.internal.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.MockMvc;
import xenosoft.imldintelligence.AbstractPostgresIntegrationTest;
import xenosoft.imldintelligence.module.audit.internal.service.AuditTrailService;
import xenosoft.imldintelligence.module.audit.internal.service.command.AuditRecordCommand;
import xenosoft.imldintelligence.module.identity.internal.model.Tenant;
import xenosoft.imldintelligence.module.identity.internal.repository.TenantRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisabledInAotMode
class AuditQueryApiIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private AuditTrailService auditTrailService;

    @Test
    void shouldRejectWhenRoleIsNotInAllowlist() throws Exception {
        Tenant tenant = createTenant();
        insertAuditLog(tenant.getId(), "PAT-1", "trace-1");

        mockMvc.perform(get("/api/v1/audit/logs")
                        .header("X-Tenant-Id", tenant.getId())
                        .header("X-User-Role", "DOCTOR"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowQueryForConfiguredRoleAndEnforceTenantIsolation() throws Exception {
        Tenant tenantA = createTenant();
        Tenant tenantB = createTenant();

        insertAuditLog(tenantA.getId(), "PAT-A", "trace-A");
        insertAuditLog(tenantB.getId(), "PAT-B", "trace-B");

        mockMvc.perform(get("/api/v1/audit/logs")
                        .header("X-Tenant-Id", tenantA.getId())
                        .header("X-User-Role", "COMPLIANCE_AUDITOR")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.items[0].tenantId").value(tenantA.getId()))
                .andExpect(jsonPath("$.items[0].resourceId").value("PAT-A"));
    }

    @Test
    void shouldReturnBadRequestWhenTenantHeaderMissing() throws Exception {
        mockMvc.perform(get("/api/v1/audit/logs")
                        .header("X-User-Role", "SYSTEM_ADMIN"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldCapRequestedPageSizeByConfiguredMaxPageSize() throws Exception {
        Tenant tenant = createTenant();
        insertAuditLog(tenant.getId(), "PAT-C", "trace-C");

        mockMvc.perform(get("/api/v1/audit/logs")
                        .header("X-Tenant-Id", tenant.getId())
                        .header("X-User-Role", "SYSTEM_ADMIN")
                        .param("size", "999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(200));
    }

    private void insertAuditLog(Long tenantId, String resourceId, String traceId) {
        AuditRecordCommand command = new AuditRecordCommand();
        command.setTenantId(tenantId);
        command.setAction("UPDATE");
        command.setResourceType("PATIENT");
        command.setResourceId(resourceId);
        command.setTraceId(traceId);
        auditTrailService.recordAudit(command);
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

    private String unique(String prefix) {
        return prefix + "_" + System.nanoTime();
    }
}
