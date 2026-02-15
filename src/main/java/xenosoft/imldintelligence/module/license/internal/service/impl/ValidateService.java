package xenosoft.imldintelligence.module.license.internal.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.stereotype.Service;
import xenosoft.imldintelligence.module.license.internal.config.DeploymentProperties;
import xenosoft.imldintelligence.module.license.internal.config.LicenseProperties;
import xenosoft.imldintelligence.module.license.internal.config.UpgradeProperties;
import xenosoft.imldintelligence.module.license.internal.model.Fingerprint;
import xenosoft.imldintelligence.module.license.internal.model.LicenseEnvelope;
import xenosoft.imldintelligence.module.license.internal.model.LicenseInfo;
import xenosoft.imldintelligence.module.license.internal.model.ReleaseManifest;
import xenosoft.imldintelligence.module.license.internal.service.CryptoService;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.PublicKey;
import java.time.LocalDate;
import java.util.Locale;

@Service
public class ValidateService {
    private final DeploymentProperties deploymentProperties;
    private final LicenseProperties licenseProperties;
    private final UpgradeProperties upgradeProperties;
    private final CryptoService cryptoService;
    private final ObjectMapper objectMapper;

    public ValidateService(DeploymentProperties deploymentProperties,
                           LicenseProperties licenseProperties,
                           UpgradeProperties upgradeProperties,
                           CryptoService cryptoService,
                           ObjectMapper objectMapper) {
        this.deploymentProperties = deploymentProperties;
        this.licenseProperties = licenseProperties;
        this.upgradeProperties = upgradeProperties;
        this.cryptoService = cryptoService;
        this.objectMapper = objectMapper.copy();
        this.objectMapper.setConfig(
                this.objectMapper.getSerializationConfig().with(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
        );
        this.objectMapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
    }

    public void validateStartupOrThrow() {
        if (!deploymentProperties.isPrivateMode()) {
            return;
        }
        LicenseProperties.PrivateEdition privateEdition = licenseProperties.getPrivateEdition();
        if (!privateEdition.isStartupValidationEnabled()) {
            return;
        }

        PublicKey publicKey = cryptoService.loadRsaPublicKey(requireExistingPath(
                privateEdition.getPublicKeyFilePath(),
                "License public key file is required for private deployment validation"
        ));

        LicenseEnvelope envelope = readSignedEnvelope(
                requireExistingPath(privateEdition.getLicenseFilePath(), "License file is required in private mode"),
                "license"
        );
        String canonicalPayload = canonicalize(envelope.getPayload());
        if (!cryptoService.verifySha256WithRsa(canonicalPayload, envelope.getSignature(), publicKey)) {
            throw new IllegalStateException("License signature verification failed");
        }

        LicenseInfo licenseInfo = toLicenseInfo(envelope.getPayload());
        validateDeploymentMode(licenseInfo);
        validateActivationCode(licenseInfo, privateEdition);
        validateMachineBinding(licenseInfo, privateEdition);

        if (upgradeProperties.isCheckEnabled() && hasText(upgradeProperties.getManifestFilePath())) {
            validateUpgradeManifestOrThrow(licenseInfo, publicKey);
        }
    }

    void validateUpgradeEntitlementOrThrow(LicenseInfo licenseInfo, ReleaseManifest releaseManifest) {
        UpgradeProperties.Entitlement entitlement = upgradeProperties.getEntitlement();
        if (!entitlement.isEnforceSupportWindow()) {
            return;
        }

        if (licenseInfo.getSupportEndDate() == null) {
            throw new IllegalStateException("License supportEndDate is missing");
        }
        if (releaseManifest.getReleaseDate() == null) {
            throw new IllegalStateException("Release manifest releaseDate is missing");
        }

        LocalDate supportEnd = licenseInfo.getSupportEndDate();
        LocalDate releaseDate = releaseManifest.getReleaseDate();
        if (!releaseDate.isAfter(supportEnd)) {
            return;
        }
        if (releaseManifest.isSecurityPatch() && entitlement.isAllowSecurityPatchAfterExpiry()) {
            return;
        }
        throw new IllegalStateException(
                "Upgrade is outside support window. supportEndDate=" + supportEnd + ", releaseDate=" + releaseDate
        );
    }

    private void validateUpgradeManifestOrThrow(LicenseInfo licenseInfo, PublicKey publicKey) {
        ReleaseManifest releaseManifest = readSignedReleaseManifest(
                requireExistingPath(upgradeProperties.getManifestFilePath(), "Upgrade manifest file is configured but not found"),
                publicKey
        );
        validateUpgradeEntitlementOrThrow(licenseInfo, releaseManifest);
    }

    private ReleaseManifest readSignedReleaseManifest(Path manifestPath, PublicKey publicKey) {
        LicenseEnvelope manifestEnvelope = readSignedEnvelope(manifestPath, "release manifest");
        String canonicalPayload = canonicalize(manifestEnvelope.getPayload());
        if (!cryptoService.verifySha256WithRsa(canonicalPayload, manifestEnvelope.getSignature(), publicKey)) {
            throw new IllegalStateException("Release manifest signature verification failed");
        }
        try {
            return objectMapper.treeToValue(manifestEnvelope.getPayload(), ReleaseManifest.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Invalid release manifest payload format", e);
        }
    }

    private void validateDeploymentMode(LicenseInfo licenseInfo) {
        String licenseMode = normalize(licenseInfo.getDeploymentMode());
        String runtimeMode = deploymentProperties.normalizedMode();
        if (!runtimeMode.equals(licenseMode)) {
            throw new IllegalStateException(
                    "License deploymentMode mismatch. runtime=" + runtimeMode + ", license=" + licenseMode
            );
        }
    }

    private void validateActivationCode(LicenseInfo licenseInfo, LicenseProperties.PrivateEdition privateEdition) {
        if (!privateEdition.isActivationRequired()) {
            return;
        }
        if (!hasText(licenseInfo.getActivationCodeHash())) {
            throw new IllegalStateException("License activationCodeHash is missing");
        }
        if (!hasText(privateEdition.getActivationCode())) {
            throw new IllegalStateException("Activation code is required but not provided");
        }
        String runtimeActivationHash = cryptoService.sha256Hex(privateEdition.getActivationCode().trim());
        if (!runtimeActivationHash.equalsIgnoreCase(licenseInfo.getActivationCodeHash().trim())) {
            throw new IllegalStateException("Activation code mismatch");
        }
    }

    private void validateMachineBinding(LicenseInfo licenseInfo, LicenseProperties.PrivateEdition privateEdition) {
        if (!privateEdition.isMachineBindingEnabled()) {
            return;
        }
        if (!hasText(licenseInfo.getMachineFingerprintHash())) {
            throw new IllegalStateException("License machineFingerprintHash is missing");
        }
        Fingerprint fingerprint = new Fingerprint();
        String runtimeFingerprintHash = fingerprint.getFingerprintHash();
        if (!runtimeFingerprintHash.equalsIgnoreCase(licenseInfo.getMachineFingerprintHash().trim())) {
            throw new IllegalStateException("Machine fingerprint mismatch");
        }
    }

    private LicenseInfo toLicenseInfo(JsonNode payload) {
        try {
            return objectMapper.treeToValue(payload, LicenseInfo.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Invalid license payload format", e);
        }
    }

    private LicenseEnvelope readSignedEnvelope(Path path, String artifactName) {
        try {
            JsonNode root = objectMapper.readTree(Files.readString(path));
            if (root == null || root.isNull()) {
                throw new IllegalStateException("Empty " + artifactName + " file: " + path);
            }
            LicenseEnvelope envelope = objectMapper.treeToValue(root, LicenseEnvelope.class);
            if (envelope.getPayload() == null || !hasText(envelope.getSignature())) {
                throw new IllegalStateException("Invalid signed envelope for " + artifactName + ": " + path);
            }
            return envelope;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to read " + artifactName + " file: " + path, e);
        }
    }

    private Path requireExistingPath(String value, String message) {
        if (!hasText(value)) {
            throw new IllegalStateException(message);
        }
        Path path = Path.of(value.trim());
        if (!Files.exists(path)) {
            throw new IllegalStateException(message + " [" + path + "]");
        }
        return path;
    }

    private String canonicalize(JsonNode payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to canonicalize payload", e);
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
