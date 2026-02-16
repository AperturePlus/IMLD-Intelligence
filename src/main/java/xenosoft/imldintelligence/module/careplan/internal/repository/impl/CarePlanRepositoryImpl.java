package xenosoft.imldintelligence.module.careplan.internal.repository.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import xenosoft.imldintelligence.module.careplan.internal.repository.CarePlanRepository;
import xenosoft.imldintelligence.module.careplan.internal.repository.mybatis.CarePlanMapper;
import xenosoft.imldintelligence.module.careplan.model.CarePlan;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CarePlanRepositoryImpl implements CarePlanRepository {
    private final CarePlanMapper carePlanMapper;

    @Override
    public Optional<CarePlan> findById(Long tenantId, Long id) {
        return Optional.ofNullable(carePlanMapper.findById(tenantId, id));
    }

    @Override
    public List<CarePlan> listByTenantId(Long tenantId) {
        return carePlanMapper.listByTenantId(tenantId);
    }

    @Override
    public List<CarePlan> listByPatientId(Long tenantId, Long patientId) {
        return carePlanMapper.listByPatientId(tenantId, patientId);
    }

    @Override
    public CarePlan save(CarePlan carePlan) {
        carePlanMapper.insert(carePlan);
        return carePlan;
    }

    @Override
    public CarePlan update(CarePlan carePlan) {
        carePlanMapper.update(carePlan);
        return carePlan;
    }

    @Override
    public Boolean deleteById(Long tenantId, Long id) {
        return carePlanMapper.deleteById(tenantId, id) > 0;
    }
}

