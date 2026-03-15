package xenosoft.imldintelligence.module.identity.internal.repository.mybatis;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import xenosoft.imldintelligence.module.identity.internal.model.Role;

/**
 * 角色 MyBatis-Plus Mapper，定义角色的数据读写映射。
 */
@Mapper
public interface RoleMapper extends BaseMapper<Role> {
}
