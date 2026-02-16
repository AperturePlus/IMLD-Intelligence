package xenosoft.imldintelligence.module.payment.internal.repository.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import xenosoft.imldintelligence.module.payment.internal.model.VipOrder;
import xenosoft.imldintelligence.module.payment.internal.repository.VipOrderRepository;
import xenosoft.imldintelligence.module.payment.internal.repository.mybatis.VipOrderMapper;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class VipOrderRepositoryImpl implements VipOrderRepository {
    private final VipOrderMapper vipOrderMapper;

    @Override
    public Optional<VipOrder> findById(Long tenantId, Long id) {
        return Optional.ofNullable(vipOrderMapper.findById(tenantId, id));
    }

    @Override
    public Optional<VipOrder> findByOrderNo(Long tenantId, String orderNo) {
        return Optional.ofNullable(vipOrderMapper.findByOrderNo(tenantId, orderNo));
    }

    @Override
    public List<VipOrder> listByTenantId(Long tenantId) {
        return vipOrderMapper.listByTenantId(tenantId);
    }

    @Override
    public List<VipOrder> listByTocUserId(Long tenantId, Long tocUserId) {
        return vipOrderMapper.listByTocUserId(tenantId, tocUserId);
    }

    @Override
    public VipOrder save(VipOrder vipOrder) {
        vipOrderMapper.insert(vipOrder);
        return vipOrder;
    }

    @Override
    public VipOrder update(VipOrder vipOrder) {
        vipOrderMapper.update(vipOrder);
        return vipOrder;
    }

    @Override
    public Boolean deleteById(Long tenantId, Long id) {
        return vipOrderMapper.deleteById(tenantId, id) > 0;
    }
}

