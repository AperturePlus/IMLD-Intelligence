package xenosoft.imldintelligence.module.community.internal.repository.mybatis;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import xenosoft.imldintelligence.module.community.internal.model.CommunityPostBookmark;

@Mapper
public interface CommunityPostBookmarkMapper extends BaseMapper<CommunityPostBookmark> {

    @Insert("INSERT INTO community_post_bookmark (tenant_id, post_id, toc_user_id) " +
            "VALUES (#{tenantId}, #{postId}, #{tocUserId}) " +
            "ON CONFLICT (tenant_id, post_id, toc_user_id) DO NOTHING")
    int insertIgnore(@Param("tenantId") Long tenantId,
                     @Param("postId") Long postId,
                     @Param("tocUserId") Long tocUserId);

    @Delete("DELETE FROM community_post_bookmark WHERE tenant_id = #{tenantId} AND post_id = #{postId} AND toc_user_id = #{tocUserId}")
    int deleteByUnique(@Param("tenantId") Long tenantId,
                       @Param("postId") Long postId,
                       @Param("tocUserId") Long tocUserId);
}

