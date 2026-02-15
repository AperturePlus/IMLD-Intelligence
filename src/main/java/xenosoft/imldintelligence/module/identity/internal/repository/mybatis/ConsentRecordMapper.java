package xenosoft.imldintelligence.module.identity.internal.repository.mybatis;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import xenosoft.imldintelligence.module.identity.internal.model.ConsentRecord;

import java.util.List;

@Mapper
public interface ConsentRecordMapper {
    ConsentRecord findById(@Param("tenantId") Long tenantId, @Param("id") Long id);

    List<ConsentRecord> listByTenantId(@Param("tenantId") Long tenantId);

    List<ConsentRecord> listByPatientId(@Param("tenantId") Long tenantId, @Param("patientId") Long patientId);

    List<ConsentRecord> listByTocUserId(@Param("tenantId") Long tenantId, @Param("tocUserId") Long tocUserId);

    int insert(ConsentRecord consentRecord);

    int update(ConsentRecord consentRecord);

    int deleteById(@Param("tenantId") Long tenantId, @Param("id") Long id);
}
