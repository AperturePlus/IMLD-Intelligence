package xenosoft.imldintelligence.module.identity.internal.service;

import xenosoft.imldintelligence.module.identity.api.dto.IdentityApiDtos;
import xenosoft.imldintelligence.module.identity.internal.model.ConsentRecord;

import java.util.List;

public interface ConsentRecordService {

    long countConsents(Long tenantId, IdentityApiDtos.Query.ConsentRecordPageQuery query);

    List<ConsentRecord> listConsents(Long tenantId, IdentityApiDtos.Query.ConsentRecordPageQuery query,
                                     long offset, int limit);

    ConsentRecord upsertConsent(Long tenantId, IdentityApiDtos.Request.UpsertConsentRecordRequest request);
}
