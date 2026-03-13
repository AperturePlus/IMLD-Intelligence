package xenosoft.imldintelligence.module.identity.internal.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import xenosoft.imldintelligence.module.identity.api.dto.IdentityApiDtos;
import xenosoft.imldintelligence.module.identity.internal.model.ConsentRecord;
import xenosoft.imldintelligence.module.identity.internal.repository.ConsentRecordRepository;
import xenosoft.imldintelligence.module.identity.internal.service.ConsentRecordService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConsentRecordServiceImpl implements ConsentRecordService {

    private final ConsentRecordRepository consentRecordRepository;

    @Override
    public long countConsents(Long tenantId, IdentityApiDtos.Query.ConsentRecordPageQuery query) {
        return consentRecordRepository.countByCondition(tenantId,
                query.patientId(), query.consentType(), query.status());
    }

    @Override
    public List<ConsentRecord> listConsents(Long tenantId,
                                             IdentityApiDtos.Query.ConsentRecordPageQuery query,
                                             long offset, int limit) {
        return consentRecordRepository.listByCondition(tenantId,
                query.patientId(), query.consentType(), query.status(),
                offset, limit);
    }

    @Override
    public ConsentRecord upsertConsent(Long tenantId,
                                        IdentityApiDtos.Request.UpsertConsentRecordRequest request) {
        ConsentRecord record = new ConsentRecord();
        record.setTenantId(tenantId);
        record.setPatientId(request.patientId());
        record.setTocUserId(request.tocUserId());
        record.setConsentType(request.consentType());
        record.setConsentVersion(request.consentVersion());
        record.setSignedOffline(request.signedOffline() != null ? request.signedOffline() : true);
        record.setSignedAt(request.signedAt());
        record.setFileId(request.fileId());
        record.setStatus(request.status());
        record.setRemark(request.remark());

        return consentRecordRepository.save(record);
    }
}
