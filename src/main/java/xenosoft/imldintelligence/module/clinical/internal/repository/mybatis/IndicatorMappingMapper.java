package xenosoft.imldintelligence.module.clinical.internal.repository.mybatis;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import xenosoft.imldintelligence.module.clinical.internal.model.IndicatorMapping;

/**
 * IndicatorMapping MyBatis-Plus Mapper，复用 BaseMapper 减少重复 CRUD SQL。
 */
@Mapper
public interface IndicatorMappingMapper extends BaseMapper<IndicatorMapping> {
    @Insert("""
            INSERT INTO indicator_mapping (
                tenant_id,
                source_system,
                source_code,
                source_name,
                target_indicator_code,
                unit_conversion_expr,
                quality_rule,
                status
            ) VALUES (
                #{mapping.tenantId},
                #{mapping.sourceSystem},
                #{mapping.sourceCode},
                #{mapping.sourceName},
                #{mapping.targetIndicatorCode},
                #{mapping.unitConversionExpr},
                #{mapping.qualityRule},
                #{mapping.status}
            )
            ON CONFLICT (tenant_id, source_system, source_code)
            DO UPDATE SET
                source_name = EXCLUDED.source_name,
                target_indicator_code = EXCLUDED.target_indicator_code,
                unit_conversion_expr = EXCLUDED.unit_conversion_expr,
                quality_rule = EXCLUDED.quality_rule,
                status = EXCLUDED.status
            """)
    int upsertByNaturalKey(@Param("mapping") IndicatorMapping mapping);
}
