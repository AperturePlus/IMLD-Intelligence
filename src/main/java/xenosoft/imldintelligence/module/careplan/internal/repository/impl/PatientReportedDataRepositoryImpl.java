package xenosoft.imldintelligence.module.careplan.internal.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import xenosoft.imldintelligence.module.careplan.internal.model.PatientReportedData;
import xenosoft.imldintelligence.module.careplan.internal.repository.PatientReportedDataRepository;
import xenosoft.imldintelligence.module.careplan.internal.repository.mybatis.PatientReportedDataMapper;

import java.util.List;
import java.util.Optional;

/**
 * 患者上报数据仓储实现类，基于 MyBatis-Plus 完成患者上报数据的数据持久化。
 */
@Repository
@RequiredArgsConstructor
public class PatientReportedDataRepositoryImpl implements PatientReportedDataRepository {
    private final PatientReportedDataMapper patientReportedDataMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<PatientReportedData> findById(Long tenantId, Long id) {
        return Optional.ofNullable(patientReportedDataMapper.selectOne(new LambdaQueryWrapper<PatientReportedData>()
                .eq(PatientReportedData::getTenantId, tenantId)
                .eq(PatientReportedData::getId, id)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<PatientReportedData> listByTenantId(Long tenantId) {
        return patientReportedDataMapper.selectList(new LambdaQueryWrapper<PatientReportedData>()
                .eq(PatientReportedData::getTenantId, tenantId)
                .orderByDesc(PatientReportedData::getId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<PatientReportedData> listByCarePlanId(Long tenantId, Long carePlanId) {
        return patientReportedDataMapper.selectList(new LambdaQueryWrapper<PatientReportedData>()
                .eq(PatientReportedData::getTenantId, tenantId)
                .eq(PatientReportedData::getCarePlanId, carePlanId)
                .orderByDesc(PatientReportedData::getId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<PatientReportedData> listByPatientId(Long tenantId, Long patientId) {
        return patientReportedDataMapper.selectList(new LambdaQueryWrapper<PatientReportedData>()
                .eq(PatientReportedData::getTenantId, tenantId)
                .eq(PatientReportedData::getPatientId, patientId)
                .orderByDesc(PatientReportedData::getId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PatientReportedData save(PatientReportedData patientReportedData) {
        patientReportedDataMapper.insert(patientReportedData);
        return patientReportedData;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PatientReportedData update(PatientReportedData patientReportedData) {
        patientReportedDataMapper.update(patientReportedData, new LambdaUpdateWrapper<PatientReportedData>()
                .eq(PatientReportedData::getTenantId, patientReportedData.getTenantId())
                .eq(PatientReportedData::getId, patientReportedData.getId()));
        return patientReportedData;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean deleteById(Long tenantId, Long id) {
        return patientReportedDataMapper.delete(new LambdaQueryWrapper<PatientReportedData>()
                .eq(PatientReportedData::getTenantId, tenantId)
                .eq(PatientReportedData::getId, id)) > 0;
    }
}
