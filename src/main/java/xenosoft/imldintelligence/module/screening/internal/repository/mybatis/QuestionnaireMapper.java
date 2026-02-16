package xenosoft.imldintelligence.module.screening.internal.repository.mybatis;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import xenosoft.imldintelligence.module.screening.model.Questionnaire;

import java.util.List;

@Mapper
public interface QuestionnaireMapper {
    Questionnaire findById(@Param("tenantId") Long tenantId, @Param("id") Long id);

    Questionnaire findByQuestionnaireCodeAndVersionNo(@Param("tenantId") Long tenantId,
                                                       @Param("questionnaireCode") String questionnaireCode,
                                                       @Param("versionNo") Integer versionNo);

    List<Questionnaire> listByTenantId(@Param("tenantId") Long tenantId);

    int insert(Questionnaire questionnaire);

    int update(Questionnaire questionnaire);

    int deleteById(@Param("tenantId") Long tenantId, @Param("id") Long id);
}

