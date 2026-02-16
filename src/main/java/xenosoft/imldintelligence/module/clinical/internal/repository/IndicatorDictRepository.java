package xenosoft.imldintelligence.module.clinical.internal.repository;

import xenosoft.imldintelligence.module.clinical.internal.model.IndicatorDict;

import java.util.List;
import java.util.Optional;

public interface IndicatorDictRepository {
    Optional<IndicatorDict> findByCode(String code);

    List<IndicatorDict> listAll();

    List<IndicatorDict> listByStatus(String status);

    IndicatorDict save(IndicatorDict indicatorDict);

    IndicatorDict update(IndicatorDict indicatorDict);

    Boolean deleteByCode(String code);
}

