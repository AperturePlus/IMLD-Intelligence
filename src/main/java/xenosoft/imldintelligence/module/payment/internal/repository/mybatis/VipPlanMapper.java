package xenosoft.imldintelligence.module.payment.internal.repository.mybatis;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import xenosoft.imldintelligence.module.payment.internal.model.VipPlan;

import java.util.List;

@Mapper
public interface VipPlanMapper {
    VipPlan findById(@Param("tenantId") Long tenantId, @Param("id") Long id);

    VipPlan findByPlanCode(@Param("tenantId") Long tenantId, @Param("planCode") String planCode);

    List<VipPlan> listByTenantId(@Param("tenantId") Long tenantId);

    int insert(VipPlan vipPlan);

    int update(VipPlan vipPlan);

    int deleteById(@Param("tenantId") Long tenantId, @Param("id") Long id);
}

