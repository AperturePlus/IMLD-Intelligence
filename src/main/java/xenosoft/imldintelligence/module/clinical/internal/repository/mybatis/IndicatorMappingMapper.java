package xenosoft.imldintelligence.module.clinical.internal.repository.mybatis;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import xenosoft.imldintelligence.module.clinical.internal.model.IndicatorMapping;

import java.util.List;

@Mapper
public interface IndicatorMappingMapper {
    IndicatorMapping findById(@Param("tenantId") Long tenantId, @Param("id") Long id);

    IndicatorMapping findBySourceSystemAndSourceCode(@Param("tenantId") Long tenantId,
                                                     @Param("sourceSystem") String sourceSystem,
                                                     @Param("sourceCode") String sourceCode);

    List<IndicatorMapping> listByTenantId(@Param("tenantId") Long tenantId);

    List<IndicatorMapping> listByTargetIndicatorCode(@Param("tenantId") Long tenantId,
                                                     @Param("targetIndicatorCode") String targetIndicatorCode);

    int insert(IndicatorMapping indicatorMapping);

    int update(IndicatorMapping indicatorMapping);

    int deleteById(@Param("tenantId") Long tenantId, @Param("id") Long id);
}

