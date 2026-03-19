package xenosoft.imldintelligence.module.identity.internal.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import xenosoft.imldintelligence.module.identity.internal.model.TocUser;
import xenosoft.imldintelligence.module.identity.internal.repository.TocUserRepository;
import xenosoft.imldintelligence.module.identity.internal.repository.mybatis.TocUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

/**
 * C端用户仓储实现类，基于 MyBatis-Plus 完成C端用户的数据持久化。
 */
@Repository
@RequiredArgsConstructor
public class TocUserRepositoryImpl implements TocUserRepository {
    private final TocUserMapper tocUserMapper;

    @Override
    public Optional<TocUser> findById(Long tenantId, Long id) {
        return Optional.ofNullable(tocUserMapper.selectOne(new LambdaQueryWrapper<TocUser>()
                .eq(TocUser::getTenantId, tenantId)
                .eq(TocUser::getId, id)));
    }

    @Override
    public Optional<TocUser> findByTocUid(Long tenantId, String tocUid) {
        return Optional.ofNullable(tocUserMapper.selectOne(new LambdaQueryWrapper<TocUser>()
                .eq(TocUser::getTenantId, tenantId)
                .eq(TocUser::getTocUid, tocUid)));
    }

    @Override
    public List<TocUser> listByTenantId(Long tenantId) {
        return tocUserMapper.selectList(new LambdaQueryWrapper<TocUser>()
                .eq(TocUser::getTenantId, tenantId)
                .orderByDesc(TocUser::getId));
    }

    @Override
    public TocUser save(TocUser tocUser) {
        tocUserMapper.insert(tocUser);
        return tocUser;
    }

    @Override
    public TocUser update(TocUser tocUser) {
        tocUserMapper.update(null, new LambdaUpdateWrapper<TocUser>()
                .eq(TocUser::getTenantId, tocUser.getTenantId())
                .eq(TocUser::getId, tocUser.getId())
                .set(TocUser::getTocUid, tocUser.getTocUid())
                .set(TocUser::getNickname, tocUser.getNickname())
                .set(TocUser::getMobileEncrypted, tocUser.getMobileEncrypted())
                .set(TocUser::getOpenid, tocUser.getOpenid())
                .set(TocUser::getUnionid, tocUser.getUnionid())
                .set(TocUser::getVipStatus, tocUser.getVipStatus())
                .set(TocUser::getStatus, tocUser.getStatus())
                .set(TocUser::getUpdatedAt, OffsetDateTime.now()));
        return tocUser;
    }

    @Override
    public Boolean deleteById(Long tenantId, Long id) {
        return tocUserMapper.update(null, new LambdaUpdateWrapper<TocUser>()
                .eq(TocUser::getTenantId, tenantId)
                .eq(TocUser::getId, id)
                .set(TocUser::getStatus, "INACTIVE")
                .set(TocUser::getUpdatedAt, OffsetDateTime.now())) > 0;
    }
}
