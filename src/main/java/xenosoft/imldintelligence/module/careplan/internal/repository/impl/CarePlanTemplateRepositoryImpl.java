package xenosoft.imldintelligence.module.careplan.internal.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import xenosoft.imldintelligence.module.careplan.internal.model.CarePlanTemplate;
import xenosoft.imldintelligence.module.careplan.internal.repository.CarePlanTemplateRepository;
import xenosoft.imldintelligence.module.careplan.internal.repository.mybatis.CarePlanTemplateMapper;

import java.util.List;
import java.util.Optional;

/**
 * 护理计划模板仓储实现类，基于 MyBatis-Plus 完成护理计划模板的数据持久化。
 */
@Repository
@RequiredArgsConstructor
public class CarePlanTemplateRepositoryImpl implements CarePlanTemplateRepository {
    private final CarePlanTemplateMapper carePlanTemplateMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<CarePlanTemplate> findById(Long tenantId, Long id) {
        return Optional.ofNullable(carePlanTemplateMapper.selectOne(new LambdaQueryWrapper<CarePlanTemplate>()
                .eq(CarePlanTemplate::getTenantId, tenantId)
                .eq(CarePlanTemplate::getId, id)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<CarePlanTemplate> findByTemplateCodeAndVersionNo(Long tenantId, String templateCode, Integer versionNo) {
        return Optional.ofNullable(carePlanTemplateMapper.selectOne(new LambdaQueryWrapper<CarePlanTemplate>()
                .eq(CarePlanTemplate::getTenantId, tenantId)
                .eq(CarePlanTemplate::getTemplateCode, templateCode)
                .eq(CarePlanTemplate::getVersionNo, versionNo)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CarePlanTemplate> listByTenantId(Long tenantId) {
        return carePlanTemplateMapper.selectList(new LambdaQueryWrapper<CarePlanTemplate>()
                .eq(CarePlanTemplate::getTenantId, tenantId)
                .orderByDesc(CarePlanTemplate::getId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CarePlanTemplate save(CarePlanTemplate carePlanTemplate) {
        carePlanTemplateMapper.insert(carePlanTemplate);
        return carePlanTemplate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CarePlanTemplate update(CarePlanTemplate carePlanTemplate) {
        carePlanTemplateMapper.update(carePlanTemplate, new LambdaUpdateWrapper<CarePlanTemplate>()
                .eq(CarePlanTemplate::getTenantId, carePlanTemplate.getTenantId())
                .eq(CarePlanTemplate::getId, carePlanTemplate.getId()));
        return carePlanTemplate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean deleteById(Long tenantId, Long id) {
        return carePlanTemplateMapper.update(null, new LambdaUpdateWrapper<CarePlanTemplate>()
                .eq(CarePlanTemplate::getTenantId, tenantId)
                .eq(CarePlanTemplate::getId, id)
                .set(CarePlanTemplate::getStatus, "INACTIVE")) > 0;
    }
}
