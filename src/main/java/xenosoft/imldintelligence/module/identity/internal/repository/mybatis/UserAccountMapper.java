package xenosoft.imldintelligence.module.identity.internal.repository.mybatis;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import xenosoft.imldintelligence.module.identity.internal.model.UserAccount;

/**
 * 用户账号 MyBatis-Plus Mapper，定义用户账号的数据读写映射。
 */
@Mapper
public interface UserAccountMapper extends BaseMapper<UserAccount> {
}
