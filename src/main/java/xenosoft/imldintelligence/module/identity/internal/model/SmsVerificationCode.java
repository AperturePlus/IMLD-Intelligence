package xenosoft.imldintelligence.module.identity.internal.model;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class SmsVerificationCode {
    private Long id;
    private Long tenantId;
    private String purpose;
    private String mobileHash;
    private String mobileEncrypted;
    private String codeHash;
    private Integer verifyAttemptCount;
    private Integer maxVerifyAttempts;
    private String status;
    private OffsetDateTime expiresAt;
    private OffsetDateTime consumedAt;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}

