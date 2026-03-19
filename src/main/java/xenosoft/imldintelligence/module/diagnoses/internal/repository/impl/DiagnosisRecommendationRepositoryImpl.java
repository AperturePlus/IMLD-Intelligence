package xenosoft.imldintelligence.module.diagnoses.internal.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import xenosoft.imldintelligence.module.diagnoses.internal.model.DiagnosisRecommendation;
import xenosoft.imldintelligence.module.diagnoses.internal.repository.DiagnosisRecommendationRepository;
import xenosoft.imldintelligence.module.diagnoses.internal.repository.mybatis.DiagnosisRecommendationMapper;

import java.util.List;
import java.util.Optional;

/**
 * 诊断建议仓储实现类，基于 MyBatis-Plus 完成诊断建议的数据持久化。
 */
@Repository
@RequiredArgsConstructor
public class DiagnosisRecommendationRepositoryImpl implements DiagnosisRecommendationRepository {
    private final DiagnosisRecommendationMapper diagnosisRecommendationMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<DiagnosisRecommendation> findById(Long tenantId, Long id) {
        return Optional.ofNullable(diagnosisRecommendationMapper.selectOne(new LambdaQueryWrapper<DiagnosisRecommendation>()
                .eq(DiagnosisRecommendation::getTenantId, tenantId)
                .eq(DiagnosisRecommendation::getId, id)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DiagnosisRecommendation> listByTenantId(Long tenantId) {
        return diagnosisRecommendationMapper.selectList(new LambdaQueryWrapper<DiagnosisRecommendation>()
                .eq(DiagnosisRecommendation::getTenantId, tenantId)
                .orderByDesc(DiagnosisRecommendation::getId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DiagnosisRecommendation> listBySessionId(Long tenantId, Long sessionId) {
        return diagnosisRecommendationMapper.selectList(new LambdaQueryWrapper<DiagnosisRecommendation>()
                .eq(DiagnosisRecommendation::getTenantId, tenantId)
                .eq(DiagnosisRecommendation::getSessionId, sessionId)
                .orderByAsc(DiagnosisRecommendation::getPriority)
                .orderByDesc(DiagnosisRecommendation::getId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DiagnosisRecommendation save(DiagnosisRecommendation diagnosisRecommendation) {
        diagnosisRecommendationMapper.insert(diagnosisRecommendation);
        return diagnosisRecommendation;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DiagnosisRecommendation update(DiagnosisRecommendation diagnosisRecommendation) {
        diagnosisRecommendationMapper.update(diagnosisRecommendation, new LambdaUpdateWrapper<DiagnosisRecommendation>()
                .eq(DiagnosisRecommendation::getTenantId, diagnosisRecommendation.getTenantId())
                .eq(DiagnosisRecommendation::getId, diagnosisRecommendation.getId()));
        return diagnosisRecommendation;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean deleteById(Long tenantId, Long id) {
        return diagnosisRecommendationMapper.delete(new LambdaQueryWrapper<DiagnosisRecommendation>()
                .eq(DiagnosisRecommendation::getTenantId, tenantId)
                .eq(DiagnosisRecommendation::getId, id)) > 0;
    }
}
