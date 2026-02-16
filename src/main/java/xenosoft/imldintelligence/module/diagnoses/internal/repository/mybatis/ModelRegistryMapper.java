package xenosoft.imldintelligence.module.diagnoses.internal.repository.mybatis;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import xenosoft.imldintelligence.module.diagnoses.model.ModelRegistry;

import java.util.List;

@Mapper
public interface ModelRegistryMapper {
    ModelRegistry findById(@Param("tenantId") Long tenantId, @Param("id") Long id);

    ModelRegistry findByModelCodeAndModelVersion(@Param("tenantId") Long tenantId,
                                                 @Param("modelCode") String modelCode,
                                                 @Param("modelVersion") String modelVersion);

    List<ModelRegistry> listByTenantId(@Param("tenantId") Long tenantId);

    int insert(ModelRegistry modelRegistry);

    int update(ModelRegistry modelRegistry);

    int deleteById(@Param("tenantId") Long tenantId, @Param("id") Long id);
}

