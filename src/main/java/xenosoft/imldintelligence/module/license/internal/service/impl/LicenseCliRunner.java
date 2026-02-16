package xenosoft.imldintelligence.module.license.internal.service.impl;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import xenosoft.imldintelligence.module.license.internal.config.LicenseProperties;
import xenosoft.imldintelligence.module.license.internal.config.UpgradeProperties;
import xenosoft.imldintelligence.module.license.internal.model.LicenseInfo;
import xenosoft.imldintelligence.module.license.internal.model.ReleaseManifest;

import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

@Component
@Order(0)
public class LicenseCliRunner implements ApplicationRunner {
    private static final String OPT_COMMAND = "license-cli-command";
    private static final String OPT_LICENSE_FILE = "license-cli-license-file";
    private static final String OPT_PUBLIC_KEY_FILE = "license-cli-public-key-file";
    private static final String OPT_MANIFEST_FILE = "license-cli-manifest-file";
    private static final String OPT_ACTIVATION_CODE = "license-cli-activation-code";
    private static final String OPT_TARGET_LICENSE_FILE = "license-cli-target-license-file";
    private static final String OPT_EXPECTED_MODE = "license-cli-expected-mode";
    private static final String OPT_ENFORCE_SUPPORT_WINDOW = "license-cli-enforce-support-window";
    private static final String OPT_ALLOW_SECURITY_PATCH = "license-cli-allow-security-patch-after-expiry";
    private static final String OPT_MACHINE_BINDING = "license-cli-machine-binding";

    private final ConfigurableApplicationContext applicationContext;
    private final LicenseCliService licenseCliService;
    private final LicenseProperties licenseProperties;
    private final UpgradeProperties upgradeProperties;

    public LicenseCliRunner(ConfigurableApplicationContext applicationContext,
                            LicenseCliService licenseCliService,
                            LicenseProperties licenseProperties,
                            UpgradeProperties upgradeProperties) {
        this.applicationContext = applicationContext;
        this.licenseCliService = licenseCliService;
        this.licenseProperties = licenseProperties;
        this.upgradeProperties = upgradeProperties;
    }

    @Override
    public void run(ApplicationArguments args) {
        String command = optionValue(args, OPT_COMMAND);
        if (!hasText(command)) {
            return;
        }

        try {
            switch (command.toLowerCase(Locale.ROOT)) {
                case "import-license" -> runImportLicense(args);
                case "check-upgrade" -> runCheckUpgrade(args);
                default -> throw new IllegalArgumentException("Unknown license CLI command: " + command);
            }
        } finally {
            applicationContext.close();
        }
    }

    private void runImportLicense(ApplicationArguments args) {
        Path sourceLicenseFile = requiredPathArg(args, OPT_LICENSE_FILE);
        Path publicKeyFile = requiredPathArg(args, OPT_PUBLIC_KEY_FILE);
        Path targetLicenseFile = targetLicenseFile(args);
        String activationCode = optionValue(args, OPT_ACTIVATION_CODE);
        String expectedMode = optionValueOrDefault(args, OPT_EXPECTED_MODE, "private");
        boolean machineBinding = booleanOption(args, OPT_MACHINE_BINDING, true);

        LicenseInfo licenseInfo = licenseCliService.readAndVerifyLicense(sourceLicenseFile, publicKeyFile);
        licenseCliService.validateDeploymentMode(licenseInfo, expectedMode);
        licenseCliService.validateActivationCode(licenseInfo, activationCode);
        if (machineBinding) {
            licenseCliService.validateMachineBinding(licenseInfo);
        }

        Path installedPath = licenseCliService.importLicenseFile(sourceLicenseFile, targetLicenseFile);
        System.out.println("LICENSE_IMPORT_OK");
        System.out.println("licenseId=" + safe(licenseInfo.getLicenseId()));
        System.out.println("hospitalId=" + safe(licenseInfo.getHospitalId()));
        System.out.println("supportEndDate=" + safe(licenseInfo.getSupportEndDate()));
        System.out.println("installedPath=" + installedPath);
    }

