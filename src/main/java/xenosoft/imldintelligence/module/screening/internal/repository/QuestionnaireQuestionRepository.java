package xenosoft.imldintelligence.module.screening.internal.repository;

import xenosoft.imldintelligence.module.screening.model.QuestionnaireQuestion;

import java.util.List;
import java.util.Optional;

public interface QuestionnaireQuestionRepository {
    Optional<QuestionnaireQuestion> findById(Long tenantId, Long id);

    Optional<QuestionnaireQuestion> findByQuestionnaireIdAndQuestionNo(Long tenantId, Long questionnaireId, String questionNo);

    List<QuestionnaireQuestion> listByTenantId(Long tenantId);

    List<QuestionnaireQuestion> listByQuestionnaireId(Long tenantId, Long questionnaireId);

    QuestionnaireQuestion save(QuestionnaireQuestion questionnaireQuestion);

    QuestionnaireQuestion update(QuestionnaireQuestion questionnaireQuestion);

    Boolean deleteById(Long tenantId, Long id);
}

