package xenosoft.imldintelligence.module.identity.internal.repository;

import xenosoft.imldintelligence.module.identity.internal.model.SmsVerificationCode;

import java.time.OffsetDateTime;
import java.util.Optional;

public interface SmsVerificationCodeRepository {
    SmsVerificationCode save(SmsVerificationCode entity);

    Optional<SmsVerificationCode> findLatestByCondition(Long tenantId, String purpose, String mobileHash);

    Optional<SmsVerificationCode> findLatestPending(Long tenantId, String purpose, String mobileHash, OffsetDateTime now);

    void replacePendingCodes(Long tenantId, String purpose, String mobileHash, OffsetDateTime now);

    void incrementVerifyAttempt(Long tenantId, Long id, int nextAttemptCount, String status, OffsetDateTime now);

    void consume(Long tenantId, Long id, OffsetDateTime now);
}

