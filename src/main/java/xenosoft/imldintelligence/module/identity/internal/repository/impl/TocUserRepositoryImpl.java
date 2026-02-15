package xenosoft.imldintelligence.module.identity.internal.repository.impl;

import xenosoft.imldintelligence.module.identity.internal.repository.TocUserRepository;
import xenosoft.imldintelligence.module.identity.internal.repository.mybatis.TocUserMapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import xenosoft.imldintelligence.module.identity.internal.model.TocUser;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TocUserRepositoryImpl implements TocUserRepository {
    private final TocUserMapper tocUserMapper;

    @Override
    public Optional<TocUser> findById(Long tenantId, Long id) {
        return Optional.ofNullable(tocUserMapper.findById(tenantId, id));
    }

    @Override
    public Optional<TocUser> findByTocUid(Long tenantId, String tocUid) {
        return Optional.ofNullable(tocUserMapper.findByTocUid(tenantId, tocUid));
    }

    @Override
    public List<TocUser> listByTenantId(Long tenantId) {
        return tocUserMapper.listByTenantId(tenantId);
    }

    @Override
    public TocUser save(TocUser tocUser) {
        tocUserMapper.insert(tocUser);
        return tocUser;
    }

    @Override
    public TocUser update(TocUser tocUser) {
        tocUserMapper.update(tocUser);
        return tocUser;
    }

    @Override
    public Boolean deleteById(Long tenantId, Long id) {
        return tocUserMapper.deleteById(tenantId, id) > 0;
    }
}
