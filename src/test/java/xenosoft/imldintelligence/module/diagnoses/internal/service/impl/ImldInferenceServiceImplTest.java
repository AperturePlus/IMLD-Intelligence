package xenosoft.imldintelligence.module.diagnoses.internal.service.impl;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;

import com.fasterxml.jackson.databind.ObjectMapper;

import xenosoft.imldintelligence.module.diagnoses.api.dto.ImldInferenceApiDtos;
import xenosoft.imldintelligence.module.diagnoses.internal.config.ImldInferenceProperties;
import xenosoft.imldintelligence.module.diagnoses.internal.service.ImldInferenceService;

class ImldInferenceServiceImplTest {

    private ImldInferenceService inferenceService;

    @BeforeEach
    void setUp() {
        ImldInferenceProperties properties = new ImldInferenceProperties();
        properties.setDesensitizedClinicalEnabled(true);
        inferenceService = new ImldInferenceServiceImpl(properties, new ObjectMapper(), new DefaultResourceLoader());
    }

    @Test
    void healthShouldExposeLoadedModelState() {
        ImldInferenceApiDtos.Response.HealthResponse response = inferenceService.health();
        assertThat(response.service()).isEqualTo("running");
        assertThat(response.modelLoaded()).isTrue();
        assertThat(response.modelVersion()).isNotBlank();
    }

    @Test
    void predictShouldReturnStructuredInferenceData() {
        ImldInferenceApiDtos.Request.ImldPredictRequest request = buildRequest("P001");
        ImldInferenceApiDtos.Response.PredictData response = inferenceService.predict(request);

        assertThat(response.patientId()).isEqualTo("P001");
        assertThat(response.riskProbability()).isBetween(0.0, 1.0);
        assertThat(response.riskLevel()).isNotBlank();
        assertThat(response.dataHash()).hasSize(64);
        assertThat(response.suggestions()).isNotEmpty();
        assertThat(response.geneAbnormalities()).isNotEmpty();
    }

    @Test
    void predictShouldAcceptV4ClinicalFeaturesAndFallbackToMetadataMedians() {
        ImldInferenceApiDtos.Request.ImldPredictRequest request = new ImldInferenceApiDtos.Request.ImldPredictRequest(
                52,
                1,
                90.0,
                32.0,
                260.0,
                0,
                null,
                List.of(),
                Map.of(
                        "ALT(U/L)", 90.0,
                        "TBIL(μmol/L)", 32.0,
                        "Smoking", 1.0,
                        "Drinking", 1.0
                ),
                "P-V4"
        );

        ImldInferenceApiDtos.Response.PredictData response = inferenceService.predict(request);

        assertThat(response.patientId()).isEqualTo("P-V4");
        assertThat(response.riskProbability()).isBetween(0.0, 1.0);
        assertThat(response.modelMetrics()).containsKey("auc");
        assertThat(response.dataHash()).hasSize(64);
    }

    @Test
    void batchPredictShouldReturnEachSampleResult() {
        List<ImldInferenceApiDtos.Request.ImldPredictRequest> requests = List.of(
                buildRequest("P001"),
                buildRequest("P002")
        );

        ImldInferenceApiDtos.Response.BatchPredictData response = inferenceService.batchPredict(requests);

        assertThat(response.batchCount()).isEqualTo(2);
        assertThat(response.batchResults()).hasSize(2);
        assertThat(response.batchResults().get(0).sampleIndex()).isEqualTo(1);
        assertThat(response.batchResults().get(1).sampleIndex()).isEqualTo(2);
        assertThat(response.batchResults()).allSatisfy(item -> {
            assertThat(item.riskProbability()).isBetween(0.0, 1.0);
            assertThat(item.dataHash()).hasSize(64);
        });
    }

    private ImldInferenceApiDtos.Request.ImldPredictRequest buildRequest(String patientId) {
        ImldInferenceApiDtos.Request.GeneVariant variant = new ImldInferenceApiDtos.Request.GeneVariant(
                "ATP7B",
                "c.2333G>T",
                "p.Arg778Leu",
                "missense",
                "compound_heterozygous",
                "pathogenic",
                0.0001
        );
        return new ImldInferenceApiDtos.Request.ImldPredictRequest(
                16,
                1,
                86.0,
                28.5,
                120.0,
                1,
                null,
                List.of(variant),
                null,
                patientId
        );
    }
}
