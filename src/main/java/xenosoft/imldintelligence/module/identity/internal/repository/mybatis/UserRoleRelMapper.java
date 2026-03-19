package xenosoft.imldintelligence.module.identity.internal.repository.mybatis;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import xenosoft.imldintelligence.module.identity.internal.model.UserRoleRel;

/**
 * 用户角色关系 MyBatis-Plus Mapper，定义用户角色关系的数据读写映射。
 */
@Mapper
public interface UserRoleRelMapper extends BaseMapper<UserRoleRel> {
}
