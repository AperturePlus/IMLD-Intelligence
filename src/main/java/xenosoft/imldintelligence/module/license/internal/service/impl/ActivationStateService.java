package xenosoft.imldintelligence.module.license.internal.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import xenosoft.imldintelligence.module.license.internal.config.LicenseProperties;
import xenosoft.imldintelligence.module.license.internal.model.ActivationState;
import xenosoft.imldintelligence.module.license.internal.service.CryptoService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

/**
 * Stores and loads private deployment activation state from local disk.
 */
@Service
public class ActivationStateService {
    private static final String DEFAULT_STATE_FILE_NAME = "activation-state.json";

    private final LicenseProperties licenseProperties;
    private final CryptoService cryptoService;
    private final ObjectMapper objectMapper;

    public ActivationStateService(LicenseProperties licenseProperties,
                                  CryptoService cryptoService,
                                  ObjectMapper objectMapper) {
        this.licenseProperties = licenseProperties;
        this.cryptoService = cryptoService;
        this.objectMapper = objectMapper.copy();
    }

    /**
     * Reads persisted activation hash state, if present.
     */
    public Optional<ActivationState> readState() {
        Path stateFile = resolveStateFilePath();
        if (!Files.exists(stateFile)) {
            return Optional.empty();
        }

        try {
            ActivationState state = objectMapper.readValue(Files.readString(stateFile), ActivationState.class);
            return Optional.ofNullable(state);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read activation state file: " + stateFile, e);
        }
    }

    /**
     * Reads persisted activation hash only.
     */
    public Optional<String> loadPersistedActivationCodeHash() {
        return readState()
                .map(ActivationState::getActivationCodeHash)
                .map(String::trim)
                .filter(value -> !value.isEmpty());
    }

    /**
     * Persists activation hash and metadata for future startup validation.
     */
    public void storeActivationCode(String activationCode,
                                    String licenseId,
                                    String machineFingerprintHash,
                                    String source) {
        if (!hasText(activationCode)) {
            throw new IllegalStateException("Activation code must not be blank");
        }

        ActivationState state = new ActivationState();
        state.setLicenseId(licenseId);
        state.setActivationCodeHash(cryptoService.sha256Hex(activationCode.trim()));
        state.setMachineFingerprintHash(machineFingerprintHash);
        state.setActivatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        state.setSource(hasText(source) ? source.trim() : "unknown");

        Path stateFile = resolveStateFilePath();
        try {
            if (stateFile.getParent() != null) {
                Files.createDirectories(stateFile.getParent());
            }
            String json = objectMapper.writeValueAsString(state);
            Files.writeString(
                    stateFile,
                    json,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE
            );
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write activation state file: " + stateFile, e);
        }
    }

    /**
     * Resolves the activation state file path.
     */
    public Path resolveStateFilePath() {
        String configuredStatePath = licenseProperties.getPrivateEdition().getActivationStateFilePath();
        if (hasText(configuredStatePath)) {
            return Path.of(configuredStatePath.trim()).toAbsolutePath().normalize();
        }

        String configuredLicensePath = licenseProperties.getPrivateEdition().getLicenseFilePath();
        if (hasText(configuredLicensePath)) {
            Path licensePath = Path.of(configuredLicensePath.trim()).toAbsolutePath().normalize();
            if (licensePath.getParent() != null) {
                return licensePath.getParent().resolve(DEFAULT_STATE_FILE_NAME).toAbsolutePath().normalize();
            }
        }

        return Path.of("license", DEFAULT_STATE_FILE_NAME).toAbsolutePath().normalize();
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
