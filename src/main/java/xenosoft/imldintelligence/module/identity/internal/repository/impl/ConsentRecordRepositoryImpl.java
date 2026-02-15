package xenosoft.imldintelligence.module.identity.internal.repository.impl;

import xenosoft.imldintelligence.module.identity.internal.repository.ConsentRecordRepository;
import xenosoft.imldintelligence.module.identity.internal.repository.mybatis.ConsentRecordMapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import xenosoft.imldintelligence.module.identity.internal.model.ConsentRecord;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ConsentRecordRepositoryImpl implements ConsentRecordRepository {
    private final ConsentRecordMapper consentRecordMapper;

    @Override
    public Optional<ConsentRecord> findById(Long tenantId, Long id) {
        return Optional.ofNullable(consentRecordMapper.findById(tenantId, id));
    }

    @Override
    public List<ConsentRecord> listByTenantId(Long tenantId) {
        return consentRecordMapper.listByTenantId(tenantId);
    }

    @Override
    public List<ConsentRecord> listByPatientId(Long tenantId, Long patientId) {
        return consentRecordMapper.listByPatientId(tenantId, patientId);
    }

    @Override
    public List<ConsentRecord> listByTocUserId(Long tenantId, Long tocUserId) {
        return consentRecordMapper.listByTocUserId(tenantId, tocUserId);
    }

    @Override
    public ConsentRecord save(ConsentRecord consentRecord) {
        consentRecordMapper.insert(consentRecord);
        return consentRecord;
    }

    @Override
    public ConsentRecord update(ConsentRecord consentRecord) {
        consentRecordMapper.update(consentRecord);
        return consentRecord;
    }

    @Override
    public Boolean deleteById(Long tenantId, Long id) {
        return consentRecordMapper.deleteById(tenantId, id) > 0;
    }
}
