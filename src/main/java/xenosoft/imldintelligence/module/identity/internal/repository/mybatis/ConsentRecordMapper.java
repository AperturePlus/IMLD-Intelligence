package xenosoft.imldintelligence.module.identity.internal.repository.mybatis;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import xenosoft.imldintelligence.module.identity.internal.model.ConsentRecord;

import java.util.List;

/**
 * 知情同意记录 MyBatis Mapper，定义知情同意记录的数据读写映射。
 */
@Mapper
public interface ConsentRecordMapper {
    ConsentRecord findById(@Param("tenantId") Long tenantId, @Param("id") Long id);

    List<ConsentRecord> listByTenantId(@Param("tenantId") Long tenantId);

    List<ConsentRecord> listByPatientId(@Param("tenantId") Long tenantId, @Param("patientId") Long patientId);

    List<ConsentRecord> listByTocUserId(@Param("tenantId") Long tenantId, @Param("tocUserId") Long tocUserId);

    long countByCondition(@Param("tenantId") Long tenantId,
                          @Param("patientId") Long patientId,
                          @Param("consentType") String consentType,
                          @Param("status") String status);

    List<ConsentRecord> listByCondition(@Param("tenantId") Long tenantId,
                                         @Param("patientId") Long patientId,
                                         @Param("consentType") String consentType,
                                         @Param("status") String status,
                                         @Param("offset") long offset,
                                         @Param("limit") int limit);

    int insert(ConsentRecord consentRecord);

    int update(ConsentRecord consentRecord);

    int deleteById(@Param("tenantId") Long tenantId, @Param("id") Long id);
}
