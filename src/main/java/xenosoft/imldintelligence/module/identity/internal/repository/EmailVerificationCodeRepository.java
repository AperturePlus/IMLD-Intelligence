package xenosoft.imldintelligence.module.identity.internal.repository;

import xenosoft.imldintelligence.module.identity.internal.model.EmailVerificationCode;

import java.time.OffsetDateTime;
import java.util.Optional;

public interface EmailVerificationCodeRepository {
    EmailVerificationCode save(EmailVerificationCode entity);

    Optional<EmailVerificationCode> findLatestByCondition(Long tenantId, String purpose, String email, String username);

    Optional<EmailVerificationCode> findLatestPending(Long tenantId, String purpose, String email, String username, OffsetDateTime now);

    void replacePendingCodes(Long tenantId, String purpose, String email, String username, OffsetDateTime now);

    void incrementVerifyAttempt(Long tenantId, Long id, int nextAttemptCount, String status, OffsetDateTime now);

    boolean consumePendingCode(Long tenantId, Long id, OffsetDateTime now);
}
