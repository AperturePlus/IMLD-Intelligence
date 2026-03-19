package xenosoft.imldintelligence.module.notify.internal.repository.mybatis;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import xenosoft.imldintelligence.module.notify.internal.model.NotificationMessage;

/**
 * 通知消息 MyBatis-Plus Mapper，复用 BaseMapper 减少重复 CRUD SQL。
 */
@Mapper
public interface NotificationMessageMapper extends BaseMapper<NotificationMessage> {
}
