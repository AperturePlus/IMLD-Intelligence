package xenosoft.imldintelligence.module.clinical.internal.repository.mybatis;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import xenosoft.imldintelligence.module.clinical.internal.model.IndicatorDict;

import java.util.List;

@Mapper
public interface IndicatorDictMapper {
    IndicatorDict findByCode(@Param("code") String code);

    List<IndicatorDict> listAll();

    List<IndicatorDict> listByStatus(@Param("status") String status);

    int insert(IndicatorDict indicatorDict);

    int update(IndicatorDict indicatorDict);

    int deleteByCode(@Param("code") String code);
}

