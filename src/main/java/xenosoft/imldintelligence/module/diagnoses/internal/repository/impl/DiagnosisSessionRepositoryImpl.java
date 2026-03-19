package xenosoft.imldintelligence.module.diagnoses.internal.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import xenosoft.imldintelligence.module.diagnoses.internal.model.DiagnosisSession;
import xenosoft.imldintelligence.module.diagnoses.internal.repository.DiagnosisSessionRepository;
import xenosoft.imldintelligence.module.diagnoses.internal.repository.mybatis.DiagnosisSessionMapper;

import java.util.List;
import java.util.Optional;

/**
 * 诊断会话仓储实现类，基于 MyBatis-Plus 完成诊断会话的数据持久化。
 */
@Repository
@RequiredArgsConstructor
public class DiagnosisSessionRepositoryImpl implements DiagnosisSessionRepository {
    private final DiagnosisSessionMapper diagnosisSessionMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<DiagnosisSession> findById(Long tenantId, Long id) {
        return Optional.ofNullable(diagnosisSessionMapper.selectOne(new LambdaQueryWrapper<DiagnosisSession>()
                .eq(DiagnosisSession::getTenantId, tenantId)
                .eq(DiagnosisSession::getId, id)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DiagnosisSession> listByTenantId(Long tenantId) {
        return diagnosisSessionMapper.selectList(new LambdaQueryWrapper<DiagnosisSession>()
                .eq(DiagnosisSession::getTenantId, tenantId)
                .orderByDesc(DiagnosisSession::getId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DiagnosisSession> listByPatientId(Long tenantId, Long patientId) {
        return diagnosisSessionMapper.selectList(new LambdaQueryWrapper<DiagnosisSession>()
                .eq(DiagnosisSession::getTenantId, tenantId)
                .eq(DiagnosisSession::getPatientId, patientId)
                .orderByDesc(DiagnosisSession::getId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DiagnosisSession> listByEncounterId(Long tenantId, Long encounterId) {
        return diagnosisSessionMapper.selectList(new LambdaQueryWrapper<DiagnosisSession>()
                .eq(DiagnosisSession::getTenantId, tenantId)
                .eq(DiagnosisSession::getEncounterId, encounterId)
                .orderByDesc(DiagnosisSession::getId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DiagnosisSession save(DiagnosisSession diagnosisSession) {
        diagnosisSessionMapper.insert(diagnosisSession);
        return diagnosisSession;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DiagnosisSession update(DiagnosisSession diagnosisSession) {
        diagnosisSessionMapper.update(diagnosisSession, new LambdaUpdateWrapper<DiagnosisSession>()
                .eq(DiagnosisSession::getTenantId, diagnosisSession.getTenantId())
                .eq(DiagnosisSession::getId, diagnosisSession.getId()));
        return diagnosisSession;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean deleteById(Long tenantId, Long id) {
        return diagnosisSessionMapper.update(null, new LambdaUpdateWrapper<DiagnosisSession>()
                .eq(DiagnosisSession::getTenantId, tenantId)
                .eq(DiagnosisSession::getId, id)
                .set(DiagnosisSession::getStatus, "ARCHIVED")) > 0;
    }
}
