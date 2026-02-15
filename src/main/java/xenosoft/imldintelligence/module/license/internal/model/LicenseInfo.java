package xenosoft.imldintelligence.module.license.internal.model;

import lombok.Data;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Data
public class LicenseInfo {
    private String licenseId;
    private String hospitalId;
    private String deploymentMode;
    private String issuer;
    private OffsetDateTime issuedAt;
    private LocalDate supportStartDate;
    private LocalDate supportEndDate;
    private String machineFingerprintHash;
    private String activationCodeHash;
    private List<String> features;
    private List<String> scenarios;
}
