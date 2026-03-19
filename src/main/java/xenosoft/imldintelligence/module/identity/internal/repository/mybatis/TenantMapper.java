package xenosoft.imldintelligence.module.identity.internal.repository.mybatis;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import xenosoft.imldintelligence.module.identity.internal.model.Tenant;

/**
 * 租户 MyBatis-Plus Mapper，定义租户的数据读写映射。
 */
@Mapper
public interface TenantMapper extends BaseMapper<Tenant> {
}
