package xenosoft.imldintelligence.common.config;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import xenosoft.imldintelligence.module.diagnoses.internal.config.ImldInferenceProperties;
import xenosoft.imldintelligence.module.identity.internal.config.IdentityVerificationProperties;
import xenosoft.imldintelligence.module.identity.internal.security.IdentitySecurityProperties;
import xenosoft.imldintelligence.module.identity.internal.service.VerificationSmsSender;
import xenosoft.imldintelligence.module.license.internal.config.DeploymentProperties;

import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Centralized startup guard for deployment-mode dependent configuration constraints.
 */
@Component
public class ConfigurationGuard implements ApplicationRunner {
    private static final String LICENSE_CLI_COMMAND_OPTION = "license-cli-command";
    private static final Set<String> SUPPORTED_SMS_PROVIDERS = Set.of("mock", "real");
    private static final Set<String> SUPPORTED_INFERENCE_ENGINES = Set.of("xgboost-java", "mock");

    private final DeploymentProperties deploymentProperties;
    private final IdentitySecurityProperties securityProperties;
    private final IdentityVerificationProperties verificationProperties;
    private final ImldInferenceProperties inferenceProperties;
    private final ObjectProvider<VerificationSmsSender> smsSenderProvider;

    public ConfigurationGuard(DeploymentProperties deploymentProperties,
                              IdentitySecurityProperties securityProperties,
                              IdentityVerificationProperties verificationProperties,
                              ImldInferenceProperties inferenceProperties,
                              ObjectProvider<VerificationSmsSender> smsSenderProvider) {
        this.deploymentProperties = deploymentProperties;
        this.securityProperties = securityProperties;
        this.verificationProperties = verificationProperties;
        this.inferenceProperties = inferenceProperties;
        this.smsSenderProvider = smsSenderProvider;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (isLicenseCliCommandActive(args)) {
            return;
        }

        if (!deploymentProperties.isSupportedMode()) {
            throw new IllegalStateException("Unsupported deployment mode: " + deploymentProperties.getMode());
        }

        String smsProvider = normalizeLower(verificationProperties.getSms().getProvider());
        if (!SUPPORTED_SMS_PROVIDERS.contains(smsProvider)) {
            throw new IllegalStateException("Unsupported SMS provider: " + verificationProperties.getSms().getProvider());
        }

        String inferenceEngine = normalizeLower(inferenceProperties.getEngine());
        if (!SUPPORTED_INFERENCE_ENGINES.contains(inferenceEngine)) {
            throw new IllegalStateException("Unsupported inference engine: " + inferenceProperties.getEngine());
        }

        boolean productionLikeMode = deploymentProperties.isSaasMode() || deploymentProperties.isPrivateMode();
        if (!productionLikeMode) {
            return;
        }

        if (!securityProperties.isEnabled()) {
            throw new IllegalStateException("imld.security.enabled must be true in saas/private mode");
        }
        if ("mock".equals(smsProvider)) {
            throw new IllegalStateException("SMS mock provider is forbidden in saas/private mode");
        }
        if ("mock".equals(inferenceEngine)) {
            throw new IllegalStateException("Mock inference engine is forbidden in saas/private mode");
        }
        if (verificationProperties.getSms().isEnabled()
                && "real".equals(smsProvider)
                && smsSenderProvider.getIfAvailable() == null) {
            throw new IllegalStateException("SMS provider is real but no VerificationSmsSender implementation is registered");
        }
    }

    private boolean isLicenseCliCommandActive(ApplicationArguments args) {
        List<String> values = args.getOptionValues(LICENSE_CLI_COMMAND_OPTION);
        if (values == null || values.isEmpty()) {
            return false;
        }
        String command = values.get(values.size() - 1);
        return command != null && !command.trim().isEmpty();
    }

    private String normalizeLower(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
