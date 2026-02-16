package xenosoft.imldintelligence.module.clinical.internal.repository.mybatis;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import xenosoft.imldintelligence.module.clinical.internal.model.GeneticReport;

import java.util.List;

@Mapper
public interface GeneticReportMapper {
    GeneticReport findById(@Param("tenantId") Long tenantId, @Param("id") Long id);

    List<GeneticReport> listByTenantId(@Param("tenantId") Long tenantId);

    List<GeneticReport> listByPatientId(@Param("tenantId") Long tenantId, @Param("patientId") Long patientId);

    List<GeneticReport> listByEncounterId(@Param("tenantId") Long tenantId, @Param("encounterId") Long encounterId);

    int insert(GeneticReport geneticReport);

    int update(GeneticReport geneticReport);

    int deleteById(@Param("tenantId") Long tenantId, @Param("id") Long id);
}

