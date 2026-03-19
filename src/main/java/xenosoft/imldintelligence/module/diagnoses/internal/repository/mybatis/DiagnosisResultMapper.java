package xenosoft.imldintelligence.module.diagnoses.internal.repository.mybatis;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import xenosoft.imldintelligence.module.diagnoses.internal.model.DiagnosisResult;

/**
 * 诊断结果 MyBatis-Plus Mapper，复用 BaseMapper 减少重复 CRUD SQL。
 */
@Mapper
public interface DiagnosisResultMapper extends BaseMapper<DiagnosisResult> {
}
