package xenosoft.imldintelligence.module.notify.internal.repository;

import xenosoft.imldintelligence.module.notify.internal.model.NotificationMessage;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 通知消息仓储接口，负责在租户边界内持久化通知消息数据。
 */
public interface NotificationMessageRepository {
    /**
     * 按租户和通知消息主键查询通知消息。
     *
     * @param tenantId 租户标识
     * @param id 通知消息主键
     * @return 匹配的通知消息，不存在时返回空
     */
    Optional<NotificationMessage> findById(Long tenantId, Long id);

    /**
     * 查询租户下全部通知消息。
     *
     * @param tenantId 租户标识
     * @return 符合条件的通知消息列表
     */
    List<NotificationMessage> listByTenantId(Long tenantId);

    /**
     * 按租户和接收方类型和接收方关联标识查询通知消息列表。
     *
     * @param tenantId 租户标识
     * @param receiverType 接收方类型
     * @param receiverRefId 接收方关联标识
     * @return 符合条件的通知消息列表
     */
    List<NotificationMessage> listByReceiver(Long tenantId, String receiverType, Long receiverRefId);

    /**
     * 新增通知消息。
     *
     * @param notificationMessage 待保存的通知消息
     * @return 保存后的通知消息
     */
    NotificationMessage save(NotificationMessage notificationMessage);

    /**
     * 更新通知消息。
     *
     * @param notificationMessage 待更新的通知消息
     * @return 更新后的通知消息
     */
    NotificationMessage update(NotificationMessage notificationMessage);

    /**
     * 仅当当前状态匹配 expectedStatus 时推进消息状态，避免重复投递回调覆盖。
     *
     * @param tenantId 租户标识
     * @param id 消息主键
     * @param expectedStatus 期望当前状态
     * @param targetStatus 目标状态
     * @param sentAt 实际发送时间
     * @return 状态迁移成功时返回 {@code true}，否则返回 {@code false}
     */
    boolean updateStatusIfCurrent(Long tenantId,
                                  Long id,
                                  String expectedStatus,
                                  String targetStatus,
                                  OffsetDateTime sentAt);

    /**
     * 按租户和通知消息主键删除通知消息。
     *
     * @param tenantId 租户标识
     * @param id 通知消息主键
     * @return 删除成功时返回 {@code true}，否则返回 {@code false}
     */
    Boolean deleteById(Long tenantId, Long id);
}
