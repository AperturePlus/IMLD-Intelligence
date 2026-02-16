package xenosoft.imldintelligence.module.careplan.internal.repository.mybatis;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import xenosoft.imldintelligence.module.careplan.model.AlertAction;

import java.util.List;

@Mapper
public interface AlertActionMapper {
    AlertAction findById(@Param("tenantId") Long tenantId, @Param("id") Long id);

    List<AlertAction> listByTenantId(@Param("tenantId") Long tenantId);

    List<AlertAction> listByAlertId(@Param("tenantId") Long tenantId, @Param("alertId") Long alertId);

    int insert(AlertAction alertAction);

    int update(AlertAction alertAction);

    int deleteById(@Param("tenantId") Long tenantId, @Param("id") Long id);
}

