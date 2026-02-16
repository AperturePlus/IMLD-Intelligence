package xenosoft.imldintelligence.module.clinical.internal.repository.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import xenosoft.imldintelligence.module.clinical.internal.model.ClinicalHistoryEntry;
import xenosoft.imldintelligence.module.clinical.internal.repository.ClinicalHistoryEntryRepository;
import xenosoft.imldintelligence.module.clinical.internal.repository.mybatis.ClinicalHistoryEntryMapper;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ClinicalHistoryEntryRepositoryImpl implements ClinicalHistoryEntryRepository {
    private final ClinicalHistoryEntryMapper clinicalHistoryEntryMapper;

    @Override
    public Optional<ClinicalHistoryEntry> findById(Long tenantId, Long id) {
        return Optional.ofNullable(clinicalHistoryEntryMapper.findById(tenantId, id));
    }

    @Override
    public List<ClinicalHistoryEntry> listByTenantId(Long tenantId) {
        return clinicalHistoryEntryMapper.listByTenantId(tenantId);
    }

    @Override
    public List<ClinicalHistoryEntry> listByPatientId(Long tenantId, Long patientId) {
        return clinicalHistoryEntryMapper.listByPatientId(tenantId, patientId);
    }

    @Override
    public List<ClinicalHistoryEntry> listByEncounterId(Long tenantId, Long encounterId) {
        return clinicalHistoryEntryMapper.listByEncounterId(tenantId, encounterId);
    }

    @Override
    public ClinicalHistoryEntry save(ClinicalHistoryEntry clinicalHistoryEntry) {
        clinicalHistoryEntryMapper.insert(clinicalHistoryEntry);
        return clinicalHistoryEntry;
    }

    @Override
    public ClinicalHistoryEntry update(ClinicalHistoryEntry clinicalHistoryEntry) {
        clinicalHistoryEntryMapper.update(clinicalHistoryEntry);
        return clinicalHistoryEntry;
    }

    @Override
    public Boolean deleteById(Long tenantId, Long id) {
        return clinicalHistoryEntryMapper.deleteById(tenantId, id) > 0;
    }
}

