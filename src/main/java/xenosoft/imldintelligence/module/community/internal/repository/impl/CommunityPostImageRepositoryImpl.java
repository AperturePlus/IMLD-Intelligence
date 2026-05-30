package xenosoft.imldintelligence.module.community.internal.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import xenosoft.imldintelligence.module.community.internal.model.CommunityPostImage;
import xenosoft.imldintelligence.module.community.internal.repository.CommunityPostImageRepository;
import xenosoft.imldintelligence.module.community.internal.repository.mybatis.CommunityPostImageMapper;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CommunityPostImageRepositoryImpl implements CommunityPostImageRepository {
    private final CommunityPostImageMapper communityPostImageMapper;

    @Override
    public Optional<CommunityPostImage> findById(Long tenantId, Long id) {
        return Optional.ofNullable(communityPostImageMapper.selectOne(new LambdaQueryWrapper<CommunityPostImage>()
                .eq(CommunityPostImage::getTenantId, tenantId)
                .eq(CommunityPostImage::getId, id)));
    }

    @Override
    public List<CommunityPostImage> listByPostId(Long tenantId, Long postId) {
        return communityPostImageMapper.selectList(new LambdaQueryWrapper<CommunityPostImage>()
                .eq(CommunityPostImage::getTenantId, tenantId)
                .eq(CommunityPostImage::getPostId, postId)
                .orderByAsc(CommunityPostImage::getSortOrder)
                .orderByAsc(CommunityPostImage::getId));
    }

    @Override
    public CommunityPostImage save(CommunityPostImage image) {
        communityPostImageMapper.insert(image);
        return image;
    }

    @Override
    public void deleteByPostId(Long tenantId, Long postId) {
        communityPostImageMapper.delete(new LambdaQueryWrapper<CommunityPostImage>()
                .eq(CommunityPostImage::getTenantId, tenantId)
                .eq(CommunityPostImage::getPostId, postId));
    }
}
