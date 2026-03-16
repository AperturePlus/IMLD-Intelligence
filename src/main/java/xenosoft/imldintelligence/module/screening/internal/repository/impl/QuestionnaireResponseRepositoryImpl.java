package xenosoft.imldintelligence.module.screening.internal.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import xenosoft.imldintelligence.module.screening.internal.model.QuestionnaireResponse;
import xenosoft.imldintelligence.module.screening.internal.repository.QuestionnaireResponseRepository;
import xenosoft.imldintelligence.module.screening.internal.repository.mybatis.QuestionnaireResponseMapper;

import java.util.List;
import java.util.Optional;

/**
 * 问卷应答仓储实现类，基于 MyBatis-Plus 完成问卷应答的数据持久化。
 */
@Repository
@RequiredArgsConstructor
public class QuestionnaireResponseRepositoryImpl implements QuestionnaireResponseRepository {
    private final QuestionnaireResponseMapper questionnaireResponseMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<QuestionnaireResponse> findById(Long tenantId, Long id) {
        return Optional.ofNullable(questionnaireResponseMapper.selectOne(new LambdaQueryWrapper<QuestionnaireResponse>()
                .eq(QuestionnaireResponse::getTenantId, tenantId)
                .eq(QuestionnaireResponse::getId, id)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<QuestionnaireResponse> findByResponseNo(Long tenantId, String responseNo) {
        return Optional.ofNullable(questionnaireResponseMapper.selectOne(new LambdaQueryWrapper<QuestionnaireResponse>()
                .eq(QuestionnaireResponse::getTenantId, tenantId)
                .eq(QuestionnaireResponse::getResponseNo, responseNo)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<QuestionnaireResponse> listByTenantId(Long tenantId) {
        return questionnaireResponseMapper.selectList(new LambdaQueryWrapper<QuestionnaireResponse>()
                .eq(QuestionnaireResponse::getTenantId, tenantId)
                .orderByDesc(QuestionnaireResponse::getId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<QuestionnaireResponse> listByQuestionnaireId(Long tenantId, Long questionnaireId) {
        return questionnaireResponseMapper.selectList(new LambdaQueryWrapper<QuestionnaireResponse>()
                .eq(QuestionnaireResponse::getTenantId, tenantId)
                .eq(QuestionnaireResponse::getQuestionnaireId, questionnaireId)
                .orderByDesc(QuestionnaireResponse::getId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<QuestionnaireResponse> listByTocUserId(Long tenantId, Long tocUserId) {
        return questionnaireResponseMapper.selectList(new LambdaQueryWrapper<QuestionnaireResponse>()
                .eq(QuestionnaireResponse::getTenantId, tenantId)
                .eq(QuestionnaireResponse::getTocUserId, tocUserId)
                .orderByDesc(QuestionnaireResponse::getId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QuestionnaireResponse save(QuestionnaireResponse questionnaireResponse) {
        questionnaireResponseMapper.insert(questionnaireResponse);
        return questionnaireResponse;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QuestionnaireResponse update(QuestionnaireResponse questionnaireResponse) {
        questionnaireResponseMapper.update(questionnaireResponse, new LambdaUpdateWrapper<QuestionnaireResponse>()
                .eq(QuestionnaireResponse::getTenantId, questionnaireResponse.getTenantId())
                .eq(QuestionnaireResponse::getId, questionnaireResponse.getId()));
        return questionnaireResponse;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean deleteById(Long tenantId, Long id) {
        return questionnaireResponseMapper.delete(new LambdaQueryWrapper<QuestionnaireResponse>()
                .eq(QuestionnaireResponse::getTenantId, tenantId)
                .eq(QuestionnaireResponse::getId, id)) > 0;
    }
}
