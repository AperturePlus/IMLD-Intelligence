package xenosoft.imldintelligence.module.identity.internal.repository.mybatis;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import xenosoft.imldintelligence.module.identity.internal.model.TocUser;

/**
 * C端用户 MyBatis-Plus Mapper，定义C端用户的数据读写映射。
 */
@Mapper
public interface TocUserMapper extends BaseMapper<TocUser> {
}
