package xenosoft.imldintelligence.module.payment.internal.repository;

import xenosoft.imldintelligence.module.payment.internal.model.VipOrder;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

/**
 * VIP订单仓储接口，负责在租户边界内持久化会员订单数据。
 */
public interface VipOrderRepository {
    /**
     * 按租户和VIP订单主键查询VIP订单。
     *
     * @param tenantId 租户标识
     * @param id VIP订单主键
     * @return 匹配的VIP订单，不存在时返回空
     */
    Optional<VipOrder> findById(Long tenantId, Long id);

    /**
     * 按租户和订单号查询VIP订单。
     *
     * @param tenantId 租户标识
     * @param orderNo 订单号
     * @return 匹配的VIP订单，不存在时返回空
     */
    Optional<VipOrder> findByOrderNo(Long tenantId, String orderNo);

    /**
     * 查询租户下全部VIP订单。
     *
     * @param tenantId 租户标识
     * @return 符合条件的VIP订单列表
     */
    List<VipOrder> listByTenantId(Long tenantId);

    /**
     * 按租户和C端用户主键查询VIP订单列表。
     *
     * @param tenantId 租户标识
     * @param tocUserId C端用户主键
     * @return 符合条件的VIP订单列表
     */
    List<VipOrder> listByTocUserId(Long tenantId, Long tocUserId);

    /**
     * 新增VIP订单。
     *
     * @param vipOrder 待保存的VIP订单
     * @return 保存后的VIP订单
     */
    VipOrder save(VipOrder vipOrder);

    /**
     * 更新VIP订单。
     *
     * @param vipOrder 待更新的VIP订单
     * @return 更新后的VIP订单
     */
    VipOrder update(VipOrder vipOrder);

    /**
     * 仅当当前状态匹配 expectedStatus 时推进订单状态，避免并发回调重复处理。
     *
     * @param tenantId 租户标识
     * @param id 订单主键
     * @param expectedStatus 期望当前状态
     * @param targetStatus 目标状态
     * @param paidAt 支付时间
     * @return 状态迁移成功时返回 {@code true}，否则返回 {@code false}
     */
    boolean updateStatusIfCurrent(Long tenantId,
                                  Long id,
                                  String expectedStatus,
                                  String targetStatus,
                                  OffsetDateTime paidAt);

    /**
     * 仅当订单号和当前状态匹配时推进订单状态，适配支付回调幂等场景。
     *
     * @param tenantId 租户标识
     * @param orderNo 订单号
     * @param expectedStatus 期望当前状态
     * @param targetStatus 目标状态
     * @param paidAt 支付时间
     * @return 状态迁移成功时返回 {@code true}，否则返回 {@code false}
     */
    boolean updateStatusByOrderNoIfCurrent(Long tenantId,
                                           String orderNo,
                                           String expectedStatus,
                                           String targetStatus,
                                           OffsetDateTime paidAt);

    /**
     * 按租户和VIP订单主键删除VIP订单。
     *
     * @param tenantId 租户标识
     * @param id VIP订单主键
     * @return 删除成功时返回 {@code true}，否则返回 {@code false}
     */
    Boolean deleteById(Long tenantId, Long id);
}
