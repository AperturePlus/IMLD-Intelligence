package xenosoft.imldintelligence.module.clinical.internal.repository.mybatis;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import xenosoft.imldintelligence.module.clinical.internal.model.GeneticReport;

/**
 * GeneticReport MyBatis-Plus Mapper，复用 BaseMapper 减少重复 CRUD SQL。
 */
@Mapper
public interface GeneticReportMapper extends BaseMapper<GeneticReport> {
}
