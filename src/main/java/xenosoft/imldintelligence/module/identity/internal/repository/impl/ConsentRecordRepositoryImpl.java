package xenosoft.imldintelligence.module.identity.internal.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import xenosoft.imldintelligence.module.identity.internal.model.ConsentRecord;
import xenosoft.imldintelligence.module.identity.internal.repository.ConsentRecordRepository;
import xenosoft.imldintelligence.module.identity.internal.repository.mybatis.ConsentRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 知情同意记录仓储实现类，基于 MyBatis-Plus 完成知情同意记录的数据持久化。
 */
@Repository
@RequiredArgsConstructor
public class ConsentRecordRepositoryImpl implements ConsentRecordRepository {
    private final ConsentRecordMapper consentRecordMapper;

    @Override
    public Optional<ConsentRecord> findById(Long tenantId, Long id) {
        return Optional.ofNullable(consentRecordMapper.selectOne(new LambdaQueryWrapper<ConsentRecord>()
                .eq(ConsentRecord::getTenantId, tenantId)
                .eq(ConsentRecord::getId, id)));
    }

    @Override
    public List<ConsentRecord> listByTenantId(Long tenantId) {
        return consentRecordMapper.selectList(new LambdaQueryWrapper<ConsentRecord>()
                .eq(ConsentRecord::getTenantId, tenantId)
                .orderByDesc(ConsentRecord::getId));
    }

    @Override
    public List<ConsentRecord> listByPatientId(Long tenantId, Long patientId) {
        return consentRecordMapper.selectList(new LambdaQueryWrapper<ConsentRecord>()
                .eq(ConsentRecord::getTenantId, tenantId)
                .eq(ConsentRecord::getPatientId, patientId)
                .orderByDesc(ConsentRecord::getId));
    }

    @Override
    public List<ConsentRecord> listByTocUserId(Long tenantId, Long tocUserId) {
        return consentRecordMapper.selectList(new LambdaQueryWrapper<ConsentRecord>()
                .eq(ConsentRecord::getTenantId, tenantId)
                .eq(ConsentRecord::getTocUserId, tocUserId)
                .orderByDesc(ConsentRecord::getId));
    }

    @Override
    public long countByCondition(Long tenantId, Long patientId, String consentType, String status) {
        return consentRecordMapper.selectCount(buildConditionWrapper(tenantId, patientId, consentType, status));
    }

    @Override
    public List<ConsentRecord> listByCondition(Long tenantId, Long patientId, String consentType,
                                               String status, long offset, int limit) {
        return consentRecordMapper.selectList(buildConditionWrapper(tenantId, patientId, consentType, status)
                .orderByDesc(ConsentRecord::getId)
                .last("LIMIT " + limit + " OFFSET " + offset));
    }

    @Override
    public ConsentRecord save(ConsentRecord consentRecord) {
        consentRecordMapper.insert(consentRecord);
        return consentRecord;
    }

    @Override
    public ConsentRecord update(ConsentRecord consentRecord) {
        consentRecordMapper.update(null, new LambdaUpdateWrapper<ConsentRecord>()
                .eq(ConsentRecord::getTenantId, consentRecord.getTenantId())
                .eq(ConsentRecord::getId, consentRecord.getId())
                .set(ConsentRecord::getPatientId, consentRecord.getPatientId())
                .set(ConsentRecord::getTocUserId, consentRecord.getTocUserId())
                .set(ConsentRecord::getConsentType, consentRecord.getConsentType())
                .set(ConsentRecord::getConsentVersion, consentRecord.getConsentVersion())
                .set(ConsentRecord::getSignedOffline, consentRecord.getSignedOffline())
                .set(ConsentRecord::getSignedAt, consentRecord.getSignedAt())
                .set(ConsentRecord::getFileId, consentRecord.getFileId())
                .set(ConsentRecord::getStatus, consentRecord.getStatus())
                .set(ConsentRecord::getRemark, consentRecord.getRemark()));
        return consentRecord;
    }

    @Override
    public Boolean deleteById(Long tenantId, Long id) {
        return consentRecordMapper.update(null, new LambdaUpdateWrapper<ConsentRecord>()
                .eq(ConsentRecord::getTenantId, tenantId)
                .eq(ConsentRecord::getId, id)
                .set(ConsentRecord::getStatus, "REVOKED")) > 0;
    }

    private LambdaQueryWrapper<ConsentRecord> buildConditionWrapper(Long tenantId, Long patientId,
                                                                    String consentType, String status) {
        return new LambdaQueryWrapper<ConsentRecord>()
                .eq(ConsentRecord::getTenantId, tenantId)
                .eq(patientId != null, ConsentRecord::getPatientId, patientId)
                .eq(consentType != null && !consentType.isBlank(), ConsentRecord::getConsentType, consentType)
                .eq(status != null && !status.isBlank(), ConsentRecord::getStatus, status);
    }
}
