package xenosoft.imldintelligence.module.identity.internal.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import xenosoft.imldintelligence.module.identity.internal.model.EmailVerificationCode;
import xenosoft.imldintelligence.module.identity.internal.repository.EmailVerificationCodeRepository;
import xenosoft.imldintelligence.module.identity.internal.repository.mybatis.EmailVerificationCodeMapper;

import java.time.OffsetDateTime;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class EmailVerificationCodeRepositoryImpl implements EmailVerificationCodeRepository {
    private final EmailVerificationCodeMapper mapper;

    @Override
    public EmailVerificationCode save(EmailVerificationCode entity) {
        mapper.insert(entity);
        return entity;
    }

    @Override
    public Optional<EmailVerificationCode> findLatestByCondition(Long tenantId, String purpose, String email, String username) {
        return Optional.ofNullable(mapper.selectOne(baseQuery(tenantId, purpose, email, username)
                .orderByDesc(EmailVerificationCode::getId)
                .last("LIMIT 1")));
    }

    @Override
    public Optional<EmailVerificationCode> findLatestPending(Long tenantId, String purpose, String email, String username, OffsetDateTime now) {
        return Optional.ofNullable(mapper.selectOne(baseQuery(tenantId, purpose, email, username)
                .eq(EmailVerificationCode::getStatus, "PENDING")
                .gt(EmailVerificationCode::getExpiresAt, now)
                .orderByDesc(EmailVerificationCode::getId)
                .last("LIMIT 1")));
    }

    @Override
    public void replacePendingCodes(Long tenantId, String purpose, String email, String username, OffsetDateTime now) {
        mapper.update(null, new LambdaUpdateWrapper<EmailVerificationCode>()
                .eq(EmailVerificationCode::getTenantId, tenantId)
                .eq(EmailVerificationCode::getPurpose, purpose)
                .eq(EmailVerificationCode::getEmail, email)
                .eq(EmailVerificationCode::getUsername, username)
                .eq(EmailVerificationCode::getStatus, "PENDING")
                .set(EmailVerificationCode::getStatus, "REPLACED")
                .set(EmailVerificationCode::getConsumedAt, now)
                .set(EmailVerificationCode::getUpdatedAt, now));
    }

    @Override
    public void incrementVerifyAttempt(Long tenantId, Long id, int nextAttemptCount, String status, OffsetDateTime now) {
        mapper.update(null, new LambdaUpdateWrapper<EmailVerificationCode>()
                .eq(EmailVerificationCode::getTenantId, tenantId)
                .eq(EmailVerificationCode::getId, id)
                .set(EmailVerificationCode::getVerifyAttemptCount, nextAttemptCount)
                .set(EmailVerificationCode::getStatus, status)
                .set("LOCKED".equals(status), EmailVerificationCode::getConsumedAt, now)
                .set(EmailVerificationCode::getUpdatedAt, now));
    }

    @Override
    public boolean consumePendingCode(Long tenantId, Long id, OffsetDateTime now) {
        return mapper.update(null, new LambdaUpdateWrapper<EmailVerificationCode>()
                .eq(EmailVerificationCode::getTenantId, tenantId)
                .eq(EmailVerificationCode::getId, id)
                .eq(EmailVerificationCode::getStatus, "PENDING")
                .gt(EmailVerificationCode::getExpiresAt, now)
                .set(EmailVerificationCode::getStatus, "CONSUMED")
                .set(EmailVerificationCode::getConsumedAt, now)
                .set(EmailVerificationCode::getUpdatedAt, now)) > 0;
    }

    private LambdaQueryWrapper<EmailVerificationCode> baseQuery(Long tenantId, String purpose, String email, String username) {
        return new LambdaQueryWrapper<EmailVerificationCode>()
                .eq(EmailVerificationCode::getTenantId, tenantId)
                .eq(EmailVerificationCode::getPurpose, purpose)
                .eq(EmailVerificationCode::getEmail, email)
                .eq(EmailVerificationCode::getUsername, username);
    }
}
