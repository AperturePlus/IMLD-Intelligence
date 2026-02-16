package xenosoft.imldintelligence.module.license.internal.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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

    public LicenseInfo readAndVerifyLicense(Path licenseFilePath, Path publicKeyFilePath) {
        PublicKey publicKey = cryptoService.loadRsaPublicKey(requireExistingPath(publicKeyFilePath, "Public key file"));
        LicenseEnvelope envelope = readSignedEnvelope(requireExistingPath(licenseFilePath, "License file"), "license");
        verifyEnvelope(envelope, publicKey, "license");
        return toValue(envelope.getPayload(), LicenseInfo.class, "license");
    }

    public ReleaseManifest readAndVerifyManifest(Path manifestFilePath, Path publicKeyFilePath) {
        PublicKey publicKey = cryptoService.loadRsaPublicKey(requireExistingPath(publicKeyFilePath, "Public key file"));
        LicenseEnvelope envelope = readSignedEnvelope(requireExistingPath(manifestFilePath, "Manifest file"), "release manifest");
        verifyEnvelope(envelope, publicKey, "release manifest");
        return toValue(envelope.getPayload(), ReleaseManifest.class, "release manifest");
    }

    public void validateDeploymentMode(LicenseInfo licenseInfo, String expectedMode) {
        String runtimeMode = normalize(expectedMode);
        String licenseMode = normalize(licenseInfo.getDeploymentMode());
        if (!runtimeMode.equals(licenseMode)) {
            throw new IllegalStateException(
                    "Deployment mode mismatch. expected=" + runtimeMode + ", license=" + licenseMode
            );
        }
    }

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

    public Path importLicenseFile(Path sourceLicenseFilePath, Path targetLicenseFilePath) {
        try {
            Path source = requireExistingPath(sourceLicenseFilePath, "Source license file");
            Path target = targetLicenseFilePath.toAbsolutePath().normalize();
            if (target.getParent() != null) {
                Files.createDirectories(target.getParent());
            }
            Files.copy(source, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            return target;
        } catch (Exception e) {
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
        } catch (Exception e) {
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

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public record UpgradeDecision(boolean allowed, String reason) {
    }
}
