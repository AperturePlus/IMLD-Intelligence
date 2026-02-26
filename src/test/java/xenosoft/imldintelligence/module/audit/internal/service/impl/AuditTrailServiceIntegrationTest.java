package xenosoft.imldintelligence.module.audit.internal.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import xenosoft.imldintelligence.AbstractPostgresIntegrationTest;
import xenosoft.imldintelligence.module.audit.internal.aop.AuditedOperation;
import xenosoft.imldintelligence.module.audit.internal.aop.SensitiveAccessed;
import xenosoft.imldintelligence.module.audit.internal.context.AuditContext;
import xenosoft.imldintelligence.module.audit.internal.context.AuditContextHolder;
import xenosoft.imldintelligence.module.audit.internal.repository.AuditLogRepository;
import xenosoft.imldintelligence.module.audit.internal.repository.SensitiveDataAccessLogRepository;
import xenosoft.imldintelligence.module.audit.internal.repository.query.AuditLogQuery;
import xenosoft.imldintelligence.module.audit.internal.repository.query.SensitiveDataAccessLogQuery;
import xenosoft.imldintelligence.module.audit.internal.service.AuditTrailService;
import xenosoft.imldintelligence.module.audit.internal.service.command.AuditRecordCommand;
import xenosoft.imldintelligence.module.audit.internal.service.exception.AuditPersistenceException;
import xenosoft.imldintelligence.module.identity.internal.model.Tenant;
import xenosoft.imldintelligence.module.identity.internal.model.UserAccount;
import xenosoft.imldintelligence.module.identity.internal.repository.TenantRepository;
import xenosoft.imldintelligence.module.identity.internal.repository.UserAccountRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisabledInAotMode
class AuditTrailServiceIntegrationTest extends AbstractPostgresIntegrationTest {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private AuditTrailService auditTrailService;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private SensitiveDataAccessLogRepository sensitiveDataAccessLogRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private AuditTestOperations auditTestOperations;

    @AfterEach
    void clearContext() {
        AuditContextHolder.clear();
    }

