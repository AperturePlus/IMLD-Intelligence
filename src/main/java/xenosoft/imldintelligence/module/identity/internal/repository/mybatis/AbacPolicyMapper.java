package xenosoft.imldintelligence.module.identity.internal.repository.mybatis;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import xenosoft.imldintelligence.module.identity.internal.model.AbacPolicy;

/**
 * ABAC策略 MyBatis-Plus Mapper，定义ABAC策略的数据读写映射。
 */
@Mapper
public interface AbacPolicyMapper extends BaseMapper<AbacPolicy> {
}
