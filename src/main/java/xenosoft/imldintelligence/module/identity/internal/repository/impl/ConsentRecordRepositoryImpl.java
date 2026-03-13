package xenosoft.imldintelligence.module.identity.internal.repository.impl;

import xenosoft.imldintelligence.module.identity.internal.repository.ConsentRecordRepository;
import xenosoft.imldintelligence.module.identity.internal.repository.mybatis.ConsentRecordMapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import xenosoft.imldintelligence.module.identity.internal.model.ConsentRecord;

import java.util.List;
import java.util.Optional;

/**
 * 知情同意记录仓储实现类，基于 MyBatis Mapper 完成知情同意记录的数据持久化。
 */
@Repository
@RequiredArgsConstructor
public class ConsentRecordRepositoryImpl implements ConsentRecordRepository {
    private final ConsentRecordMapper consentRecordMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<ConsentRecord> findById(Long tenantId, Long id) {
        return Optional.ofNullable(consentRecordMapper.findById(tenantId, id));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ConsentRecord> listByTenantId(Long tenantId) {
        return consentRecordMapper.listByTenantId(tenantId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ConsentRecord> listByPatientId(Long tenantId, Long patientId) {
        return consentRecordMapper.listByPatientId(tenantId, patientId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ConsentRecord> listByTocUserId(Long tenantId, Long tocUserId) {
        return consentRecordMapper.listByTocUserId(tenantId, tocUserId);
    }

    @Override
    public long countByCondition(Long tenantId, Long patientId, String consentType, String status) {
        return consentRecordMapper.countByCondition(tenantId, patientId, consentType, status);
    }

    @Override
    public List<ConsentRecord> listByCondition(Long tenantId, Long patientId, String consentType,
                                                String status, long offset, int limit) {
        return consentRecordMapper.listByCondition(tenantId, patientId, consentType, status, offset, limit);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConsentRecord save(ConsentRecord consentRecord) {
        consentRecordMapper.insert(consentRecord);
        return consentRecord;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConsentRecord update(ConsentRecord consentRecord) {
        consentRecordMapper.update(consentRecord);
        return consentRecord;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean deleteById(Long tenantId, Long id) {
        return consentRecordMapper.deleteById(tenantId, id) > 0;
    }
}
