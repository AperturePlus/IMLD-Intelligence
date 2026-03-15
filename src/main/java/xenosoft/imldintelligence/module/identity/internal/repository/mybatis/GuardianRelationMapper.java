package xenosoft.imldintelligence.module.identity.internal.repository.mybatis;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import xenosoft.imldintelligence.module.identity.internal.model.GuardianRelation;

/**
 * 监护人关系 MyBatis-Plus Mapper，定义监护人关系的数据读写映射。
 */
@Mapper
public interface GuardianRelationMapper extends BaseMapper<GuardianRelation> {
}
