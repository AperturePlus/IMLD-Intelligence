package xenosoft.imldintelligence.module.diagnoses.internal.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xenosoft.imldintelligence.module.diagnoses.api.dto.ImldInferenceApiDtos;
import xenosoft.imldintelligence.module.diagnoses.internal.config.ImldInferenceProperties;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MockImldInferenceServiceTest {

    private MockImldInferenceService service;

    @BeforeEach
    void setUp() {
        ImldInferenceProperties properties = new ImldInferenceProperties();
        properties.setEngine("mock");
        properties.setLowRiskThreshold(0.2);
        properties.setHighRiskThreshold(0.7);
        service = new MockImldInferenceService(properties);
    }

    @Test
    void predictShouldBeDeterministicForSameInput() {
        ImldInferenceApiDtos.Request.ImldPredictRequest request = buildRequest("P-001");

        ImldInferenceApiDtos.Response.PredictData first = service.predict(request);
        ImldInferenceApiDtos.Response.PredictData second = service.predict(request);

        assertThat(first.riskProbability()).isEqualTo(second.riskProbability());
        assertThat(first.riskLevel()).isEqualTo(second.riskLevel());
        assertThat(first.dataHash()).isEqualTo(second.dataHash());
        assertThat(first.dataHash()).hasSize(64);
    }

    @Test
    void batchPredictShouldReturnStableIndexedResults() {
        List<ImldInferenceApiDtos.Request.ImldPredictRequest> requests = List.of(
                buildRequest("P-001"),
                buildRequest("P-002")
        );

        ImldInferenceApiDtos.Response.BatchPredictData response = service.batchPredict(requests);

        assertThat(response.batchCount()).isEqualTo(2);
        assertThat(response.batchResults()).hasSize(2);
        assertThat(response.batchResults().get(0).sampleIndex()).isEqualTo(1);
        assertThat(response.batchResults().get(1).sampleIndex()).isEqualTo(2);
        assertThat(response.batchResults()).allSatisfy(item -> {
            assertThat(item.riskProbability()).isBetween(0.0, 1.0);
            assertThat(item.dataHash()).hasSize(64);
        });
    }

    @Test
    void healthShouldExposeMockEngine() {
        ImldInferenceApiDtos.Response.HealthResponse health = service.health();

        assertThat(health.service()).isEqualTo("running");
        assertThat(health.engine()).isEqualTo("mock");
        assertThat(health.modelLoaded()).isTrue();
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
                26,
                1,
                95.0,
                28.0,
                140.0,
                1,
                4,
                List.of(variant),
                patientId
        );
    }
}
