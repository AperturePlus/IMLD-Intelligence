package xenosoft.imldintelligence.module.integration.internal.repository;

import xenosoft.imldintelligence.module.integration.internal.model.IntegrationJob;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 集成任务仓储接口，负责在租户边界内持久化系统集成任务数据。
 */
public interface IntegrationJobRepository {
    /**
     * 按租户和集成任务主键查询集成任务。
     *
     * @param tenantId 租户标识
     * @param id 集成任务主键
     * @return 匹配的集成任务，不存在时返回空
     */
    Optional<IntegrationJob> findById(Long tenantId, Long id);

    /**
     * 按租户和集成任务编号查询集成任务。
     *
     * @param tenantId 租户标识
     * @param jobNo 集成任务编号
     * @return 匹配的集成任务，不存在时返回空
     */
    Optional<IntegrationJob> findByJobNo(Long tenantId, String jobNo);

    /**
     * 查询租户下全部集成任务。
     *
     * @param tenantId 租户标识
     * @return 符合条件的集成任务列表
     */
    List<IntegrationJob> listByTenantId(Long tenantId);

    /**
     * 按租户和来源系统编码查询集成任务列表。
     *
     * @param tenantId 租户标识
     * @param sourceSystem 来源系统编码
     * @return 符合条件的集成任务列表
     */
    List<IntegrationJob> listBySourceSystem(Long tenantId, String sourceSystem);

    /**
     * 新增集成任务。
     *
     * @param integrationJob 待保存的集成任务
     * @return 保存后的集成任务
     */
    IntegrationJob save(IntegrationJob integrationJob);

    /**
     * 更新集成任务。
     *
     * @param integrationJob 待更新的集成任务
     * @return 更新后的集成任务
     */
    IntegrationJob update(IntegrationJob integrationJob);

    /**
     * 仅当当前状态匹配 expectedStatus 时推进任务状态，避免并发重试覆盖结果。
     *
     * @param tenantId 租户标识
     * @param id 集成任务主键
     * @param expectedStatus 期望当前状态
     * @param targetStatus 目标状态
     * @param finishedAt 完成时间
     * @param errorMessage 错误信息
     * @return 状态迁移成功时返回 {@code true}，否则返回 {@code false}
     */
    boolean updateStatusIfCurrent(Long tenantId,
                                  Long id,
                                  String expectedStatus,
                                  String targetStatus,
                                  OffsetDateTime finishedAt,
                                  String errorMessage);

    /**
     * 按租户和集成任务主键删除集成任务。
     *
     * @param tenantId 租户标识
     * @param id 集成任务主键
     * @return 删除成功时返回 {@code true}，否则返回 {@code false}
     */
    Boolean deleteById(Long tenantId, Long id);
}
