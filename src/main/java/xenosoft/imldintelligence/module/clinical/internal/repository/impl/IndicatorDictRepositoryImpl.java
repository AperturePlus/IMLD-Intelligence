package xenosoft.imldintelligence.module.clinical.internal.repository.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import xenosoft.imldintelligence.module.clinical.internal.model.IndicatorDict;
import xenosoft.imldintelligence.module.clinical.internal.repository.IndicatorDictRepository;
import xenosoft.imldintelligence.module.clinical.internal.repository.mybatis.IndicatorDictMapper;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class IndicatorDictRepositoryImpl implements IndicatorDictRepository {
    private final IndicatorDictMapper indicatorDictMapper;

    @Override
    public Optional<IndicatorDict> findByCode(String code) {
        return Optional.ofNullable(indicatorDictMapper.findByCode(code));
    }

    @Override
    public List<IndicatorDict> listAll() {
        return indicatorDictMapper.listAll();
    }

    @Override
    public List<IndicatorDict> listByStatus(String status) {
        return indicatorDictMapper.listByStatus(status);
    }

    @Override
    public IndicatorDict save(IndicatorDict indicatorDict) {
        indicatorDictMapper.insert(indicatorDict);
        return indicatorDict;
    }

    @Override
    public IndicatorDict update(IndicatorDict indicatorDict) {
        indicatorDictMapper.update(indicatorDict);
        return indicatorDict;
    }

    @Override
    public Boolean deleteByCode(String code) {
        return indicatorDictMapper.deleteByCode(code) > 0;
    }
}

