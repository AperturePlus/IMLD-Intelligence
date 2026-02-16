package xenosoft.imldintelligence.module.careplan.internal.repository.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import xenosoft.imldintelligence.module.careplan.internal.repository.CarePlanTemplateRepository;
import xenosoft.imldintelligence.module.careplan.internal.repository.mybatis.CarePlanTemplateMapper;
import xenosoft.imldintelligence.module.careplan.model.CarePlanTemplate;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CarePlanTemplateRepositoryImpl implements CarePlanTemplateRepository {
    private final CarePlanTemplateMapper carePlanTemplateMapper;

    @Override
    public Optional<CarePlanTemplate> findById(Long tenantId, Long id) {
        return Optional.ofNullable(carePlanTemplateMapper.findById(tenantId, id));
    }

    @Override
    public Optional<CarePlanTemplate> findByTemplateCodeAndVersionNo(Long tenantId, String templateCode, Integer versionNo) {
        return Optional.ofNullable(carePlanTemplateMapper.findByTemplateCodeAndVersionNo(tenantId, templateCode, versionNo));
    }

    @Override
    public List<CarePlanTemplate> listByTenantId(Long tenantId) {
        return carePlanTemplateMapper.listByTenantId(tenantId);
    }

    @Override
    public CarePlanTemplate save(CarePlanTemplate carePlanTemplate) {
        carePlanTemplateMapper.insert(carePlanTemplate);
        return carePlanTemplate;
    }

    @Override
    public CarePlanTemplate update(CarePlanTemplate carePlanTemplate) {
        carePlanTemplateMapper.update(carePlanTemplate);
        return carePlanTemplate;
    }

    @Override
    public Boolean deleteById(Long tenantId, Long id) {
        return carePlanTemplateMapper.deleteById(tenantId, id) > 0;
    }
}

