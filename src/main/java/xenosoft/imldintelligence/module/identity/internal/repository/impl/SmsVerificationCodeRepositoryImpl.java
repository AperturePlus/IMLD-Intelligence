package xenosoft.imldintelligence.module.identity.internal.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import xenosoft.imldintelligence.module.identity.internal.model.SmsVerificationCode;
import xenosoft.imldintelligence.module.identity.internal.repository.SmsVerificationCodeRepository;
import xenosoft.imldintelligence.module.identity.internal.repository.mybatis.SmsVerificationCodeMapper;

import java.time.OffsetDateTime;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SmsVerificationCodeRepositoryImpl implements SmsVerificationCodeRepository {

    private final SmsVerificationCodeMapper mapper;

    @Override
    public SmsVerificationCode save(SmsVerificationCode entity) {
        mapper.insert(entity);
        return entity;
    }

    @Override
    public Optional<SmsVerificationCode> findLatestByCondition(Long tenantId, String purpose, String mobileHash) {
        return Optional.ofNullable(mapper.selectOne(baseQuery(tenantId, purpose, mobileHash)
                .orderByDesc(SmsVerificationCode::getId)
                .last("LIMIT 1")));
    }

    @Override
    public Optional<SmsVerificationCode> findLatestPending(Long tenantId, String purpose, String mobileHash, OffsetDateTime now) {
        return Optional.ofNullable(mapper.selectOne(baseQuery(tenantId, purpose, mobileHash)
                .eq(SmsVerificationCode::getStatus, "PENDING")
                .gt(SmsVerificationCode::getExpiresAt, now)
                .orderByDesc(SmsVerificationCode::getId)
                .last("LIMIT 1")));
    }

    @Override
    public void replacePendingCodes(Long tenantId, String purpose, String mobileHash, OffsetDateTime now) {
        mapper.update(null, new LambdaUpdateWrapper<SmsVerificationCode>()
                .eq(SmsVerificationCode::getTenantId, tenantId)
                .eq(SmsVerificationCode::getPurpose, purpose)
                .eq(SmsVerificationCode::getMobileHash, mobileHash)
                .eq(SmsVerificationCode::getStatus, "PENDING")
                .gt(SmsVerificationCode::getExpiresAt, now)
                .set(SmsVerificationCode::getStatus, "REPLACED")
                .set(SmsVerificationCode::getConsumedAt, now)
                .set(SmsVerificationCode::getUpdatedAt, now));
    }

    @Override
    public void incrementVerifyAttempt(Long tenantId, Long id, int nextAttemptCount, String status, OffsetDateTime now) {
        mapper.update(null, new LambdaUpdateWrapper<SmsVerificationCode>()
                .eq(SmsVerificationCode::getTenantId, tenantId)
                .eq(SmsVerificationCode::getId, id)
                .set(SmsVerificationCode::getVerifyAttemptCount, nextAttemptCount)
                .set(SmsVerificationCode::getStatus, status)
                .set("LOCKED".equals(status), SmsVerificationCode::getConsumedAt, now)
                .set(SmsVerificationCode::getUpdatedAt, now));
    }

    @Override
    public void consume(Long tenantId, Long id, OffsetDateTime now) {
        mapper.update(null, new LambdaUpdateWrapper<SmsVerificationCode>()
                .eq(SmsVerificationCode::getTenantId, tenantId)
                .eq(SmsVerificationCode::getId, id)
                .set(SmsVerificationCode::getStatus, "CONSUMED")
                .set(SmsVerificationCode::getConsumedAt, now)
                .set(SmsVerificationCode::getUpdatedAt, now));
    }

    private LambdaQueryWrapper<SmsVerificationCode> baseQuery(Long tenantId, String purpose, String mobileHash) {
        return new LambdaQueryWrapper<SmsVerificationCode>()
                .eq(SmsVerificationCode::getTenantId, tenantId)
                .eq(SmsVerificationCode::getPurpose, purpose)
                .eq(SmsVerificationCode::getMobileHash, mobileHash);
    }
}

