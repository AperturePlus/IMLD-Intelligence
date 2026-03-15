package xenosoft.imldintelligence.module.identity.internal.repository.mybatis;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import xenosoft.imldintelligence.module.identity.internal.model.ConsentRecord;

/**
 * 知情同意记录 MyBatis-Plus Mapper，定义知情同意记录的数据读写映射。
 */
@Mapper
public interface ConsentRecordMapper extends BaseMapper<ConsentRecord> {
}
