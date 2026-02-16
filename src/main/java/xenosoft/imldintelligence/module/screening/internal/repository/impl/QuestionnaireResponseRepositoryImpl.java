package xenosoft.imldintelligence.module.screening.internal.repository.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import xenosoft.imldintelligence.module.screening.internal.repository.QuestionnaireResponseRepository;
import xenosoft.imldintelligence.module.screening.internal.repository.mybatis.QuestionnaireResponseMapper;
import xenosoft.imldintelligence.module.screening.model.QuestionnaireResponse;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class QuestionnaireResponseRepositoryImpl implements QuestionnaireResponseRepository {
    private final QuestionnaireResponseMapper questionnaireResponseMapper;

    @Override
    public Optional<QuestionnaireResponse> findById(Long tenantId, Long id) {
        return Optional.ofNullable(questionnaireResponseMapper.findById(tenantId, id));
    }

    @Override
    public Optional<QuestionnaireResponse> findByResponseNo(Long tenantId, String responseNo) {
        return Optional.ofNullable(questionnaireResponseMapper.findByResponseNo(tenantId, responseNo));
    }

    @Override
    public List<QuestionnaireResponse> listByTenantId(Long tenantId) {
        return questionnaireResponseMapper.listByTenantId(tenantId);
    }

    @Override
    public List<QuestionnaireResponse> listByQuestionnaireId(Long tenantId, Long questionnaireId) {
        return questionnaireResponseMapper.listByQuestionnaireId(tenantId, questionnaireId);
    }

    @Override
    public List<QuestionnaireResponse> listByTocUserId(Long tenantId, Long tocUserId) {
        return questionnaireResponseMapper.listByTocUserId(tenantId, tocUserId);
    }

    @Override
    public QuestionnaireResponse save(QuestionnaireResponse questionnaireResponse) {
        questionnaireResponseMapper.insert(questionnaireResponse);
        return questionnaireResponse;
    }

    @Override
    public QuestionnaireResponse update(QuestionnaireResponse questionnaireResponse) {
        questionnaireResponseMapper.update(questionnaireResponse);
        return questionnaireResponse;
    }

    @Override
    public Boolean deleteById(Long tenantId, Long id) {
        return questionnaireResponseMapper.deleteById(tenantId, id) > 0;
    }
}

