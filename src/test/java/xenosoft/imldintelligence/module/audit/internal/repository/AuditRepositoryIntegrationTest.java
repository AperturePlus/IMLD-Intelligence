package xenosoft.imldintelligence.module.audit.internal.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.transaction.annotation.Transactional;
import xenosoft.imldintelligence.AbstractPostgresIntegrationTest;
import xenosoft.imldintelligence.module.audit.internal.repository.query.AuditLogQuery;
import xenosoft.imldintelligence.module.audit.internal.repository.query.ModelInvocationLogQuery;
import xenosoft.imldintelligence.module.audit.internal.repository.query.SensitiveDataAccessLogQuery;
import xenosoft.imldintelligence.module.identity.internal.model.Tenant;
import xenosoft.imldintelligence.module.identity.internal.model.UserAccount;
import xenosoft.imldintelligence.module.identity.internal.repository.TenantRepository;
import xenosoft.imldintelligence.module.identity.internal.repository.UserAccountRepository;
import xenosoft.imldintelligence.common.model.AuditLog;
import xenosoft.imldintelligence.common.model.ModelInvocationLog;
import xenosoft.imldintelligence.common.model.SensitiveDataAccessLog;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisabledInAotMode
class AuditRepositoryIntegrationTest extends AbstractPostgresIntegrationTest {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private SensitiveDataAccessLogRepository sensitiveDataAccessLogRepository;

    @Autowired
    private ModelInvocationLogRepository modelInvocationLogRepository;

    @Test
    void shouldSaveAndQueryAllAuditRepositoriesWithTenantIsolation() {
        Tenant tenantA = createTenant();
        Tenant tenantB = createTenant();
        UserAccount userA = createUserAccount(tenantA.getId());

        OffsetDateTime from = OffsetDateTime.now().minusMinutes(5);

        AuditLog auditLogA = new AuditLog();
        auditLogA.setTenantId(tenantA.getId());
        auditLogA.setUserId(userA.getId());
        auditLogA.setUserRole("SYSTEM_ADMIN");
        auditLogA.setAction("UPDATE");
        auditLogA.setResourceType("PATIENT");
        auditLogA.setResourceId("PAT_A");
        auditLogA.setBeforeData(OBJECT_MAPPER.createObjectNode().put("name", "old"));
        auditLogA.setAfterData(OBJECT_MAPPER.createObjectNode().put("name", "new"));
        auditLogA.setIpAddress("10.0.0.1");
        auditLogA.setTraceId("trace-a");
        auditLogRepository.save(auditLogA);

        AuditLog auditLogB = new AuditLog();
        auditLogB.setTenantId(tenantB.getId());
        auditLogB.setAction("UPDATE");
        auditLogB.setResourceType("PATIENT");
        auditLogB.setResourceId("PAT_B");
        auditLogB.setTraceId("trace-b");
        auditLogRepository.save(auditLogB);

        AuditLogQuery auditQuery = new AuditLogQuery();
        auditQuery.setTenantId(tenantA.getId());
        auditQuery.setAction("UPDATE");
        auditQuery.setResourceType("PATIENT");
        auditQuery.setTraceId("trace-a");
        auditQuery.setFrom(from);

        assertThat(auditLogRepository.count(auditQuery)).isEqualTo(1L);
        assertThat(auditLogRepository.query(auditQuery, 0, 10))
                .singleElement()
                .satisfies(value -> {
                    assertThat(value.getTenantId()).isEqualTo(tenantA.getId());
                    assertThat(value.getResourceId()).isEqualTo("PAT_A");
                    assertThat(value.getAfterData().get("name").asText()).isEqualTo("new");
                });

        SensitiveDataAccessLog sensitiveA = new SensitiveDataAccessLog();
        sensitiveA.setTenantId(tenantA.getId());
        sensitiveA.setUserId(userA.getId());
        sensitiveA.setSensitiveType("GENETIC");
        sensitiveA.setResourceType("PATIENT");
        sensitiveA.setResourceId("PAT_A");
        sensitiveA.setAccessReason("FOLLOWUP");
        sensitiveA.setAccessResult("ALLOW");
        sensitiveA.setIpAddress("10.0.0.2");
        sensitiveDataAccessLogRepository.save(sensitiveA);

        SensitiveDataAccessLog sensitiveB = new SensitiveDataAccessLog();
        sensitiveB.setTenantId(tenantB.getId());
        sensitiveB.setSensitiveType("GENETIC");
        sensitiveB.setResourceType("PATIENT");
        sensitiveB.setResourceId("PAT_B");
        sensitiveB.setAccessResult("DENY");
        sensitiveDataAccessLogRepository.save(sensitiveB);

        SensitiveDataAccessLogQuery sensitiveQuery = new SensitiveDataAccessLogQuery();
        sensitiveQuery.setTenantId(tenantA.getId());
        sensitiveQuery.setSensitiveType("GENETIC");
        sensitiveQuery.setAccessResult("ALLOW");
        sensitiveQuery.setFrom(from);

        assertThat(sensitiveDataAccessLogRepository.count(sensitiveQuery)).isEqualTo(1L);
        assertThat(sensitiveDataAccessLogRepository.query(sensitiveQuery, 0, 10))
                .singleElement()
                .satisfies(value -> {
                    assertThat(value.getTenantId()).isEqualTo(tenantA.getId());
                    assertThat(value.getAccessReason()).isEqualTo("FOLLOWUP");
                });

        ModelInvocationLog invocationA = new ModelInvocationLog();
        invocationA.setTenantId(tenantA.getId());
        invocationA.setProvider("LOCAL");
        invocationA.setRequestId("REQ_A");
        invocationA.setInputDigest("in_a");
        invocationA.setOutputDigest("out_a");
        invocationA.setLatencyMs(123);
        invocationA.setTokenIn(11);
        invocationA.setTokenOut(22);
        invocationA.setCostAmount(BigDecimal.valueOf(0.1234));
        invocationA.setStatus("SUCCESS");
        modelInvocationLogRepository.save(invocationA);

        ModelInvocationLog invocationB = new ModelInvocationLog();
        invocationB.setTenantId(tenantB.getId());
        invocationB.setProvider("LOCAL");
        invocationB.setRequestId("REQ_B");
        invocationB.setStatus("FAILED");
        modelInvocationLogRepository.save(invocationB);

        ModelInvocationLogQuery invocationQuery = new ModelInvocationLogQuery();
        invocationQuery.setTenantId(tenantA.getId());
        invocationQuery.setProvider("LOCAL");
        invocationQuery.setRequestId("REQ_A");
        invocationQuery.setStatus("SUCCESS");
        invocationQuery.setFrom(from);

        assertThat(modelInvocationLogRepository.count(invocationQuery)).isEqualTo(1L);
        assertThat(modelInvocationLogRepository.query(invocationQuery, 0, 10))
                .singleElement()
                .satisfies(value -> {
                    assertThat(value.getTenantId()).isEqualTo(tenantA.getId());
                    assertThat(value.getLatencyMs()).isEqualTo(123);
                    assertThat(value.getCostAmount()).isEqualByComparingTo("0.1234");
                });
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
        userAccount.setUserType("ADMIN");
        userAccount.setStatus("ACTIVE");
        userAccountRepository.save(userAccount);
        return userAccount;
    }

    private String unique(String prefix) {
        return prefix + "_" + System.nanoTime();
    }
}
