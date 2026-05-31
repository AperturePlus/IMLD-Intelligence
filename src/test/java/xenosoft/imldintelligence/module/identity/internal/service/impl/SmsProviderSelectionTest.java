package xenosoft.imldintelligence.module.identity.internal.service.impl;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import xenosoft.imldintelligence.module.identity.internal.config.IdentityVerificationProperties;
import xenosoft.imldintelligence.module.identity.internal.service.VerificationSmsSender;

import static org.assertj.core.api.Assertions.assertThat;

class SmsProviderSelectionTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(TestConfig.class);

    @Test
    void shouldLoadMockSmsSenderWhenProviderIsMockAndEnabled() {
        contextRunner
                .withPropertyValues(
                        "imld.identity.verification.sms.enabled=true",
                        "imld.identity.verification.sms.provider=mock"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(VerificationSmsSender.class);
                    assertThat(context.getBean(VerificationSmsSender.class)).isInstanceOf(MockVerificationSmsSender.class);
                });
    }

    @Test
    void shouldNotLoadSmsSenderWhenProviderIsReal() {
        contextRunner
                .withPropertyValues(
                        "imld.identity.verification.sms.enabled=true",
                        "imld.identity.verification.sms.provider=real"
                )
                .run(context -> assertThat(context).doesNotHaveBean(VerificationSmsSender.class));
    }

    @Test
    void shouldNotLoadSmsSenderWhenSmsDisabled() {
        contextRunner
                .withPropertyValues(
                        "imld.identity.verification.sms.enabled=false",
                        "imld.identity.verification.sms.provider=mock"
                )
                .run(context -> assertThat(context).doesNotHaveBean(VerificationSmsSender.class));
    }

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(IdentityVerificationProperties.class)
    @Import(MockVerificationSmsSender.class)
    static class TestConfig {
    }
}
