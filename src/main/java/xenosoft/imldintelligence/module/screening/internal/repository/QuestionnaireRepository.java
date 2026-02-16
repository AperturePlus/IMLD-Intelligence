package xenosoft.imldintelligence.module.screening.internal.repository;

import xenosoft.imldintelligence.module.screening.model.Questionnaire;

import java.util.List;
import java.util.Optional;

public interface QuestionnaireRepository {
    Optional<Questionnaire> findById(Long tenantId, Long id);

    Optional<Questionnaire> findByQuestionnaireCodeAndVersionNo(Long tenantId, String questionnaireCode, Integer versionNo);

    List<Questionnaire> listByTenantId(Long tenantId);

    Questionnaire save(Questionnaire questionnaire);

    Questionnaire update(Questionnaire questionnaire);

    Boolean deleteById(Long tenantId, Long id);
}

