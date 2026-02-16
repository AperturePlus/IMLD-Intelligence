package xenosoft.imldintelligence.module.notify.internal.repository.mybatis;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import xenosoft.imldintelligence.module.notify.internal.model.NotificationDelivery;

import java.util.List;

@Mapper
public interface NotificationDeliveryMapper {
    NotificationDelivery findById(@Param("tenantId") Long tenantId, @Param("id") Long id);

    List<NotificationDelivery> listByTenantId(@Param("tenantId") Long tenantId);

    List<NotificationDelivery> listByMessageId(@Param("tenantId") Long tenantId, @Param("messageId") Long messageId);

    int insert(NotificationDelivery notificationDelivery);

    int update(NotificationDelivery notificationDelivery);

    int deleteById(@Param("tenantId") Long tenantId, @Param("id") Long id);
}

