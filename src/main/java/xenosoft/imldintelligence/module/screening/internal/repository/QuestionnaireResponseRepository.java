package xenosoft.imldintelligence.module.screening.internal.repository;

import xenosoft.imldintelligence.module.screening.model.QuestionnaireResponse;

import java.util.List;
import java.util.Optional;

public interface QuestionnaireResponseRepository {
    Optional<QuestionnaireResponse> findById(Long tenantId, Long id);

    Optional<QuestionnaireResponse> findByResponseNo(Long tenantId, String responseNo);

    List<QuestionnaireResponse> listByTenantId(Long tenantId);

    List<QuestionnaireResponse> listByQuestionnaireId(Long tenantId, Long questionnaireId);

    List<QuestionnaireResponse> listByTocUserId(Long tenantId, Long tocUserId);

    QuestionnaireResponse save(QuestionnaireResponse questionnaireResponse);

    QuestionnaireResponse update(QuestionnaireResponse questionnaireResponse);

    Boolean deleteById(Long tenantId, Long id);
}

