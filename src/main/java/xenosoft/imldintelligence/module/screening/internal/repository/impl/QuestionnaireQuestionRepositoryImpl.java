package xenosoft.imldintelligence.module.screening.internal.repository.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import xenosoft.imldintelligence.module.screening.internal.repository.QuestionnaireQuestionRepository;
import xenosoft.imldintelligence.module.screening.internal.repository.mybatis.QuestionnaireQuestionMapper;
import xenosoft.imldintelligence.module.screening.model.QuestionnaireQuestion;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class QuestionnaireQuestionRepositoryImpl implements QuestionnaireQuestionRepository {
    private final QuestionnaireQuestionMapper questionnaireQuestionMapper;

    @Override
    public Optional<QuestionnaireQuestion> findById(Long tenantId, Long id) {
        return Optional.ofNullable(questionnaireQuestionMapper.findById(tenantId, id));
    }

    @Override
    public Optional<QuestionnaireQuestion> findByQuestionnaireIdAndQuestionNo(Long tenantId, Long questionnaireId, String questionNo) {
        return Optional.ofNullable(questionnaireQuestionMapper.findByQuestionnaireIdAndQuestionNo(tenantId, questionnaireId, questionNo));
    }

    @Override
    public List<QuestionnaireQuestion> listByTenantId(Long tenantId) {
        return questionnaireQuestionMapper.listByTenantId(tenantId);
    }

    @Override
    public List<QuestionnaireQuestion> listByQuestionnaireId(Long tenantId, Long questionnaireId) {
        return questionnaireQuestionMapper.listByQuestionnaireId(tenantId, questionnaireId);
    }

    @Override
    public QuestionnaireQuestion save(QuestionnaireQuestion questionnaireQuestion) {
        questionnaireQuestionMapper.insert(questionnaireQuestion);
        return questionnaireQuestion;
    }

    @Override
    public QuestionnaireQuestion update(QuestionnaireQuestion questionnaireQuestion) {
        questionnaireQuestionMapper.update(questionnaireQuestion);
        return questionnaireQuestion;
    }

    @Override
    public Boolean deleteById(Long tenantId, Long id) {
        return questionnaireQuestionMapper.deleteById(tenantId, id) > 0;
    }
}

