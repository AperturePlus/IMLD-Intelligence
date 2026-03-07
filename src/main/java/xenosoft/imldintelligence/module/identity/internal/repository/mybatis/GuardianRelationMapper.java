package xenosoft.imldintelligence.module.identity.internal.repository.mybatis;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import xenosoft.imldintelligence.module.identity.internal.model.GuardianRelation;

import java.util.List;

/**
 * 监护人关系 MyBatis Mapper，定义监护人关系的数据读写映射。
 */
@Mapper
public interface GuardianRelationMapper {
    GuardianRelation findById(@Param("tenantId") Long tenantId, @Param("id") Long id);

    GuardianRelation findPrimaryByPatientId(@Param("tenantId") Long tenantId, @Param("patientId") Long patientId);

    List<GuardianRelation> listByPatientId(@Param("tenantId") Long tenantId, @Param("patientId") Long patientId);

    List<GuardianRelation> listByTenantId(@Param("tenantId") Long tenantId);

    int insert(GuardianRelation guardianRelation);

    int update(GuardianRelation guardianRelation);

    int deleteById(@Param("tenantId") Long tenantId, @Param("id") Long id);
}
