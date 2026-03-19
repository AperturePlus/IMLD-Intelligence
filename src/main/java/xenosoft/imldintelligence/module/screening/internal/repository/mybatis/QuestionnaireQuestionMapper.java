package xenosoft.imldintelligence.module.screening.internal.repository.mybatis;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import xenosoft.imldintelligence.module.screening.internal.model.QuestionnaireQuestion;

/**
 * 问卷题目 MyBatis-Plus Mapper，复用 BaseMapper 减少重复 CRUD SQL。
 */
@Mapper
public interface QuestionnaireQuestionMapper extends BaseMapper<QuestionnaireQuestion> {
}
