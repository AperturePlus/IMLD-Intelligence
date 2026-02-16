package xenosoft.imldintelligence.module.clinical.internal.repository.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import xenosoft.imldintelligence.module.clinical.internal.model.LabResult;
import xenosoft.imldintelligence.module.clinical.internal.repository.LabResultRepository;
import xenosoft.imldintelligence.module.clinical.internal.repository.mybatis.LabResultMapper;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class LabResultRepositoryImpl implements LabResultRepository {
    private final LabResultMapper labResultMapper;

    @Override
    public Optional<LabResult> findById(Long tenantId, Long id) {
        return Optional.ofNullable(labResultMapper.findById(tenantId, id));
    }

    @Override
    public List<LabResult> listByTenantId(Long tenantId) {
        return labResultMapper.listByTenantId(tenantId);
    }

    @Override
    public List<LabResult> listByPatientId(Long tenantId, Long patientId) {
        return labResultMapper.listByPatientId(tenantId, patientId);
    }

    @Override
    public List<LabResult> listByEncounterId(Long tenantId, Long encounterId) {
        return labResultMapper.listByEncounterId(tenantId, encounterId);
    }

    @Override
    public LabResult save(LabResult labResult) {
        labResultMapper.insert(labResult);
        return labResult;
    }

    @Override
    public LabResult update(LabResult labResult) {
        labResultMapper.update(labResult);
        return labResult;
    }

    @Override
    public Boolean deleteById(Long tenantId, Long id) {
        return labResultMapper.deleteById(tenantId, id) > 0;
    }
}

