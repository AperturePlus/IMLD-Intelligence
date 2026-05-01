package xenosoft.imldintelligence.module.community.internal.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import xenosoft.imldintelligence.module.community.internal.model.CommunityContentReport;
import xenosoft.imldintelligence.module.community.internal.repository.CommunityReportRepository;
import xenosoft.imldintelligence.module.community.internal.repository.mybatis.CommunityContentReportMapper;
import xenosoft.imldintelligence.module.community.internal.repository.mybatis.CommunityReportPageRow;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CommunityReportRepositoryImpl implements CommunityReportRepository {
    private final CommunityContentReportMapper communityContentReportMapper;

    @Override
    public Optional<CommunityContentReport> findById(Long tenantId, Long reportId) {
        return Optional.ofNullable(communityContentReportMapper.selectOne(new LambdaQueryWrapper<CommunityContentReport>()
                .eq(CommunityContentReport::getTenantId, tenantId)
                .eq(CommunityContentReport::getId, reportId)));
    }

    @Override
    public List<CommunityReportPageRow> listByConditionWithTotal(Long tenantId,
                                                                String status,
                                                                OffsetDateTime createdFrom,
                                                                OffsetDateTime createdTo,
                                                                long offset,
                                                                int limit) {
        return communityContentReportMapper.listByConditionWithTotal(tenantId, status, createdFrom, createdTo, offset, limit);
    }

    @Override
    public CommunityContentReport save(CommunityContentReport report) {
        communityContentReportMapper.insert(report);
        return report;
    }

    @Override
    public boolean updateModeration(Long tenantId,
                                    Long reportId,
                                    String status,
                                    String resultAction,
                                    String resultNote,
                                    Long handledBy) {
        return communityContentReportMapper.updateModeration(tenantId, reportId, status, resultAction, resultNote, handledBy) > 0;
    }
}

