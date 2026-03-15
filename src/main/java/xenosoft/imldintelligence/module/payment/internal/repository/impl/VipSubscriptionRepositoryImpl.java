package xenosoft.imldintelligence.module.payment.internal.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import xenosoft.imldintelligence.module.payment.internal.model.VipSubscription;
import xenosoft.imldintelligence.module.payment.internal.repository.VipSubscriptionRepository;
import xenosoft.imldintelligence.module.payment.internal.repository.mybatis.VipSubscriptionMapper;

import java.util.List;
import java.util.Optional;

/**
 * VIP订阅仓储实现类，基于 MyBatis-Plus 完成VIP订阅的数据持久化。
 */
@Repository
@RequiredArgsConstructor
public class VipSubscriptionRepositoryImpl implements VipSubscriptionRepository {
    private final VipSubscriptionMapper vipSubscriptionMapper;

    @Override
    public Optional<VipSubscription> findById(Long tenantId, Long id) {
        return Optional.ofNullable(vipSubscriptionMapper.selectOne(new LambdaQueryWrapper<VipSubscription>()
                .eq(VipSubscription::getTenantId, tenantId)
                .eq(VipSubscription::getId, id)));
    }

    @Override
    public List<VipSubscription> listByTenantId(Long tenantId) {
        return vipSubscriptionMapper.selectList(new LambdaQueryWrapper<VipSubscription>()
                .eq(VipSubscription::getTenantId, tenantId)
                .orderByDesc(VipSubscription::getId));
    }

    @Override
    public List<VipSubscription> listByTocUserId(Long tenantId, Long tocUserId) {
        return vipSubscriptionMapper.selectList(new LambdaQueryWrapper<VipSubscription>()
                .eq(VipSubscription::getTenantId, tenantId)
                .eq(VipSubscription::getTocUserId, tocUserId)
                .orderByDesc(VipSubscription::getId));
    }

    @Override
    public List<VipSubscription> listByOrderId(Long tenantId, Long orderId) {
        return vipSubscriptionMapper.selectList(new LambdaQueryWrapper<VipSubscription>()
                .eq(VipSubscription::getTenantId, tenantId)
                .eq(VipSubscription::getOrderId, orderId)
                .orderByDesc(VipSubscription::getId));
    }

    @Override
    public VipSubscription save(VipSubscription vipSubscription) {
        vipSubscriptionMapper.insert(vipSubscription);
        return vipSubscription;
    }

    @Override
    public VipSubscription update(VipSubscription vipSubscription) {
        vipSubscriptionMapper.update(vipSubscription, new LambdaUpdateWrapper<VipSubscription>()
                .eq(VipSubscription::getTenantId, vipSubscription.getTenantId())
                .eq(VipSubscription::getId, vipSubscription.getId()));
        return vipSubscription;
    }

    @Override
    public Boolean deleteById(Long tenantId, Long id) {
        return vipSubscriptionMapper.delete(new LambdaQueryWrapper<VipSubscription>()
                .eq(VipSubscription::getTenantId, tenantId)
                .eq(VipSubscription::getId, id)) > 0;
    }
}
