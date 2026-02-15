package xenosoft.imldintelligence.module.identity.internal.repository.mybatis;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import xenosoft.imldintelligence.module.identity.internal.model.Encounter;

import java.util.List;

@Mapper
public interface EncounterMapper {
    Encounter findById(@Param("tenantId") Long tenantId, @Param("id") Long id);

    Encounter findByEncounterNo(@Param("tenantId") Long tenantId, @Param("encounterNo") String encounterNo);

    List<Encounter> listByTenantId(@Param("tenantId") Long tenantId);

    List<Encounter> listByPatientId(@Param("tenantId") Long tenantId, @Param("patientId") Long patientId);

    int insert(Encounter encounter);

    int update(Encounter encounter);

    int deleteById(@Param("tenantId") Long tenantId, @Param("id") Long id);
}
