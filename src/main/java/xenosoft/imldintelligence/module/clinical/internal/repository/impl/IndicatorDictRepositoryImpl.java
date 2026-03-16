package xenosoft.imldintelligence.module.clinical.internal.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
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
        return Optional.ofNullable(indicatorDictMapper.selectById(code));
    }

    @Override
    public List<IndicatorDict> listAll() {
        return indicatorDictMapper.selectList(new LambdaQueryWrapper<IndicatorDict>().orderByAsc(IndicatorDict::getCode));
    }

    @Override
    public List<IndicatorDict> listByStatus(String status) {
        return indicatorDictMapper.selectList(new LambdaQueryWrapper<IndicatorDict>()
                .eq(IndicatorDict::getStatus, status)
                .orderByAsc(IndicatorDict::getCode));
    }

    @Override
    public IndicatorDict save(IndicatorDict indicatorDict) {
        indicatorDictMapper.insert(indicatorDict);
        return indicatorDict;
    }

    @Override
    public IndicatorDict update(IndicatorDict indicatorDict) {
        indicatorDictMapper.update(indicatorDict, new LambdaUpdateWrapper<IndicatorDict>()
                .eq(IndicatorDict::getCode, indicatorDict.getCode()));
        return indicatorDict;
    }

    @Override
    public Boolean deleteByCode(String code) {
        return indicatorDictMapper.update(null, new LambdaUpdateWrapper<IndicatorDict>()
                .eq(IndicatorDict::getCode, code)
                .set(IndicatorDict::getStatus, "INACTIVE")) > 0;
    }
}
