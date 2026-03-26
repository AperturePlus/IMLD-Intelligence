package xenosoft.imldintelligence.module.license.internal.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.stereotype.Service;
import xenosoft.imldintelligence.module.license.api.dto.LicenseApiDtos;
import xenosoft.imldintelligence.module.license.internal.config.DeploymentProperties;
import xenosoft.imldintelligence.module.license.internal.config.LicenseProperties;
import xenosoft.imldintelligence.module.license.internal.config.UpgradeProperties;
import xenosoft.imldintelligence.module.license.internal.model.Fingerprint;
import xenosoft.imldintelligence.module.license.internal.model.LicenseEnvelope;
import xenosoft.imldintelligence.module.license.internal.model.LicenseInfo;
import xenosoft.imldintelligence.module.license.internal.model.ReleaseManifest;
import xenosoft.imldintelligence.module.license.internal.service.CryptoService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.PublicKey;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

/**
 * Application-facing license workflow service for private deployment activation and validation.
 */
@Service
public class LicenseManagementService {
    private static final String DEFAULT_LICENSE_FILE_NAME = "license.json";

    private final DeploymentProperties deploymentProperties;
    private final LicenseProperties licenseProperties;
    private final UpgradeProperties upgradeProperties;
    private final LicenseCliService licenseCliService;
    private final ActivationStateService activationStateService;
    private final CryptoService cryptoService;
    private final ObjectMapper objectMapper;

