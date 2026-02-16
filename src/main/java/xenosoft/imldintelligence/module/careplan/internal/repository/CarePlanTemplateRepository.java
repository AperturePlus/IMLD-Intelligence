package xenosoft.imldintelligence.module.careplan.internal.repository;

import xenosoft.imldintelligence.module.careplan.model.CarePlanTemplate;

import java.util.List;
import java.util.Optional;

public interface CarePlanTemplateRepository {
    Optional<CarePlanTemplate> findById(Long tenantId, Long id);

    Optional<CarePlanTemplate> findByTemplateCodeAndVersionNo(Long tenantId, String templateCode, Integer versionNo);

    List<CarePlanTemplate> listByTenantId(Long tenantId);

    CarePlanTemplate save(CarePlanTemplate carePlanTemplate);

    CarePlanTemplate update(CarePlanTemplate carePlanTemplate);

    Boolean deleteById(Long tenantId, Long id);
}

