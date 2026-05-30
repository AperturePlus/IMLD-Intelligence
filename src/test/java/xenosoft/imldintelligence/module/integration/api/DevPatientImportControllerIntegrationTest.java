package xenosoft.imldintelligence.module.integration.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.web.servlet.MockMvc;
import xenosoft.imldintelligence.AbstractPostgresIntegrationTest;
import xenosoft.imldintelligence.module.identity.internal.config.DevDataSeedRunner;
import xenosoft.imldintelligence.module.identity.internal.repository.TenantRepository;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"test", "dev"})
@DisabledInAotMode
class DevPatientImportControllerIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TenantRepository tenantRepository;

    private Long tenantId;

    @BeforeEach
    void setUp() {
        this.tenantId = tenantRepository.findByTenantCode(DevDataSeedRunner.TENANT_CODE).orElseThrow().getId();
    }

    @Test
    void hisLisPreviewRejectsMissingPatientNoAndVisitNo() throws Exception {
        mockMvc.perform(post("/api/v1/web/integration/imports/patient/his-lis")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void imageAndPdfPreviewRejectMissingFilePayload() throws Exception {
        mockMvc.perform(post("/api/v1/web/integration/imports/patient/image")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("fileName", "lab.png"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));

        mockMvc.perform(post("/api/v1/web/integration/imports/patient/pdf")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("fileContentBase64", "ZmFrZQ=="))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void hisLisPreviewReturnsFrontendAlignedPreview() throws Exception {
        mockMvc.perform(post("/api/v1/web/integration/imports/patient/his-lis")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("patientNo", "P001"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.sourceType").value("HIS_LIS"))
                .andExpect(jsonPath("$.data.patientNo").value("P001"))
                .andExpect(jsonPath("$.data.name").value("林建国"))
                .andExpect(jsonPath("$.data.gender").value("男"))
                .andExpect(jsonPath("$.data.encounterType").value("OUTPATIENT"));
    }
}
