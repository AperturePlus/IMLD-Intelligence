package xenosoft.imldintelligence.module.clinical.internal.repository;

import xenosoft.imldintelligence.module.clinical.internal.model.IndicatorMapping;

import java.util.List;
import java.util.Optional;

/**
 * 指标映射仓储接口，负责在租户边界内维护来源指标到标准指标的映射关系。
 */
public interface IndicatorMappingRepository {
    /**
     * 按租户和指标映射主键查询指标映射。
     *
     * @param tenantId 租户标识
     * @param id 指标映射主键
     * @return 匹配的指标映射，不存在时返回空
     */
    Optional<IndicatorMapping> findById(Long tenantId, Long id);

    /**
     * 按租户和来源系统编码和来源指标编码查询指标映射。
     *
     * @param tenantId 租户标识
     * @param sourceSystem 来源系统编码
     * @param sourceCode 来源指标编码
     * @return 匹配的指标映射，不存在时返回空
     */
    Optional<IndicatorMapping> findBySourceSystemAndSourceCode(Long tenantId, String sourceSystem, String sourceCode);

    /**
     * 查询租户下全部指标映射。
     *
     * @param tenantId 租户标识
     * @return 符合条件的指标映射列表
     */
    List<IndicatorMapping> listByTenantId(Long tenantId);

    /**
     * 按租户和目标指标编码查询指标映射列表。
     *
     * @param tenantId 租户标识
     * @param targetIndicatorCode 目标指标编码
     * @return 符合条件的指标映射列表
     */
    List<IndicatorMapping> listByTargetIndicatorCode(Long tenantId, String targetIndicatorCode);

    /**
     * 新增指标映射。
     *
     * @param indicatorMapping 待保存的指标映射
     * @return 保存后的指标映射
     */
    IndicatorMapping save(IndicatorMapping indicatorMapping);

    IndicatorMapping upsertByNaturalKey(IndicatorMapping indicatorMapping);

    /**
     * 更新指标映射。
     *
     * @param indicatorMapping 待更新的指标映射
     * @return 更新后的指标映射
     */
    IndicatorMapping update(IndicatorMapping indicatorMapping);

    /**
     * 按租户和指标映射主键删除指标映射。
     *
     * @param tenantId 租户标识
     * @param id 指标映射主键
     * @return 删除成功时返回 {@code true}，否则返回 {@code false}
     */
    Boolean deleteById(Long tenantId, Long id);
}
