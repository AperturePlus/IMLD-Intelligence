package xenosoft.imldintelligence.module.report.internal.repository;

import com.fasterxml.jackson.databind.JsonNode;
import xenosoft.imldintelligence.module.report.internal.model.Report;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 报告仓储接口，负责在租户边界内持久化业务报告数据。
 */
public interface ReportRepository {
    /**
     * 按租户和报告主键查询报告。
     *
     * @param tenantId 租户标识
     * @param id 报告主键
     * @return 匹配的报告，不存在时返回空
     */
    Optional<Report> findById(Long tenantId, Long id);

    /**
     * 按租户和报告编号查询报告。
     *
     * @param tenantId 租户标识
     * @param reportNo 报告编号
     * @return 匹配的报告，不存在时返回空
     */
    Optional<Report> findByReportNo(Long tenantId, String reportNo);

    /**
     * 查询租户下全部报告。
     *
     * @param tenantId 租户标识
     * @return 符合条件的报告列表
     */
    List<Report> listByTenantId(Long tenantId);

    /**
     * 按租户和患者主键查询报告列表。
     *
     * @param tenantId 租户标识
     * @param patientId 患者主键
     * @return 符合条件的报告列表
     */
    List<Report> listByPatientId(Long tenantId, Long patientId);

    /**
     * 按租户和诊断会话主键查询报告列表。
     *
     * @param tenantId 租户标识
     * @param sessionId 诊断会话主键
     * @return 符合条件的报告列表
     */
    List<Report> listBySessionId(Long tenantId, Long sessionId);

    /**
     * 新增报告。
     *
     * @param report 待保存的报告
     * @return 保存后的报告
     */
    Report save(Report report);

    /**
     * 更新报告。
     *
     * @param report 待更新的报告
     * @return 更新后的报告
     */
    Report update(Report report);

    /**
     * 以悲观锁方式读取报告行，需在事务内调用。
     *
     * @param tenantId 租户标识
     * @param id 报告主键
     * @return 加锁读取到的报告，不存在时返回空
     */
    Optional<Report> lockByIdForUpdate(Long tenantId, Long id);

    /**
     * 仅当当前状态匹配 expectedStatus 时推进状态。
     *
     * @param tenantId 租户标识
     * @param id 报告主键
     * @param expectedStatus 期望当前状态
     * @param targetStatus 目标状态
     * @param signedBy 签署人
     * @param signedAt 签署时间
     * @param signatureData 签名数据
     * @return 状态迁移成功时返回 {@code true}，否则返回 {@code false}
     */
    boolean updateStatusIfCurrent(Long tenantId,
                                  Long id,
                                  String expectedStatus,
                                  String targetStatus,
                                  Long signedBy,
                                  OffsetDateTime signedAt,
                                  JsonNode signatureData);

    /**
     * 乐观推进报告当前版本号，仅在 expectedVersion 命中时更新。
     *
     * @param tenantId 租户标识
     * @param id 报告主键
     * @param expectedVersion 期望当前版本
     * @param targetVersion 目标版本
     * @param updatedAt 更新时间
     * @return 版本推进成功时返回 {@code true}，否则返回 {@code false}
     */
    boolean updateCurrentVersionIfMatches(Long tenantId,
                                          Long id,
                                          Integer expectedVersion,
                                          Integer targetVersion,
                                          OffsetDateTime updatedAt);

    /**
     * 按租户和报告主键删除报告。
     *
     * @param tenantId 租户标识
     * @param id 报告主键
     * @return 删除成功时返回 {@code true}，否则返回 {@code false}
     */
    Boolean deleteById(Long tenantId, Long id);
}