    @Test
    void shouldAutofillContextAndSanitizePayload() {
        Tenant tenant = createTenant();
        UserAccount user = createUserAccount(tenant.getId());

        AuditContext context = new AuditContext();
        context.setTenantId(tenant.getId());
        context.setUserId(user.getId());
        context.setUserRole("SYSTEM_ADMIN");
        context.setTraceId("trace-autofill");
        context.setIpAddress("10.0.0.10");
        context.setDeviceInfo("Desktop");
        context.setAppVersion("1.0.0");
        AuditContextHolder.set(context);

        AuditRecordCommand command = new AuditRecordCommand();
        command.setAction("UPDATE");
        command.setResourceType("PATIENT");
        command.setResourceId("PAT-001");
        command.setBeforeData(OBJECT_MAPPER.createObjectNode().put("id_card", "330123199001011234"));
        command.setAfterData(OBJECT_MAPPER.createObjectNode().put("patientName", "Alice"));

        auditTrailService.recordAudit(command);

        AuditLogQuery query = new AuditLogQuery();
        query.setTenantId(tenant.getId());
        query.setTraceId("trace-autofill");

        assertThat(auditLogRepository.query(query, 0, 10))
                .singleElement()
                .satisfies(value -> {
                    assertThat(value.getUserId()).isEqualTo(user.getId());
                    assertThat(value.getUserRole()).isEqualTo("SYSTEM_ADMIN");
                    assertThat(value.getIpAddress()).isEqualTo("10.0.0.10");
                    assertThat(value.getBeforeData().get("id_card").asText()).isEqualTo("******");
                    assertThat(value.getAfterData().get("patientName").asText()).isEqualTo("******");
                });
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void shouldFailClosedAndRollbackBusinessTransactionWhenAuditPersistenceFails() {
        Tenant tenant = createTenant();
        String username = "rollback_" + unique("user");

        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        assertThatThrownBy(() -> transactionTemplate.executeWithoutResult(status -> {
            UserAccount user = new UserAccount();
            user.setTenantId(tenant.getId());
            user.setUserNo("UNO_" + unique("userNo"));
            user.setUsername(username);
            user.setPasswordHash("pwd_hash");
            user.setDisplayName("Rollback User");
            user.setUserType("ADMIN");
            user.setStatus("ACTIVE");
            userAccountRepository.save(user);

            AuditRecordCommand command = new AuditRecordCommand();
            command.setTenantId(999999999L);
            command.setAction("UPDATE");
            command.setResourceType("PATIENT");
            command.setResourceId("PAT-ROLLBACK");
            auditTrailService.recordAudit(command);
        }))
                .isInstanceOf(AuditPersistenceException.class);

        assertThat(userAccountRepository.findByUsername(tenant.getId(), username)).isEmpty();
    }

    @Test
    void shouldRecordAopEventsForSuccessAndFailurePaths() {
        Tenant tenant = createTenant();
        UserAccount user = createUserAccount(tenant.getId());

        AuditContext context = new AuditContext();
        context.setTenantId(tenant.getId());
        context.setUserId(user.getId());
        context.setUserRole("SYSTEM_ADMIN");
        context.setTraceId("trace-aop");
        context.setIpAddress("10.0.0.11");
        AuditContextHolder.set(context);

        assertThat(auditTestOperations.auditSuccess("RES-SUCCESS")).isEqualTo("ok");
        assertThatThrownBy(() -> auditTestOperations.auditFailure("RES-FAIL"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("boom");

        assertThat(auditTestOperations.sensitiveSuccess("PAT-OK", "FOLLOWUP")).isEqualTo("done");
        assertThatThrownBy(() -> auditTestOperations.sensitiveFailure("PAT-DENY", "NO_PERMISSION"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("denied");

        AuditLogQuery auditQuery = new AuditLogQuery();
        auditQuery.setTenantId(tenant.getId());
        auditQuery.setAction("UPDATE");
        assertThat(auditLogRepository.count(auditQuery)).isEqualTo(2L);
        assertThat(auditLogRepository.query(auditQuery, 0, 10))
                .extracting("resourceId")
                .contains("RES-SUCCESS", "RES-FAIL");

        SensitiveDataAccessLogQuery sensitiveQuery = new SensitiveDataAccessLogQuery();
        sensitiveQuery.setTenantId(tenant.getId());
        sensitiveQuery.setSensitiveType("GENETIC");
        assertThat(sensitiveDataAccessLogRepository.count(sensitiveQuery)).isEqualTo(2L);
        assertThat(sensitiveDataAccessLogRepository.query(sensitiveQuery, 0, 10))
                .extracting("accessResult")
                .contains("ALLOW", "DENY");
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

    @TestConfiguration
    static class TestConfig {
        @Bean
        AuditTestOperations auditTestOperations() {
            return new AuditTestOperations();
        }
    }

    static class AuditTestOperations {
        @AuditedOperation(action = "UPDATE", resourceType = "REPORT", resourceIdExpression = "#p0", successOnly = false)
        public String auditSuccess(String resourceId) {
            return "ok";
        }

        @AuditedOperation(action = "UPDATE", resourceType = "REPORT", resourceIdExpression = "#p0", successOnly = false)
        public String auditFailure(String resourceId) {
            throw new IllegalStateException("boom");
        }

        @SensitiveAccessed(sensitiveType = "GENETIC", resourceType = "PATIENT", resourceIdExpression = "#p0", reasonExpression = "#p1")
        public String sensitiveSuccess(String resourceId, String reason) {
            return "done";
        }

        @SensitiveAccessed(sensitiveType = "GENETIC", resourceType = "PATIENT", resourceIdExpression = "#p0", reasonExpression = "#p1")
        public String sensitiveFailure(String resourceId, String reason) {
            throw new IllegalArgumentException("denied");
        }
    }
}
