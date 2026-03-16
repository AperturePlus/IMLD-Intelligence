package xenosoft.imldintelligence.module.careplan.internal.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import xenosoft.imldintelligence.module.careplan.internal.model.CarePlan;
import xenosoft.imldintelligence.module.careplan.internal.repository.CarePlanRepository;
import xenosoft.imldintelligence.module.careplan.internal.repository.mybatis.CarePlanMapper;

import java.util.List;
import java.util.Optional;

/**
 * 护理计划仓储实现类，基于 MyBatis-Plus 完成护理计划的数据持久化。
 */
@Repository
@RequiredArgsConstructor
public class CarePlanRepositoryImpl implements CarePlanRepository {
    private final CarePlanMapper carePlanMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<CarePlan> findById(Long tenantId, Long id) {
        return Optional.ofNullable(carePlanMapper.selectOne(new LambdaQueryWrapper<CarePlan>()
                .eq(CarePlan::getTenantId, tenantId)
                .eq(CarePlan::getId, id)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CarePlan> listByTenantId(Long tenantId) {
        return carePlanMapper.selectList(new LambdaQueryWrapper<CarePlan>()
                .eq(CarePlan::getTenantId, tenantId)
                .orderByDesc(CarePlan::getId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CarePlan> listByPatientId(Long tenantId, Long patientId) {
        return carePlanMapper.selectList(new LambdaQueryWrapper<CarePlan>()
                .eq(CarePlan::getTenantId, tenantId)
                .eq(CarePlan::getPatientId, patientId)
                .orderByDesc(CarePlan::getId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CarePlan save(CarePlan carePlan) {
        carePlanMapper.insert(carePlan);
        return carePlan;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CarePlan update(CarePlan carePlan) {
        carePlanMapper.update(carePlan, new LambdaUpdateWrapper<CarePlan>()
                .eq(CarePlan::getTenantId, carePlan.getTenantId())
                .eq(CarePlan::getId, carePlan.getId()));
        return carePlan;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean deleteById(Long tenantId, Long id) {
        return carePlanMapper.delete(new LambdaQueryWrapper<CarePlan>()
                .eq(CarePlan::getTenantId, tenantId)
                .eq(CarePlan::getId, id)) > 0;
    }
}
