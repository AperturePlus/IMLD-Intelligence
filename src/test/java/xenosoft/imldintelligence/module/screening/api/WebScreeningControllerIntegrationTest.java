package xenosoft.imldintelligence.module.screening.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.web.servlet.MockMvc;
import xenosoft.imldintelligence.AbstractPostgresIntegrationTest;
import xenosoft.imldintelligence.module.identity.internal.model.Tenant;
import xenosoft.imldintelligence.module.identity.internal.repository.TenantRepository;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisabledInAotMode
class WebScreeningControllerIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TenantRepository tenantRepository;

    private Long tenantId;

    @BeforeEach
    void setUp() {
        Tenant tenant = new Tenant();
        tenant.setTenantCode("WEB_SCREENING_" + System.nanoTime());
        tenant.setTenantName("Web Screening Test");
        tenant.setDeployMode("SAAS");
        tenant.setStatus("ACTIVE");
        tenantRepository.save(tenant);
        this.tenantId = tenant.getId();
    }

    @Test
    void overviewTreatsBlankDateRangeAsEmptyFilterAndReturnsDefaultPayload() throws Exception {
        mockMvc.perform(get("/api/v1/web/screening/overview/")
                        .header("X-Tenant-Id", tenantId)
                        .param("from", "")
                        .param("to", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.updatedAt").isString())
                .andExpect(jsonPath("$.data.statCards", hasSize(4)))
                .andExpect(jsonPath("$.data.statCards[0].title").value("累计筛查总人数"))
                .andExpect(jsonPath("$.data.statCards[0].value").value(0))
                .andExpect(jsonPath("$.data.statCards[0].trend").value(0))
                .andExpect(jsonPath("$.data.riskDistribution", empty()))
                .andExpect(jsonPath("$.data.topGenes", empty()))
                .andExpect(jsonPath("$.data.aiEfficiency.diagnosisMatchRate").value(0))
                .andExpect(jsonPath("$.data.aiEfficiency.missRate").value("0%"))
                .andExpect(jsonPath("$.data.aiEfficiency.avgDuration").value("--"))
                .andExpect(jsonPath("$.data.highRiskPatients", empty()));
    }

    @Test
    void overviewSupportsPathWithoutTrailingSlash() throws Exception {
        mockMvc.perform(get("/api/v1/web/screening/overview")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.statCards", hasSize(4)));
    }

    @Test
    void overviewRejectsInvalidDateInsteadOfReturningEmptyData() throws Exception {
        mockMvc.perform(get("/api/v1/web/screening/overview")
                        .header("X-Tenant-Id", tenantId)
                        .param("from", "invalid")
                        .param("to", ""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void overviewRejectsMissingTenantHeader() throws Exception {
        mockMvc.perform(get("/api/v1/web/screening/overview")
                        .param("from", "")
                        .param("to", ""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }
}
