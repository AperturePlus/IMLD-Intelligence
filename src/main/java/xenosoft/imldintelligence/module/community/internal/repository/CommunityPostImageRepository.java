package xenosoft.imldintelligence.module.community.internal.repository;

import xenosoft.imldintelligence.module.community.internal.model.CommunityPostImage;

import java.util.List;
import java.util.Optional;

public interface CommunityPostImageRepository {
    Optional<CommunityPostImage> findById(Long tenantId, Long id);

    List<CommunityPostImage> listByPostId(Long tenantId, Long postId);

    CommunityPostImage save(CommunityPostImage image);

    void deleteByPostId(Long tenantId, Long postId);
}
