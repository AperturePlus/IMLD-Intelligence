package xenosoft.imldintelligence.module.license.internal.model;

import lombok.Data;

import java.time.OffsetDateTime;

/**
 * Persisted activation state for private deployments.
 *
 * <p>The file stores only activation hash metadata and must never contain a plain activation code.</p>
 */
@Data
public class ActivationState {
    private String licenseId;
    private String activationCodeHash;
    private String machineFingerprintHash;
    private OffsetDateTime activatedAt;
    private String source;
}