    public LicenseManagementService(DeploymentProperties deploymentProperties,
                                    LicenseProperties licenseProperties,
                                    UpgradeProperties upgradeProperties,
                                    LicenseCliService licenseCliService,
                                    ActivationStateService activationStateService,
                                    CryptoService cryptoService,
                                    ObjectMapper objectMapper) {
        this.deploymentProperties = deploymentProperties;
        this.licenseProperties = licenseProperties;
        this.upgradeProperties = upgradeProperties;
        this.licenseCliService = licenseCliService;
        this.activationStateService = activationStateService;
        this.cryptoService = cryptoService;
        this.objectMapper = objectMapper.copy();
        this.objectMapper.setConfig(
                this.objectMapper.getSerializationConfig().with(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
        );
        this.objectMapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
    }

    public LicenseApiDtos.Response.LicenseSummaryResponse getLicenseStatus() {
        String runtimeMode = deploymentProperties.normalizedMode();
        if (!deploymentProperties.isPrivateMode()) {
            return emptySummary(runtimeMode, true);
        }

        Path licenseFile = configuredLicensePath();
        Path publicKeyFile;
        try {
            publicKeyFile = configuredPublicKeyPath();
        } catch (IllegalStateException ex) {
            return emptySummary(runtimeMode, false);
        }
        if (!Files.exists(licenseFile) || !Files.exists(publicKeyFile)) {
            return emptySummary(runtimeMode, false);
        }

        try {
            LicenseInfo licenseInfo = licenseCliService.readAndVerifyLicense(licenseFile, publicKeyFile);
            return toSummaryResponse(licenseInfo);
        } catch (IllegalStateException ex) {
            return emptySummary(runtimeMode, false);
        }
    }

    public LicenseApiDtos.Response.LicenseSummaryResponse uploadLicenseEnvelope(
            LicenseApiDtos.Request.UploadLicenseEnvelopeRequest request) {
        ensurePrivateMode();
        PublicKey publicKey = loadConfiguredPublicKeyOrThrow();
        LicenseInfo licenseInfo = verifyLicensePayload(request.payload(), request.signature(), publicKey, "license");
        licenseCliService.validateDeploymentMode(licenseInfo, deploymentProperties.normalizedMode());
        if (licenseProperties.getPrivateEdition().isMachineBindingEnabled()) {
            licenseCliService.validateMachineBinding(licenseInfo);
        }
        if (hasText(request.activationCode())) {
            licenseCliService.validateActivationCode(licenseInfo, request.activationCode());
            activationStateService.storeActivationCode(
                    request.activationCode(),
                    licenseInfo.getLicenseId(),
                    new Fingerprint().getFingerprintHash(),
                    "api-upload-envelope"
            );
        }

        writeEnvelope(configuredLicensePath(), request.payload(), request.signature());
        return toSummaryResponse(licenseInfo);
    }

    public LicenseApiDtos.Response.LicenseValidationResponse activateLicense(
            LicenseApiDtos.Request.ActivateLicenseRequest request) {
        String runtimeFingerprintHash = new Fingerprint().getFingerprintHash();
        boolean supportActive = false;

        try {
            ensurePrivateMode();
            LicenseInfo licenseInfo = readConfiguredLicenseOrThrow();
            supportActive = isSupportActive(licenseInfo);

            licenseCliService.validateDeploymentMode(licenseInfo, deploymentProperties.normalizedMode());
            if (!runtimeFingerprintHash.equalsIgnoreCase(request.machineFingerprintHash().trim())) {
                throw new IllegalStateException("Machine fingerprint mismatch");
            }
            if (licenseProperties.getPrivateEdition().isMachineBindingEnabled()) {
                licenseCliService.validateMachineBinding(licenseInfo);
            }
            licenseCliService.validateActivationCode(licenseInfo, request.activationCode());

            activationStateService.storeActivationCode(
                    request.activationCode(),
                    licenseInfo.getLicenseId(),
                    runtimeFingerprintHash,
                    "api-activate"
            );

            return new LicenseApiDtos.Response.LicenseValidationResponse(
                    true,
                    "activation-success",
                    runtimeFingerprintHash,
                    supportActive
            );
        } catch (IllegalStateException ex) {
            return new LicenseApiDtos.Response.LicenseValidationResponse(
                    false,
                    messageOrDefault(ex.getMessage(), "activation-failed"),
                    runtimeFingerprintHash,
                    supportActive
            );
        }
    }

    public LicenseApiDtos.Response.LicenseValidationResponse validateLicense(
            LicenseApiDtos.Request.ValidateLicenseRequest request) {
        String runtimeFingerprintHash = new Fingerprint().getFingerprintHash();
        try {
            PublicKey publicKey = loadConfiguredPublicKeyOrThrow();
            LicenseInfo licenseInfo = verifyLicensePayload(request.payload(), request.signature(), publicKey, "license");
            licenseCliService.validateDeploymentMode(licenseInfo, deploymentProperties.normalizedMode());
            if (licenseProperties.getPrivateEdition().isMachineBindingEnabled()) {
                licenseCliService.validateMachineBinding(licenseInfo);
            }
            return new LicenseApiDtos.Response.LicenseValidationResponse(
                    true,
                    "signature-valid",
                    runtimeFingerprintHash,
                    isSupportActive(licenseInfo)
            );
        } catch (IllegalStateException ex) {
            return new LicenseApiDtos.Response.LicenseValidationResponse(
                    false,
                    messageOrDefault(ex.getMessage(), "validation-failed"),
                    runtimeFingerprintHash,
                    false
            );
        }
    }

    public LicenseApiDtos.Response.FingerprintResponse getFingerprint() {
        Fingerprint fingerprint = new Fingerprint();
        // The last field returns the full runtime fingerprint hash used by activation checks.
        return new LicenseApiDtos.Response.FingerprintResponse(
                cryptoService.sha256Hex(fingerprint.getCpuId()),
                cryptoService.sha256Hex(fingerprint.getMacAddr()),
                cryptoService.sha256Hex(fingerprint.getHdId()),
                cryptoService.sha256Hex(System.getProperty("os.name")
                        + System.getProperty("os.version")
                        + System.getProperty("os.arch")),
                fingerprint.getFingerprintHash()
        );
    }

    public LicenseApiDtos.Response.ReleaseEligibilityResponse evaluateReleaseEligibility(
            LicenseApiDtos.Query.ReleaseEligibilityQuery query) {
        if (!deploymentProperties.isPrivateMode()) {
            return new LicenseApiDtos.Response.ReleaseEligibilityResponse(true, "not-private-mode", false);
        }

        try {
            LicenseInfo licenseInfo = readConfiguredLicenseOrThrow();
            ReleaseManifest releaseManifest = new ReleaseManifest();
            releaseManifest.setReleaseDate(query.releaseDate());
            releaseManifest.setSecurityPatch(Boolean.TRUE.equals(query.securityPatch()));
            releaseManifest.setChannel(query.channel());

            LicenseCliService.UpgradeDecision decision = licenseCliService.evaluateUpgrade(
                    licenseInfo,
                    releaseManifest,
                    upgradeProperties.getEntitlement().isEnforceSupportWindow(),
                    upgradeProperties.getEntitlement().isAllowSecurityPatchAfterExpiry()
            );

            boolean securityPatchOverride = decision.allowed()
                    && "security-patch-allowed-after-expiry".equals(decision.reason());
            return new LicenseApiDtos.Response.ReleaseEligibilityResponse(
                    decision.allowed(),
                    decision.reason(),
                    securityPatchOverride
            );
        } catch (IllegalStateException ex) {
            return new LicenseApiDtos.Response.ReleaseEligibilityResponse(
                    false,
                    messageOrDefault(ex.getMessage(), "release-eligibility-failed"),
                    false
            );
        }
    }

    private LicenseInfo readConfiguredLicenseOrThrow() {
        Path licenseFile = configuredLicensePath();
        Path publicKeyFile = configuredPublicKeyPath();
        if (!Files.exists(licenseFile)) {
            throw new IllegalStateException("License file not found: " + licenseFile);
        }
        if (!Files.exists(publicKeyFile)) {
            throw new IllegalStateException("License public key file not found: " + publicKeyFile);
        }
        return licenseCliService.readAndVerifyLicense(licenseFile, publicKeyFile);
    }

    private PublicKey loadConfiguredPublicKeyOrThrow() {
        Path publicKeyFile = configuredPublicKeyPath();
        if (!Files.exists(publicKeyFile)) {
            throw new IllegalStateException("License public key file not found: " + publicKeyFile);
        }
        return cryptoService.loadRsaPublicKey(publicKeyFile);
    }

    private LicenseInfo verifyLicensePayload(JsonNode payload,
                                             String signature,
                                             PublicKey publicKey,
                                             String artifactName) {
        if (payload == null) {
            throw new IllegalStateException("Payload must not be null");
        }
        if (!hasText(signature)) {
            throw new IllegalStateException("Signature must not be blank");
        }
        String canonicalPayload = canonicalize(payload);
        if (!cryptoService.verifySha256WithRsa(canonicalPayload, signature, publicKey)) {
            throw new IllegalStateException(artifactName + " signature verification failed");
        }
        try {
            return objectMapper.treeToValue(payload, LicenseInfo.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Invalid " + artifactName + " payload format", e);
        }
    }

    private void writeEnvelope(Path file, JsonNode payload, String signature) {
        try {
            if (file.getParent() != null) {
                Files.createDirectories(file.getParent());
            }
            LicenseEnvelope envelope = new LicenseEnvelope();
            envelope.setPayload(payload);
            envelope.setSignature(signature);
            Files.writeString(
                    file,
                    objectMapper.writeValueAsString(envelope),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE
            );
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write license file: " + file, e);
        }
    }

    private LicenseApiDtos.Response.LicenseSummaryResponse toSummaryResponse(LicenseInfo licenseInfo) {
        List<String> features = licenseInfo.getFeatures() == null ? List.of() : licenseInfo.getFeatures();
        List<String> scenarios = licenseInfo.getScenarios() == null ? List.of() : licenseInfo.getScenarios();
        boolean machineBound = hasText(licenseInfo.getMachineFingerprintHash());
        boolean activated = isActivated(licenseInfo);

        return new LicenseApiDtos.Response.LicenseSummaryResponse(
                licenseInfo.getLicenseId(),
                licenseInfo.getHospitalId(),
                licenseInfo.getDeploymentMode(),
                licenseInfo.getIssuer(),
                licenseInfo.getIssuedAt(),
                licenseInfo.getSupportStartDate(),
                licenseInfo.getSupportEndDate(),
                features,
                scenarios,
                machineBound,
                activated
        );
    }

    private LicenseApiDtos.Response.LicenseSummaryResponse emptySummary(String mode, boolean activated) {
        return new LicenseApiDtos.Response.LicenseSummaryResponse(
                null,
                null,
                mode,
                null,
                null,
                null,
                null,
                List.of(),
                List.of(),
                false,
                activated
        );
    }

    private boolean isActivated(LicenseInfo licenseInfo) {
        if (!licenseProperties.getPrivateEdition().isActivationRequired()) {
            return true;
        }
        if (!hasText(licenseInfo.getActivationCodeHash())) {
            return false;
        }

        String runtimeActivationHash = resolveRuntimeActivationHash();
        return hasText(runtimeActivationHash)
                && runtimeActivationHash.equalsIgnoreCase(licenseInfo.getActivationCodeHash().trim());
    }

    private String resolveRuntimeActivationHash() {
        String configuredActivationCode = licenseProperties.getPrivateEdition().getActivationCode();
        if (hasText(configuredActivationCode)) {
            return cryptoService.sha256Hex(configuredActivationCode.trim());
        }
        return activationStateService.loadPersistedActivationCodeHash().orElse(null);
    }

    private boolean isSupportActive(LicenseInfo licenseInfo) {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        LocalDate supportStart = licenseInfo.getSupportStartDate();
        LocalDate supportEnd = licenseInfo.getSupportEndDate();
        if (supportEnd == null) {
            return false;
        }
        if (supportStart != null && today.isBefore(supportStart)) {
            return false;
        }
        return !today.isAfter(supportEnd);
    }

    private Path configuredLicensePath() {
        String configured = licenseProperties.getPrivateEdition().getLicenseFilePath();
        if (hasText(configured)) {
            return Path.of(configured.trim()).toAbsolutePath().normalize();
        }
        return Path.of("license", DEFAULT_LICENSE_FILE_NAME).toAbsolutePath().normalize();
    }

    private Path configuredPublicKeyPath() {
        String configured = licenseProperties.getPrivateEdition().getPublicKeyFilePath();
        if (!hasText(configured)) {
            throw new IllegalStateException("License public key file path is not configured");
        }
        return Path.of(configured.trim()).toAbsolutePath().normalize();
    }

    private String canonicalize(JsonNode payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to canonicalize payload", e);
        }
    }

    private void ensurePrivateMode() {
        if (!deploymentProperties.isPrivateMode()) {
            throw new IllegalStateException("License operations are only available in private mode");
        }
    }

    private String messageOrDefault(String value, String fallback) {
        if (!hasText(value)) {
            return fallback;
        }
        return value.trim();
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
