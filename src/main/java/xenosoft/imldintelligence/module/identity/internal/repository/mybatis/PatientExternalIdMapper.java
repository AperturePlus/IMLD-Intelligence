package xenosoft.imldintelligence.module.identity.internal.repository.mybatis;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import xenosoft.imldintelligence.module.identity.internal.model.PatientExternalId;

/**
 * 患者外部标识 MyBatis-Plus Mapper，定义患者外部标识的数据读写映射。
 */
@Mapper
public interface PatientExternalIdMapper extends BaseMapper<PatientExternalId> {
}
