package xenosoft.imldintelligence.module.diagnoses.internal.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import xenosoft.imldintelligence.module.diagnoses.internal.config.ImldInferenceProperties;
import xenosoft.imldintelligence.module.diagnoses.internal.service.ImldInferenceService;

import static org.assertj.core.api.Assertions.assertThat;

class InferenceEngineSelectionTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(TestConfig.class);

    @Test
    void shouldLoadMockEngineWhenConfigured() {
        contextRunner
                .withPropertyValues("imld.inference.imld.engine=mock")
                .run(context -> {
                    assertThat(context).hasSingleBean(ImldInferenceService.class);
                    assertThat(context.getBean(ImldInferenceService.class)).isInstanceOf(MockImldInferenceService.class);
                });
    }

    @Test
    void shouldLoadXgboostEngineWhenConfigured() {
        contextRunner
                .withPropertyValues("imld.inference.imld.engine=xgboost-java")
                .run(context -> {
                    assertThat(context).hasSingleBean(ImldInferenceService.class);
                    assertThat(context.getBean(ImldInferenceService.class)).isInstanceOf(ImldInferenceServiceImpl.class);
                });
    }

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(ImldInferenceProperties.class)
    @Import({ImldInferenceServiceImpl.class, MockImldInferenceService.class})
    static class TestConfig {
        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper();
        }

        @Bean
        ResourceLoader resourceLoader() {
            return new DefaultResourceLoader();
        }
    }
}
