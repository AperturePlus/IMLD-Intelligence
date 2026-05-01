package xenosoft.imldintelligence.module.community.internal.repository.mybatis;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import xenosoft.imldintelligence.module.community.internal.model.CommunityPostComment;

import java.util.List;

@Mapper
public interface CommunityPostCommentMapper extends BaseMapper<CommunityPostComment> {

    @Select({
            "<script>",
            "SELECT",
            " c.id,",
            " c.tenant_id,",
            " c.post_id,",
            " c.parent_comment_id,",
            " c.author_toc_user_id,",
            " c.author_display_name,",
            " c.anonymous_flag,",
            " c.content,",
            " c.status,",
            " c.created_at,",
            " c.updated_at,",
            " c.deleted_at,",
            " COUNT(*) OVER() AS total_count",
            "FROM community_post_comment c",
            "<where>",
            " c.tenant_id = #{tenantId} AND c.post_id = #{postId}",
            " AND c.status = 'PUBLISHED'",
            " <if test='parentCommentId != null'> AND c.parent_comment_id = #{parentCommentId}</if>",
            " <if test='excludeDeleted'> AND c.deleted_at IS NULL AND c.status &lt;&gt; 'DELETED'</if>",
            "</where>",
            "ORDER BY c.created_at ASC, c.id ASC",
            "LIMIT #{limit} OFFSET #{offset}",
            "</script>"
    })
    List<CommunityCommentPageRow> listByPostIdWithTotal(@Param("tenantId") Long tenantId,
                                                        @Param("postId") Long postId,
                                                        @Param("parentCommentId") Long parentCommentId,
                                                        @Param("excludeDeleted") boolean excludeDeleted,
                                                        @Param("offset") long offset,
                                                        @Param("limit") int limit);
}
