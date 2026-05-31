package xenosoft.imldintelligence.common.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.DefaultApplicationArguments;
import xenosoft.imldintelligence.module.diagnoses.internal.config.ImldInferenceProperties;
import xenosoft.imldintelligence.module.identity.internal.config.IdentityVerificationProperties;
import xenosoft.imldintelligence.module.identity.internal.security.IdentitySecurityProperties;
import xenosoft.imldintelligence.module.identity.internal.service.VerificationSmsSender;
import xenosoft.imldintelligence.module.license.internal.config.DeploymentProperties;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConfigurationGuardTest {

    @Test
    void shouldRejectUnsupportedDeploymentMode() {
        ConfigurationGuard guard = guard("invalid", true, true, "mock", "xgboost-java", true);

        assertThatThrownBy(() -> guard.run(new DefaultApplicationArguments()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Unsupported deployment mode");
    }

    @Test
    void shouldRejectSecurityDisabledInSaasOrPrivate() {
        ConfigurationGuard guard = guard("saas", false, false, "real", "xgboost-java", true);

        assertThatThrownBy(() -> guard.run(new DefaultApplicationArguments()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("imld.security.enabled");
    }

    @Test
    void shouldRejectMockProviderInSaasOrPrivate() {
        ConfigurationGuard guard = guard("private", true, false, "mock", "xgboost-java", true);

        assertThatThrownBy(() -> guard.run(new DefaultApplicationArguments()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("SMS mock provider");
    }

    @Test
    void shouldRejectMockEngineInSaasOrPrivate() {
        ConfigurationGuard guard = guard("saas", true, false, "real", "mock", true);

        assertThatThrownBy(() -> guard.run(new DefaultApplicationArguments()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Mock inference engine");
    }

    @Test
    void shouldRejectRealSmsProviderWithoutImplementationWhenEnabled() {
        ConfigurationGuard guard = guard("private", true, true, "real", "xgboost-java", false);

        assertThatThrownBy(() -> guard.run(new DefaultApplicationArguments()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no VerificationSmsSender implementation");
    }

    @Test
    void shouldAllowDevelopModeWithMockDependencies() {
        ConfigurationGuard guard = guard("develop", false, true, "mock", "mock", false);

        assertThatCode(() -> guard.run(new DefaultApplicationArguments()))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldSkipValidationForLicenseCliCommand() {
        ConfigurationGuard guard = guard("saas", false, true, "mock", "mock", false);

        assertThatCode(() -> guard.run(new DefaultApplicationArguments("--license-cli-command=validate")))
                .doesNotThrowAnyException();
    }

    private ConfigurationGuard guard(String mode,
                                     boolean securityEnabled,
                                     boolean smsEnabled,
                                     String smsProvider,
                                     String inferenceEngine,
                                     boolean smsBeanPresent) {
        DeploymentProperties deploymentProperties = new DeploymentProperties();
        deploymentProperties.setMode(mode);

        IdentitySecurityProperties securityProperties = new IdentitySecurityProperties();
        securityProperties.setEnabled(securityEnabled);

        IdentityVerificationProperties verificationProperties = new IdentityVerificationProperties();
        verificationProperties.getSms().setEnabled(smsEnabled);
        verificationProperties.getSms().setProvider(smsProvider);

        ImldInferenceProperties inferenceProperties = new ImldInferenceProperties();
        inferenceProperties.setEngine(inferenceEngine);

        @SuppressWarnings("unchecked")
        ObjectProvider<VerificationSmsSender> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(smsBeanPresent ? mock(VerificationSmsSender.class) : null);

        return new ConfigurationGuard(
                deploymentProperties,
                securityProperties,
                verificationProperties,
                inferenceProperties,
                provider
        );
    }
}
