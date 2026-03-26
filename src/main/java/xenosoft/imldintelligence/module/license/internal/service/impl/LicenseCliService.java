package xenosoft.imldintelligence.module.license.internal.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.PublicKey;
import java.time.LocalDate;
import java.util.Locale;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import xenosoft.imldintelligence.module.license.internal.model.Fingerprint;
import xenosoft.imldintelligence.module.license.internal.model.LicenseEnvelope;
import xenosoft.imldintelligence.module.license.internal.model.LicenseInfo;
import xenosoft.imldintelligence.module.license.internal.model.ReleaseManifest;
import xenosoft.imldintelligence.module.license.internal.service.CryptoService;

/**
 * Provides reusable license CLI operations for verifying artifacts and evaluating upgrade rights.
 */
@Service
public class LicenseCliService {
    private final CryptoService cryptoService;
    private final ObjectMapper objectMapper;

    public LicenseCliService(CryptoService cryptoService, ObjectMapper objectMapper) {
        this.cryptoService = cryptoService;
        this.objectMapper = objectMapper.copy();
        this.objectMapper.setConfig(
                this.objectMapper.getSerializationConfig().with(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
        );
        this.objectMapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
    }

    /**
     * Reads a signed license file, verifies its signature, and converts it into a typed payload.
     *
     * @param licenseFilePath path to the signed license envelope
     * @param publicKeyFilePath path to the RSA public key used for verification
     * @return verified license payload
     * @throws IllegalStateException if the artifact cannot be read, verified, or deserialized
     */
    public LicenseInfo readAndVerifyLicense(Path licenseFilePath, Path publicKeyFilePath) {
        PublicKey publicKey = cryptoService.loadRsaPublicKey(requireExistingPath(publicKeyFilePath, "Public key file"));
        LicenseEnvelope envelope = readSignedEnvelope(requireExistingPath(licenseFilePath, "License file"), "license");
        verifyEnvelope(envelope, publicKey, "license");
        return toValue(envelope.getPayload(), LicenseInfo.class, "license");
    }

    /**
     * Reads a signed release manifest, verifies its signature, and converts it into a typed payload.
     *
     * @param manifestFilePath path to the signed manifest envelope
     * @param publicKeyFilePath path to the RSA public key used for verification
     * @return verified release manifest payload
     * @throws IllegalStateException if the artifact cannot be read, verified, or deserialized
     */
    public ReleaseManifest readAndVerifyManifest(Path manifestFilePath, Path publicKeyFilePath) {
        PublicKey publicKey = cryptoService.loadRsaPublicKey(requireExistingPath(publicKeyFilePath, "Public key file"));
        LicenseEnvelope envelope = readSignedEnvelope(requireExistingPath(manifestFilePath, "Manifest file"), "release manifest");
        verifyEnvelope(envelope, publicKey, "release manifest");
        return toValue(envelope.getPayload(), ReleaseManifest.class, "release manifest");
    }

    /**
     * Confirms that the license deployment mode matches the expected runtime mode.
     *
     * @param licenseInfo verified license payload
     * @param expectedMode expected deployment mode, for example {@code private} or {@code saas}
     * @throws IllegalStateException if the expected mode differs from the license mode
     */
    public void validateDeploymentMode(LicenseInfo licenseInfo, String expectedMode) {
        String runtimeMode = normalizeDeploymentMode(expectedMode);
        String licenseMode = normalizeDeploymentMode(licenseInfo.getDeploymentMode());
        if (!runtimeMode.equals(licenseMode)) {
            throw new IllegalStateException(
                    "Deployment mode mismatch. expected=" + runtimeMode + ", license=" + licenseMode
            );
        }
    }

    /**
     * Validates an operator-supplied activation code when the license requires one.
     *
     * @param licenseInfo verified license payload
     * @param activationCode activation code entered for the current deployment
     * @throws IllegalStateException if the license requires an activation code and the supplied value is invalid
     */
    public void validateActivationCode(LicenseInfo licenseInfo, String activationCode) {
        if (!hasText(licenseInfo.getActivationCodeHash())) {
            return;
        }
        if (!hasText(activationCode)) {
            throw new IllegalStateException("Activation code is required by license but was not provided");
        }
        String hash = cryptoService.sha256Hex(activationCode.trim());
        if (!hash.equalsIgnoreCase(licenseInfo.getActivationCodeHash().trim())) {
            throw new IllegalStateException("Activation code mismatch");
        }
    }

    /**
     * Verifies that the current machine fingerprint matches the fingerprint bound to the license.
     *
     * @param licenseInfo verified license payload
     * @throws IllegalStateException if machine binding is required and the local fingerprint does not match
     */
    public void validateMachineBinding(LicenseInfo licenseInfo) {
        if (!hasText(licenseInfo.getMachineFingerprintHash())) {
            return;
        }
        Fingerprint fingerprint = new Fingerprint();
        String runtimeHash = fingerprint.getFingerprintHash();
        if (!runtimeHash.equalsIgnoreCase(licenseInfo.getMachineFingerprintHash().trim())) {
            throw new IllegalStateException("Machine fingerprint mismatch");
        }
    }

    /**
     * Evaluates whether a target release is covered by the license support window policy.
     *
     * @param licenseInfo verified license payload
     * @param releaseManifest verified target release manifest
     * @param enforceSupportWindow whether support-window enforcement is enabled
     * @param allowSecurityPatchAfterExpiry whether security patches may bypass an expired support window
     * @return decision object describing whether the upgrade is allowed and why
     */
    public UpgradeDecision evaluateUpgrade(LicenseInfo licenseInfo,
                                           ReleaseManifest releaseManifest,
                                           boolean enforceSupportWindow,
                                           boolean allowSecurityPatchAfterExpiry) {
        if (!enforceSupportWindow) {
            return new UpgradeDecision(true, "support-window-check-disabled");
        }
        if (licenseInfo.getSupportEndDate() == null) {
            return new UpgradeDecision(false, "license-support-end-date-missing");
        }
        if (releaseManifest.getReleaseDate() == null) {
            return new UpgradeDecision(false, "manifest-release-date-missing");
        }

        LocalDate supportEndDate = licenseInfo.getSupportEndDate();
        LocalDate releaseDate = releaseManifest.getReleaseDate();
        if (!releaseDate.isAfter(supportEndDate)) {
            return new UpgradeDecision(true, "within-support-window");
        }
        if (releaseManifest.isSecurityPatch() && allowSecurityPatchAfterExpiry) {
            return new UpgradeDecision(true, "security-patch-allowed-after-expiry");
        }
        return new UpgradeDecision(false, "outside-support-window");
    }

    /**
     * Copies a verified license file into the configured installation location.
     *
     * @param sourceLicenseFilePath path to the source license file
     * @param targetLicenseFilePath destination path where the license should be installed
     * @return normalized target path of the installed license file
     * @throws IllegalStateException if the source file is missing or the copy operation fails
     */
    public Path importLicenseFile(Path sourceLicenseFilePath, Path targetLicenseFilePath) {
        try {
            Path source = requireExistingPath(sourceLicenseFilePath, "Source license file");
            Path target = targetLicenseFilePath.toAbsolutePath().normalize();
            if (target.getParent() != null) {
                Files.createDirectories(target.getParent());
            }
            Files.copy(source, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            return target;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to import license file", e);
        }
    }

    private void verifyEnvelope(LicenseEnvelope envelope, PublicKey publicKey, String artifactName) {
        String canonicalPayload = canonicalize(envelope.getPayload());
        if (!cryptoService.verifySha256WithRsa(canonicalPayload, envelope.getSignature(), publicKey)) {
            throw new IllegalStateException(artifactName + " signature verification failed");
        }
    }

    private <T> T toValue(JsonNode payload, Class<T> clazz, String artifactName) {
        try {
            return objectMapper.treeToValue(payload, clazz);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Invalid " + artifactName + " payload format", e);
        }
    }

    private LicenseEnvelope readSignedEnvelope(Path path, String artifactName) {
        try {
            JsonNode root = objectMapper.readTree(Files.readString(path));
            LicenseEnvelope envelope = objectMapper.treeToValue(root, LicenseEnvelope.class);
            if (envelope.getPayload() == null || !hasText(envelope.getSignature())) {
                throw new IllegalStateException("Invalid signed envelope for " + artifactName + ": " + path);
            }
            return envelope;
        } catch (IOException | IllegalArgumentException | IllegalStateException e) {
            throw new IllegalStateException("Failed to read " + artifactName + " file: " + path, e);
        }
    }

    private String canonicalize(JsonNode payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to canonicalize payload", e);
        }
    }

    private Path requireExistingPath(Path path, String description) {
        if (path == null) {
            throw new IllegalStateException(description + " is required");
        }
        Path normalized = path.toAbsolutePath().normalize();
        if (!Files.exists(normalized)) {
            throw new IllegalStateException(description + " not found: " + normalized);
        }
        return normalized;
    }

    private String normalizeDeploymentMode(String value) {
        if (value == null) {
            return "";
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if ("hybrid".equals(normalized)) {
            return "private";
        }
        return normalized;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    /**
     * Encapsulates the result of an upgrade entitlement evaluation.
     *
     * @param allowed whether the upgrade is permitted
     * @param reason machine-readable reason for the decision
     */
    public record UpgradeDecision(boolean allowed, String reason) {
    }
}
