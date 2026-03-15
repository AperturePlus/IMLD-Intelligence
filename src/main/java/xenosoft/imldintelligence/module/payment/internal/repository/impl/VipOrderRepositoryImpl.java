package xenosoft.imldintelligence.module.payment.internal.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import xenosoft.imldintelligence.module.payment.internal.model.VipOrder;
import xenosoft.imldintelligence.module.payment.internal.repository.VipOrderRepository;
import xenosoft.imldintelligence.module.payment.internal.repository.mybatis.VipOrderMapper;

import java.util.List;
import java.util.Optional;

/**
 * VIP订单仓储实现类，基于 MyBatis-Plus 完成VIP订单的数据持久化。
 */
@Repository
@RequiredArgsConstructor
public class VipOrderRepositoryImpl implements VipOrderRepository {
    private final VipOrderMapper vipOrderMapper;

    @Override
    public Optional<VipOrder> findById(Long tenantId, Long id) {
        return Optional.ofNullable(vipOrderMapper.selectOne(new LambdaQueryWrapper<VipOrder>()
                .eq(VipOrder::getTenantId, tenantId)
                .eq(VipOrder::getId, id)));
    }

    @Override
    public Optional<VipOrder> findByOrderNo(Long tenantId, String orderNo) {
        return Optional.ofNullable(vipOrderMapper.selectOne(new LambdaQueryWrapper<VipOrder>()
                .eq(VipOrder::getTenantId, tenantId)
                .eq(VipOrder::getOrderNo, orderNo)));
    }

    @Override
    public List<VipOrder> listByTenantId(Long tenantId) {
        return vipOrderMapper.selectList(new LambdaQueryWrapper<VipOrder>()
                .eq(VipOrder::getTenantId, tenantId)
                .orderByDesc(VipOrder::getId));
    }

    @Override
    public List<VipOrder> listByTocUserId(Long tenantId, Long tocUserId) {
        return vipOrderMapper.selectList(new LambdaQueryWrapper<VipOrder>()
                .eq(VipOrder::getTenantId, tenantId)
                .eq(VipOrder::getTocUserId, tocUserId)
                .orderByDesc(VipOrder::getId));
    }

    @Override
    public VipOrder save(VipOrder vipOrder) {
        vipOrderMapper.insert(vipOrder);
        return vipOrder;
    }

    @Override
    public VipOrder update(VipOrder vipOrder) {
        vipOrderMapper.update(vipOrder, new LambdaUpdateWrapper<VipOrder>()
                .eq(VipOrder::getTenantId, vipOrder.getTenantId())
                .eq(VipOrder::getId, vipOrder.getId()));
        return vipOrder;
    }

    @Override
    public Boolean deleteById(Long tenantId, Long id) {
        return vipOrderMapper.delete(new LambdaQueryWrapper<VipOrder>()
                .eq(VipOrder::getTenantId, tenantId)
                .eq(VipOrder::getId, id)) > 0;
    }
}
