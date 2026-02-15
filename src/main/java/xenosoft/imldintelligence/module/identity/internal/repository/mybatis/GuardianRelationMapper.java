package xenosoft.imldintelligence.module.identity.internal.repository.mybatis;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import xenosoft.imldintelligence.module.identity.internal.model.GuardianRelation;

import java.util.List;

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
