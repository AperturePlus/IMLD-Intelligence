package xenosoft.imldintelligence.module.identity.internal.repository.mybatis;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import xenosoft.imldintelligence.module.identity.internal.model.Encounter;

/**
 * 就诊记录 MyBatis-Plus Mapper，定义就诊记录的数据读写映射。
 */
@Mapper
public interface EncounterMapper extends BaseMapper<Encounter> {
}
