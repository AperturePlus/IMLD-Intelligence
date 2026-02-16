package xenosoft.imldintelligence.module.screening.internal.repository.mybatis;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import xenosoft.imldintelligence.module.screening.model.TocClinicalTransfer;

import java.util.List;

@Mapper
public interface TocClinicalTransferMapper {
    TocClinicalTransfer findById(@Param("tenantId") Long tenantId, @Param("id") Long id);

    List<TocClinicalTransfer> listByTenantId(@Param("tenantId") Long tenantId);

    List<TocClinicalTransfer> listByResponseId(@Param("tenantId") Long tenantId, @Param("responseId") Long responseId);

    List<TocClinicalTransfer> listByPatientId(@Param("tenantId") Long tenantId, @Param("patientId") Long patientId);

    int insert(TocClinicalTransfer tocClinicalTransfer);

    int update(TocClinicalTransfer tocClinicalTransfer);

    int deleteById(@Param("tenantId") Long tenantId, @Param("id") Long id);
}

