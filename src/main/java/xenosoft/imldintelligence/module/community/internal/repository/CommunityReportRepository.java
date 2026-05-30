package xenosoft.imldintelligence.module.community.internal.repository;

import xenosoft.imldintelligence.module.community.internal.model.CommunityContentReport;
import xenosoft.imldintelligence.module.community.internal.repository.mybatis.CommunityReportPageRow;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface CommunityReportRepository {
    Optional<CommunityContentReport> findById(Long tenantId, Long reportId);

    List<CommunityReportPageRow> listByConditionWithTotal(Long tenantId,
                                                          String status,
                                                          OffsetDateTime createdFrom,
                                                          OffsetDateTime createdTo,
                                                          long offset,
                                                          int limit);

    CommunityContentReport save(CommunityContentReport report);

    boolean updateModeration(Long tenantId,
                             Long reportId,
                             String status,
                             String resultAction,
                             String resultNote,
                             Long handledBy);

    boolean updateModerationIfStatus(Long tenantId,
                                     Long reportId,
                                     String expectedStatus,
                                     String status,
                                     String resultAction,
                                     String resultNote,
                                     Long handledBy);
}

