package xenosoft.imldintelligence.module.community.internal.repository.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import xenosoft.imldintelligence.module.community.internal.repository.CommunityLikeRepository;
import xenosoft.imldintelligence.module.community.internal.repository.mybatis.CommunityPostLikeMapper;

@Repository
@RequiredArgsConstructor
public class CommunityLikeRepositoryImpl implements CommunityLikeRepository {
    private final CommunityPostLikeMapper communityPostLikeMapper;

    @Override
    public boolean like(Long tenantId, Long postId, Long tocUserId) {
        return communityPostLikeMapper.insertIgnore(tenantId, postId, tocUserId) > 0;
    }

    @Override
    public boolean unlike(Long tenantId, Long postId, Long tocUserId) {
        return communityPostLikeMapper.deleteByUnique(tenantId, postId, tocUserId) > 0;
    }
}

