package xenosoft.imldintelligence.module.screening.internal.repository;

import xenosoft.imldintelligence.module.screening.internal.model.TocClinicalTransfer;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

/**
 * TOC临床转化仓储接口，负责在租户边界内维护问卷转临床的数据链路。
 */
public interface TocClinicalTransferRepository {
    /**
     * 按租户和TOC临床转化记录主键查询TOC临床转化记录。
     *
     * @param tenantId 租户标识
     * @param id TOC临床转化记录主键
     * @return 匹配的TOC临床转化记录，不存在时返回空
     */
    Optional<TocClinicalTransfer> findById(Long tenantId, Long id);

    /**
     * 查询租户下全部TOC临床转化记录。
     *
     * @param tenantId 租户标识
     * @return 符合条件的TOC临床转化记录列表
     */
    List<TocClinicalTransfer> listByTenantId(Long tenantId);

    /**
     * 按租户和问卷应答主键查询TOC临床转化记录列表。
     *
     * @param tenantId 租户标识
     * @param responseId 问卷应答主键
     * @return 符合条件的TOC临床转化记录列表
     */
    List<TocClinicalTransfer> listByResponseId(Long tenantId, Long responseId);

    /**
     * 按租户和患者主键查询TOC临床转化记录列表。
     *
     * @param tenantId 租户标识
     * @param patientId 患者主键
     * @return 符合条件的TOC临床转化记录列表
     */
    List<TocClinicalTransfer> listByPatientId(Long tenantId, Long patientId);

    /**
     * 新增TOC临床转化记录。
     *
     * @param tocClinicalTransfer 待保存的TOC临床转化记录
     * @return 保存后的TOC临床转化记录
     */
    TocClinicalTransfer save(TocClinicalTransfer tocClinicalTransfer);

    /**
     * 更新TOC临床转化记录。
     *
     * @param tocClinicalTransfer 待更新的TOC临床转化记录
     * @return 更新后的TOC临床转化记录
     */
    TocClinicalTransfer update(TocClinicalTransfer tocClinicalTransfer);

    /**
     * 仅当当前状态匹配 expectedStatus 时推进状态，避免并发重复审批。
     *
     * @param tenantId 租户标识
     * @param id 转诊记录主键
     * @param expectedStatus 期望当前状态
     * @param targetStatus 目标状态
     * @param approvedBy 审批人
     * @param approvedAt 审批时间
     * @param transferNote 审批备注
     * @return 状态迁移成功时返回 {@code true}，否则返回 {@code false}
     */
    boolean updateStatusIfCurrent(Long tenantId,
                                  Long id,
                                  String expectedStatus,
                                  String targetStatus,
                                  Long approvedBy,
                                  OffsetDateTime approvedAt,
                                  String transferNote);

    /**
     * 按租户和TOC临床转化记录主键删除TOC临床转化记录。
     *
     * @param tenantId 租户标识
     * @param id TOC临床转化记录主键
     * @return 删除成功时返回 {@code true}，否则返回 {@code false}
     */
    Boolean deleteById(Long tenantId, Long id);
}
