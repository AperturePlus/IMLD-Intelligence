package xenosoft.imldintelligence.module.screening.internal.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import xenosoft.imldintelligence.module.screening.internal.model.Questionnaire;
import xenosoft.imldintelligence.module.screening.internal.repository.QuestionnaireRepository;
import xenosoft.imldintelligence.module.screening.internal.repository.mybatis.QuestionnaireMapper;

import java.util.List;
import java.util.Optional;

/**
 * 问卷仓储实现类，基于 MyBatis-Plus 完成问卷的数据持久化。
 */
@Repository
@RequiredArgsConstructor
public class QuestionnaireRepositoryImpl implements QuestionnaireRepository {
    private final QuestionnaireMapper questionnaireMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Questionnaire> findById(Long tenantId, Long id) {
        return Optional.ofNullable(questionnaireMapper.selectOne(new LambdaQueryWrapper<Questionnaire>()
                .eq(Questionnaire::getTenantId, tenantId)
                .eq(Questionnaire::getId, id)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Questionnaire> findByQuestionnaireCodeAndVersionNo(Long tenantId, String questionnaireCode, Integer versionNo) {
        return Optional.ofNullable(questionnaireMapper.selectOne(new LambdaQueryWrapper<Questionnaire>()
                .eq(Questionnaire::getTenantId, tenantId)
                .eq(Questionnaire::getQuestionnaireCode, questionnaireCode)
                .eq(Questionnaire::getVersionNo, versionNo)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Questionnaire> listByTenantId(Long tenantId) {
        return questionnaireMapper.selectList(new LambdaQueryWrapper<Questionnaire>()
                .eq(Questionnaire::getTenantId, tenantId)
                .orderByDesc(Questionnaire::getId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Questionnaire save(Questionnaire questionnaire) {
        questionnaireMapper.insert(questionnaire);
        return questionnaire;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Questionnaire update(Questionnaire questionnaire) {
        questionnaireMapper.update(questionnaire, new LambdaUpdateWrapper<Questionnaire>()
                .eq(Questionnaire::getTenantId, questionnaire.getTenantId())
                .eq(Questionnaire::getId, questionnaire.getId()));
        return questionnaire;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean deleteById(Long tenantId, Long id) {
        return questionnaireMapper.update(null, new LambdaUpdateWrapper<Questionnaire>()
                .eq(Questionnaire::getTenantId, tenantId)
                .eq(Questionnaire::getId, id)
                .set(Questionnaire::getStatus, "INACTIVE")) > 0;
    }
}
