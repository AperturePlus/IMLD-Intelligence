package xenosoft.imldintelligence.module.screening.internal.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import xenosoft.imldintelligence.module.screening.internal.model.QuestionnaireQuestion;
import xenosoft.imldintelligence.module.screening.internal.repository.QuestionnaireQuestionRepository;
import xenosoft.imldintelligence.module.screening.internal.repository.mybatis.QuestionnaireQuestionMapper;

import java.util.List;
import java.util.Optional;

/**
 * 问卷题目仓储实现类，基于 MyBatis-Plus 完成问卷题目的数据持久化。
 */
@Repository
@RequiredArgsConstructor
public class QuestionnaireQuestionRepositoryImpl implements QuestionnaireQuestionRepository {
    private final QuestionnaireQuestionMapper questionnaireQuestionMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<QuestionnaireQuestion> findById(Long tenantId, Long id) {
        return Optional.ofNullable(questionnaireQuestionMapper.selectOne(new LambdaQueryWrapper<QuestionnaireQuestion>()
                .eq(QuestionnaireQuestion::getTenantId, tenantId)
                .eq(QuestionnaireQuestion::getId, id)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<QuestionnaireQuestion> findByQuestionnaireIdAndQuestionNo(Long tenantId, Long questionnaireId, String questionNo) {
        return Optional.ofNullable(questionnaireQuestionMapper.selectOne(new LambdaQueryWrapper<QuestionnaireQuestion>()
                .eq(QuestionnaireQuestion::getTenantId, tenantId)
                .eq(QuestionnaireQuestion::getQuestionnaireId, questionnaireId)
                .eq(QuestionnaireQuestion::getQuestionNo, questionNo)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<QuestionnaireQuestion> listByTenantId(Long tenantId) {
        return questionnaireQuestionMapper.selectList(new LambdaQueryWrapper<QuestionnaireQuestion>()
                .eq(QuestionnaireQuestion::getTenantId, tenantId)
                .orderByDesc(QuestionnaireQuestion::getId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<QuestionnaireQuestion> listByQuestionnaireId(Long tenantId, Long questionnaireId) {
        return questionnaireQuestionMapper.selectList(new LambdaQueryWrapper<QuestionnaireQuestion>()
                .eq(QuestionnaireQuestion::getTenantId, tenantId)
                .eq(QuestionnaireQuestion::getQuestionnaireId, questionnaireId)
                .orderByAsc(QuestionnaireQuestion::getSortOrder)
                .orderByDesc(QuestionnaireQuestion::getId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QuestionnaireQuestion save(QuestionnaireQuestion questionnaireQuestion) {
        questionnaireQuestionMapper.insert(questionnaireQuestion);
        return questionnaireQuestion;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QuestionnaireQuestion update(QuestionnaireQuestion questionnaireQuestion) {
        questionnaireQuestionMapper.update(questionnaireQuestion, new LambdaUpdateWrapper<QuestionnaireQuestion>()
                .eq(QuestionnaireQuestion::getTenantId, questionnaireQuestion.getTenantId())
                .eq(QuestionnaireQuestion::getId, questionnaireQuestion.getId()));
        return questionnaireQuestion;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean deleteById(Long tenantId, Long id) {
        return questionnaireQuestionMapper.delete(new LambdaQueryWrapper<QuestionnaireQuestion>()
                .eq(QuestionnaireQuestion::getTenantId, tenantId)
                .eq(QuestionnaireQuestion::getId, id)) > 0;
    }
}
