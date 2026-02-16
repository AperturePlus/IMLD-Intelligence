package xenosoft.imldintelligence.module.integration.internal.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.transaction.annotation.Transactional;
import xenosoft.imldintelligence.AbstractPostgresIntegrationTest;
import xenosoft.imldintelligence.module.identity.internal.model.Tenant;
import xenosoft.imldintelligence.module.identity.internal.repository.TenantRepository;
import xenosoft.imldintelligence.module.integration.internal.model.IntegrationJob;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisabledInAotMode
class IntegrationRepositoryIntegrationTest extends AbstractPostgresIntegrationTest {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private IntegrationJobRepository integrationJobRepository;

    @Test
    void integrationJobCrudWithJsonbAndTenantIsolation() {
        Tenant tenantA = createTenant();
        Tenant tenantB = createTenant();

        IntegrationJob job = new IntegrationJob();
        job.setTenantId(tenantA.getId());
        job.setJobNo("JOB_" + unique("no"));
        job.setSourceSystem("HIS");
        job.setDirection("OUTBOUND");
        job.setBizType("PATIENT");
        job.setRequestPayload(OBJECT_MAPPER.createObjectNode().put("req", "x"));
        job.setResponsePayload(OBJECT_MAPPER.createObjectNode().put("resp", "y"));
        job.setStatus("RUNNING");
        job.setStartedAt(OffsetDateTime.now().withNano(0));
        integrationJobRepository.save(job);

        assertThat(job.getId()).isNotNull();
        assertThat(integrationJobRepository.findById(tenantA.getId(), job.getId())).isPresent();
        assertThat(integrationJobRepository.findById(tenantB.getId(), job.getId())).isEmpty();
        assertThat(integrationJobRepository.findByJobNo(tenantA.getId(), job.getJobNo())).isPresent();
        assertThat(integrationJobRepository.listByTenantId(tenantA.getId())).extracting(IntegrationJob::getId).contains(job.getId());
        assertThat(integrationJobRepository.listBySourceSystem(tenantA.getId(), "HIS")).extracting(IntegrationJob::getId).contains(job.getId());

        job.setStatus("SUCCESS");
        job.setResponsePayload(OBJECT_MAPPER.createObjectNode().put("resp", "ok"));
        job.setFinishedAt(OffsetDateTime.now().withNano(0));
        integrationJobRepository.update(job);
        assertThat(integrationJobRepository.findById(tenantA.getId(), job.getId())).get()
                .satisfies(value -> {
                    assertThat(value.getStatus()).isEqualTo("SUCCESS");
                    assertThat(value.getResponsePayload().get("resp").asText()).isEqualTo("ok");
                });

        assertThat(integrationJobRepository.deleteById(tenantA.getId(), job.getId())).isTrue();
        assertThat(integrationJobRepository.findById(tenantA.getId(), job.getId())).isEmpty();
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

