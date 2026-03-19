package xenosoft.imldintelligence.module.report.internal.repository.mybatis;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import xenosoft.imldintelligence.module.report.internal.model.Report;

/**
 * Report MyBatis-Plus Mapper，复用 BaseMapper 减少重复 CRUD SQL。
 */
@Mapper
public interface ReportMapper extends BaseMapper<Report> {
}
