package xenosoft.imldintelligence.module.payment.internal.repository.mybatis;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import xenosoft.imldintelligence.module.payment.internal.model.VipOrder;

/**
 * VipOrder MyBatis-Plus Mapper，复用 BaseMapper 减少重复 CRUD SQL。
 */
@Mapper
public interface VipOrderMapper extends BaseMapper<VipOrder> {
}
