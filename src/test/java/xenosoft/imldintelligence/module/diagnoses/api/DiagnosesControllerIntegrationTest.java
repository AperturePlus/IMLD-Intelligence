package xenosoft.imldintelligence.module.diagnoses.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.web.servlet.MockMvc;
import xenosoft.imldintelligence.AbstractPostgresIntegrationTest;
import xenosoft.imldintelligence.module.identity.internal.model.Tenant;
import xenosoft.imldintelligence.module.identity.internal.model.UserSubject;
import xenosoft.imldintelligence.module.identity.internal.repository.TenantRepository;
import xenosoft.imldintelligence.module.identity.internal.util.JwtUtil;

import java.util.Set;

import static org.hamcrest.Matchers.empty;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisabledInAotMode
@TestPropertySource(properties = {
        "imld.security.enabled=true",
        "imld.security.public-paths[0]=/error",
        "imld.security.jwt.issuer=imld-test",
        "imld.security.jwt.secret=01234567890123456789012345678901",
        "imld.security.jwt.access-token-ttl=15m",
        "imld.security.jwt.refresh-token-ttl=7d",
        "imld.security.jwt.clock-skew=0s"
})
class DiagnosesControllerIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private Long tenantId;
    private String doctorToken;

    @BeforeEach
    void setUp() {
        Tenant tenant = new Tenant();
        tenant.setTenantCode("DIAGNOSES_EMPTY_" + System.nanoTime());
        tenant.setTenantName("Diagnoses Empty Test");
        tenant.setDeployMode("SAAS");
        tenant.setStatus("ACTIVE");
        tenantRepository.save(tenant);
        this.tenantId = tenant.getId();

        doctorToken = jwtUtil.generateAccessToken(
                new UserSubject(1L, tenantId, "DOCTOR", "ICU", Set.of("DOCTOR")));
    }

    @Test
    void listSessionsReturnsEmptyPageWhenTenantHasNoDiagnosisData() throws Exception {
        mockMvc.perform(get("/api/v1/web/diagnoses/sessions")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + doctorToken)
                        .header("X-Tenant-Id", tenantId)
                        .param("page", "0")
                        .param("size", "200"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.size").value(200))
                .andExpect(jsonPath("$.data.total").value(0))
                .andExpect(jsonPath("$.data.items", empty()));
    }

    @Test
    void listSessionsRejectsMissingTenantHeader() throws Exception {
        mockMvc.perform(get("/api/v1/web/diagnoses/sessions")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + doctorToken)
                        .param("page", "0")
                        .param("size", "200"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void listSessionsRejectsMissingToken() throws Exception {
        mockMvc.perform(get("/api/v1/web/diagnoses/sessions")
                        .header("X-Tenant-Id", tenantId)
                        .param("page", "0")
                        .param("size", "200"))
                .andExpect(status().isUnauthorized());
    }
}
