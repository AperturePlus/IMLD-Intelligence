package xenosoft.imldintelligence.module.license.internal.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

class ProfileLicenseConfigurationTest {

    @Test
    void shouldDisableStartupValidationForDevPrivateProfile() {
        try (ConfigurableApplicationContext context = loadContext("--spring.profiles.active=dev-private")) {
            DeploymentProperties deploymentProperties = context.getBean(DeploymentProperties.class);
            LicenseProperties licenseProperties = context.getBean(LicenseProperties.class);

            assertThat(deploymentProperties.normalizedMode()).isEqualTo("private");
            assertThat(licenseProperties.getPrivateEdition().isStartupValidationEnabled()).isFalse();
        }
    }

    @Test
    void shouldAllowStartupValidationOverrideForDevPrivateProfile() {
        try (ConfigurableApplicationContext context = loadContext(
                "--spring.profiles.active=dev-private",
                "--imld.licensing.private-edition.startup-validation-enabled=true"
        )) {
            LicenseProperties licenseProperties = context.getBean(LicenseProperties.class);

            assertThat(licenseProperties.getPrivateEdition().isStartupValidationEnabled()).isTrue();
        }
    }

    @Test
    void shouldEnableStartupValidationForPrivateProfileByDefault() {
        try (ConfigurableApplicationContext context = loadContext("--spring.profiles.active=private")) {
            DeploymentProperties deploymentProperties = context.getBean(DeploymentProperties.class);
            LicenseProperties licenseProperties = context.getBean(LicenseProperties.class);

            assertThat(deploymentProperties.normalizedMode()).isEqualTo("private");
            assertThat(licenseProperties.getPrivateEdition().isStartupValidationEnabled()).isTrue();
        }
    }

    private ConfigurableApplicationContext loadContext(String... args) {
        return new SpringApplicationBuilder(TestConfig.class)
                .web(WebApplicationType.NONE)
                .properties(
                        "spring.main.banner-mode=off",
                        "spring.main.log-startup-info=false"
                )
                .run(args);
    }

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties({DeploymentProperties.class, LicenseProperties.class})
    static class TestConfig {
    }
}