    private void runCheckUpgrade(ApplicationArguments args) {
        Path licenseFile = requiredPathArg(args, OPT_LICENSE_FILE);
        Path publicKeyFile = requiredPathArg(args, OPT_PUBLIC_KEY_FILE);
        Path manifestFile = requiredPathArg(args, OPT_MANIFEST_FILE);
        String activationCode = optionValue(args, OPT_ACTIVATION_CODE);
        String expectedMode = optionValueOrDefault(args, OPT_EXPECTED_MODE, "private");
        boolean machineBinding = booleanOption(args, OPT_MACHINE_BINDING, true);
        boolean enforceSupportWindow = booleanOption(
                args,
                OPT_ENFORCE_SUPPORT_WINDOW,
                upgradeProperties.getEntitlement().isEnforceSupportWindow()
        );
        boolean allowSecurityPatchAfterExpiry = booleanOption(
                args,
                OPT_ALLOW_SECURITY_PATCH,
                upgradeProperties.getEntitlement().isAllowSecurityPatchAfterExpiry()
        );

        LicenseInfo licenseInfo = licenseCliService.readAndVerifyLicense(licenseFile, publicKeyFile);
        licenseCliService.validateDeploymentMode(licenseInfo, expectedMode);
        licenseCliService.validateActivationCode(licenseInfo, activationCode);
        if (machineBinding) {
            licenseCliService.validateMachineBinding(licenseInfo);
        }

        ReleaseManifest releaseManifest = licenseCliService.readAndVerifyManifest(manifestFile, publicKeyFile);
        LicenseCliService.UpgradeDecision decision = licenseCliService.evaluateUpgrade(
                licenseInfo,
                releaseManifest,
                enforceSupportWindow,
                allowSecurityPatchAfterExpiry
        );

        System.out.println("UPGRADE_DECISION");
        System.out.println("allowed=" + decision.allowed());
        System.out.println("reason=" + decision.reason());
        System.out.println("licenseId=" + safe(licenseInfo.getLicenseId()));
        System.out.println("supportEndDate=" + safe(licenseInfo.getSupportEndDate()));
        System.out.println("releaseVersion=" + safe(releaseManifest.getVersion()));
        System.out.println("releaseDate=" + safe(releaseManifest.getReleaseDate()));

        if (!decision.allowed()) {
            throw new IllegalStateException("Upgrade is not allowed: " + decision.reason());
        }
    }

    private Path targetLicenseFile(ApplicationArguments args) {
        String option = optionValue(args, OPT_TARGET_LICENSE_FILE);
        if (hasText(option)) {
            return Path.of(option.trim());
        }
        String configured = licenseProperties.getPrivateEdition().getLicenseFilePath();
        if (hasText(configured)) {
            return Path.of(configured.trim());
        }
        return Path.of("license", "license.json");
    }

    private Path requiredPathArg(ApplicationArguments args, String optionName) {
        String value = optionValue(args, optionName);
        if (!hasText(value)) {
            throw new IllegalArgumentException("Missing required option --" + optionName);
        }
        return Path.of(value.trim());
    }

    private String optionValueOrDefault(ApplicationArguments args, String optionName, String defaultValue) {
        String value = optionValue(args, optionName);
        return hasText(value) ? value.trim() : defaultValue;
    }

    private boolean booleanOption(ApplicationArguments args, String optionName, boolean defaultValue) {
        String value = optionValue(args, optionName);
        if (!hasText(value)) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value.trim());
    }

    private String optionValue(ApplicationArguments args, String optionName) {
        List<String> values = args.getOptionValues(optionName);
        if (values == null || values.isEmpty()) {
            return null;
        }
        return values.getLast();
    }

    private String safe(Object value) {
        return value == null ? "" : value.toString();
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
