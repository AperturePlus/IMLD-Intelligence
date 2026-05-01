package xenosoft.imldintelligence.module.community.internal.repository.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import xenosoft.imldintelligence.module.community.internal.repository.CommunityBookmarkRepository;
import xenosoft.imldintelligence.module.community.internal.repository.mybatis.CommunityPostBookmarkMapper;

@Repository
@RequiredArgsConstructor
public class CommunityBookmarkRepositoryImpl implements CommunityBookmarkRepository {
    private final CommunityPostBookmarkMapper communityPostBookmarkMapper;

    @Override
    public boolean bookmark(Long tenantId, Long postId, Long tocUserId) {
        return communityPostBookmarkMapper.insertIgnore(tenantId, postId, tocUserId) > 0;
    }

    @Override
    public boolean unbookmark(Long tenantId, Long postId, Long tocUserId) {
        return communityPostBookmarkMapper.deleteByUnique(tenantId, postId, tocUserId) > 0;
    }
}

