package xenosoft.imldintelligence.module.screening.internal.repository.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import xenosoft.imldintelligence.module.screening.internal.repository.TocClinicalTransferRepository;
import xenosoft.imldintelligence.module.screening.internal.repository.mybatis.TocClinicalTransferMapper;
import xenosoft.imldintelligence.module.screening.model.TocClinicalTransfer;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TocClinicalTransferRepositoryImpl implements TocClinicalTransferRepository {
    private final TocClinicalTransferMapper tocClinicalTransferMapper;

    @Override
    public Optional<TocClinicalTransfer> findById(Long tenantId, Long id) {
        return Optional.ofNullable(tocClinicalTransferMapper.findById(tenantId, id));
    }

    @Override
    public List<TocClinicalTransfer> listByTenantId(Long tenantId) {
        return tocClinicalTransferMapper.listByTenantId(tenantId);
    }

    @Override
    public List<TocClinicalTransfer> listByResponseId(Long tenantId, Long responseId) {
        return tocClinicalTransferMapper.listByResponseId(tenantId, responseId);
    }

    @Override
    public List<TocClinicalTransfer> listByPatientId(Long tenantId, Long patientId) {
        return tocClinicalTransferMapper.listByPatientId(tenantId, patientId);
    }

    @Override
    public TocClinicalTransfer save(TocClinicalTransfer tocClinicalTransfer) {
        tocClinicalTransferMapper.insert(tocClinicalTransfer);
        return tocClinicalTransfer;
    }

    @Override
    public TocClinicalTransfer update(TocClinicalTransfer tocClinicalTransfer) {
        tocClinicalTransferMapper.update(tocClinicalTransfer);
        return tocClinicalTransfer;
    }

    @Override
    public Boolean deleteById(Long tenantId, Long id) {
        return tocClinicalTransferMapper.deleteById(tenantId, id) > 0;
    }
}

